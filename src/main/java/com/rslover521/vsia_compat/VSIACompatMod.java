package com.rslover521.vsia_compat;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(VSIACompatMod.MODID)
public class VSIACompatMod {
	private int tickCounter = 0;
    public static final String MODID = "vsiacompatmod";
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public VSIACompatMod() {
    	@SuppressWarnings("removal")
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    	modEventBus.addListener(this::setup);
    	LOGGER.info("[VSIACompat] Compatibility mod loaded!");
    	MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
    }
    private void setup(final FMLCommonSetupEvent event) {
    	LOGGER.info("[VSIACompat] Setting up compatibility checks.");
    }
    
	private void onServerTick(ServerTickEvent event) {
    	if (++tickCounter % 100 != 0) return; // Runs every 5 seconds (20 ticks per second) for performance boost
    	
    	MinecraftServer server = event.getServer();
    	for (ServerLevel level : server.getAllLevels()) {
    		for (Entity entity: level.getEntities(null, new AABB(BlockPos.ZERO).inflate(512))) { // Adjust the .inflate() method for larger scan
    			try {
    				if(PhysicsCompat.isVSShip(entity)) {
    					LOGGER.info("[VSIACompat] VS Ship detected: " + entity.getName().getString());
    					PhysicsCompat.prelodChunksAroundShip(level, entity.blockPosition());
    					continue;
    				} 
    				
    				if (PhysicsCompat.isIAAircraft(entity)) {
    					LOGGER.info("[VSIACompat] Likely pirate ship detected: " + entity.getName().getString());
    					continue;
    				}
    				
    				if (PhysicsCompat.isLikelyPirateShip(entity)) {
    					if (!PhysicsCompat.isBoundingBoxValid(entity)) {
    						LOGGER.warn("[VSIACompat] Skipping pirate ship due to invalid bounding box!");
    						continue;
    					}
    					
    					LOGGER.info("[VSIACompat] Detected generic pirate ship: " + entity.getName().getString());
    					PhysicsCompat.prelodChunksAroundShip(level, entity.blockPosition());
    					
    					BlockPos helmPos = PhysicsCompat.findShipHelm(level, entity.blockPosition());
    					
    					if (helmPos == null) {
    						LOGGER.warn("[VSIACompat] WARNING: Purate ship is missing a ship helm!");
    					} else {
    						LOGGER.info("[VSIACompat] Ship helm found at " + helmPos);
    					}
    				}
    			} catch (Throwable t) {
    				LOGGER.error("[VSIACompat] Error checking entity for physics compatibility", t);
    			}
    		}
    	}
    }
}
