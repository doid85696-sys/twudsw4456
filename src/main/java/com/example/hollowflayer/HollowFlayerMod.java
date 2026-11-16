package com.example.hollowflayer;

import com.example.hollowflayer.entity.FlayerEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HollowFlayerMod.MODID)
public class HollowFlayerMod {
    public static final String MODID = "hollowflayer";

    public HollowFlayerMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(bus);
        ModEntities.ITEMS.register(bus);

        bus.addListener((EntityAttributeCreationEvent e) ->
                e.put(ModEntities.FLAYER.get(), FlayerEntity.createAttributes().build()));

        bus.addListener((SpawnPlacementRegisterEvent e) ->
                e.register(ModEntities.FLAYER.get(), SpawnPlacements.Type.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules,
                        SpawnPlacementRegisterEvent.Operation.OR));

        bus.addListener((BuildCreativeModeTabContentsEvent e) -> {
            if (e.getTabKey() == CreativeModeTabs.SPAWN_EGGS) e.accept(ModEntities.FLAYER_EGG);
        });
    }
}
