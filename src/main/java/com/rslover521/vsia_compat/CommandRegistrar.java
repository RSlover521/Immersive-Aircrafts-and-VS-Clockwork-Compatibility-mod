package com.rslover521.vsia_compat;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CommandRegistrar {
	
	@SubscribeEvent
	public static void onRegisterCommands (RegisterCommandsEvent event) {
		RescueShipCommand.register(event.getDispatcher());
	}
}
