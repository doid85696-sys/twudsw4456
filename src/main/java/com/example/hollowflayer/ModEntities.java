package com.example.hollowflayer;

import com.example.hollowflayer.entity.FlayerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HollowFlayerMod.MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, HollowFlayerMod.MODID);

    public static final RegistryObject<EntityType<FlayerEntity>> FLAYER = ENTITIES.register("flayer",
            () -> EntityType.Builder.of(FlayerEntity::new, MobCategory.MONSTER)
                    .sized(0.7f, 3.4f).clientTrackingRange(10)
                    .build(HollowFlayerMod.MODID + ":flayer"));

    public static final RegistryObject<Item> FLAYER_EGG = ITEMS.register("flayer_spawn_egg",
            () -> new ForgeSpawnEggItem(FLAYER, 0x3a0000, 0xb8b09a, new Item.Properties()));
}
