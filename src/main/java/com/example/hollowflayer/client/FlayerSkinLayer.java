package com.example.hollowflayer.client;

import com.example.hollowflayer.HollowFlayerMod;
import com.example.hollowflayer.entity.FlayerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

/** Во время срыва кожи рисует кожу поверх мяса и снимает её по частям. */
public class FlayerSkinLayer extends RenderLayer<FlayerEntity, FlayerModel> {
    private static final ResourceLocation SKIN =
            new ResourceLocation(HollowFlayerMod.MODID, "textures/entity/flayer.png");

    public FlayerSkinLayer(RenderLayerParent<FlayerEntity, FlayerModel> parent) { super(parent); }

    @Override
    public void render(PoseStack ps, MultiBufferSource buf, int light, FlayerEntity e, float limbSwing,
                       float limbSwingAmount, float partial, float age, float yaw, float pitch) {
        if (e.getAnim() != FlayerEntity.A_SHED || e.getVictim().isPresent()) return;
        VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull(SKIN));
        ps.pushPose();
        ps.scale(1.05f, 1.01f, 1.05f); // кожа чуть "мешком" поверх мяса
        getParentModel().renderSkin(ps, vc, light, LivingEntityRenderer.getOverlayCoords(e, 0f), e.animTime(partial));
        ps.popPose();
    }
}
