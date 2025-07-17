package com.rslover521.vsia_compat;

import org.slf4j.Logger;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RescueShipCommand {
	public static final Logger LOGGER = LogUtils.getLogger();
	
	public static void register (CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("rescue_ship").requires(source -> true).executes(context -> {
			ServerPlayer player = context.getSource().getPlayerOrException();
			
			if(tryFixShipIfOnIt(player)) {
				context.getSource().sendSuccess(() -> Component.literal("Ship rescued successfully!"), false);
			} else {
				context.getSource().sendFailure(Component.literal("You're not on a ship. Stand on a ship and try again."));
			}
			
			
			return Command.SINGLE_SUCCESS;
		}));
	}
	public static boolean tryFixShipIfOnIt(ServerPlayer player) {
		try {
			// Checks if the player is standing on a ship. Checking the block entity underneath
			BlockPos playerPos = player.blockPosition();
			Entity vehicle = player.getVehicle();
			
			// Option A: Try to get the entity below the player
			if (vehicle != null && vehicle.getType().toString().toLowerCase().contains("ship_mounting")) {
				LOGGER.info("[VSIACompat] Player is mounted on a ship: " + vehicle);
				// fixShipAt(vehicle.blockPosition(), player.level());
				return true;
			}
			BlockPos below = playerPos.below();
			BlockEntity entityBelow = player.level().getBlockEntity(below);
			if (entityBelow != null && entityBelow.getClass().getSimpleName().toLowerCase().contains("ship")) {
				LOGGER.info("[VSIACompat] Ship block entity detected under player: " + entityBelow);
				fixShipAt((ServerLevel) player.level(), below);
				return true;
			}
			
		} catch (Exception e) {
			LOGGER.warn("[VSIACompat] Player is not on a ship or ship cannot be fixed: ", e.getMessage());
			return false;
		}
		return false;
	}
	public static boolean fixShipAt(ServerLevel level, BlockPos origin) {
		BlockPos helmPos = PhysicsCompat.findShipHelm(level, origin);
		
		if (helmPos == null) {
			LOGGER.warn("[VSIACompat] No Ship Helm found at or near " + origin);
			return false;
		}
		
		if (helmPos.equals(origin)) {
			LOGGER.info("[VSIACompat] Ship Helm already at origin: " + origin);
			return true;
		}
		
		BlockState helmState = level.getBlockState(helmPos);
		level.setBlockAndUpdate(origin, helmState);
		level.removeBlock(helmPos, false);
		LOGGER.info("[VSIACompat] Moved Ship Helm from" + helmPos + " to " + origin);
		return true;
	}
}
