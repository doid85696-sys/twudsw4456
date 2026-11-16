package com.example.hollowflayer.client;

import com.example.hollowflayer.HollowFlayerMod;
import com.example.hollowflayer.entity.FlayerEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class FlayerEyesLayer extends EyesLayer<FlayerEntity, FlayerModel> {
    private static final RenderType EYES = RenderType.eyes(
            new ResourceLocation(HollowFlayerMod.MODID, "textures/entity/flayer_eyes.png"));

    public FlayerEyesLayer(RenderLayerParent<FlayerEntity, FlayerModel> parent) { super(parent); }

    @Override
    public RenderType renderType() { return EYES; }
}
