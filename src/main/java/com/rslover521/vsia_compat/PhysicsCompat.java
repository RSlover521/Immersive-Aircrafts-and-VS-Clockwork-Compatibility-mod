package com.rslover521.vsia_compat;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;


public class PhysicsCompat {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static boolean isVSShip(Entity entity) {
		if(!ModList.get().isLoaded("valkyrienskies")) {
			LOGGER.info("Valkyrien Skies is not installed. Skipping VS check.");
			return false;
		}
		try {
			String className = entity.getClass().getName().toLowerCase();
			return className.contains("valkyrienskies") || className.contains("vs_");
		} catch (Exception e){
			LOGGER.error("[VSIACompat] Failed to check if entity is a VS ship", e);
			return false;
		}
	}
	public static boolean isIAAircraft(Entity entity) {
		if (!ModList.get().isLoaded("immersive_aircraft")) {
			LOGGER.warn("[VSIACompat] Immersive Aircraft is not installed. Skipping IA check");
			return false;
		}
		try {
			ResourceLocation key = EntityTypeKeyHelper.getRegistryKey(entity);
			return key != null && key.getNamespace().equals("immersive_aircrafts");
		} catch (Exception e) {
			
			return false;
		}
	}
	
	public static BlockPos findShipHelm(ServerLevel level, BlockPos center) {
		for(int dx = -6; dx <= 6; dx++) {
			for(int dy = -6; dy <= 6; dy++) {
				for(int dz = -6; dz <= 6; dz++) {
					BlockPos check = center.offset(dx, dy, dz);
					Block block = level.getBlockState(check).getBlock();
					ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
					if (id != null) {
						if (id.getPath().equals("ship_helm") || id.getPath().equals("controller")){
							LOGGER.info("[VSIACompat] Ship Helm found at " + check + ": " + id );
							return check;
						};
					} 
				}
			}
		}
		LOGGER.info("[VSIACompat] Ship Helm not found anywhere on this ship. Automatically creating Ship Helm.");
		return null;
	}
	
	public static boolean isBoundingBoxValid(Entity entity) {
		try {
			AABB box = entity.getBoundingBox();
			double volume = box.getXsize() * box.getYsize() * box.getZsize();
			return volume < 100_000;
 		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	public static void prelodChunksAroundShip(ServerLevel level, BlockPos pos) {
		int chunkX = pos.getX() >> 4;
		int chunkZ = pos.getZ() >> 4;
		
		for(int dx = -2; dx <= 2; dx++) {
			for(int dz = -2; dx <= 2; dz++) {
				level.setChunkForced(chunkX + dx, chunkZ + dz, true);
			}
		}
		LOGGER.info("[VSIACompat] Preloaded chunks around ship at" + pos.toShortString());
	}
	public static boolean isLikelyPirateShip(Entity entity) {
		ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
		try {
			if (key != null) {
				String namespace = key.getNamespace();
				String path = key.getPath();
				return namespace.contains("pirate") || path.contains("airships") || path.contains("balloon");
			}
		} catch (Exception e) {
			LOGGER.error("[VSIACompat] Failed to check if entity is likely pirate ship", e);
		}
		return false;
	}
}
