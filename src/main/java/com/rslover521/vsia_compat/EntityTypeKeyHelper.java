package com.rslover521.vsia_compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityTypeKeyHelper {
	public static ResourceLocation getRegistryKey(Entity entity) {
		EntityType<?> type = entity.getType();
		return ForgeRegistries.ENTITY_TYPES.getKey(type);
	}
}
