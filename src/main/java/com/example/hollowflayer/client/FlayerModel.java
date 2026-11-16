package com.example.hollowflayer.client;

import com.example.hollowflayer.entity.FlayerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

public class FlayerModel extends EntityModel<FlayerEntity> {
    private final FlayerRawModel raw;
    private final PlayerModel<FlayerEntity> skinned;
    private boolean useSkinned;

    public FlayerModel(EntityRendererProvider.Context ctx) {
        super(RenderType::entityCutoutNoCull);
        this.raw = new FlayerRawModel(ctx.bakeLayer(FlayerRawModel.LAYER));
        this.skinned = new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false);
    }

    @Override
    public void setupAnim(FlayerEntity e, float swing, float amt, float age, float yaw, float pitch) {
        useSkinned = e.getVictim().isPresent();
        FlayerPose p = FlayerPose.compute(e, swing, amt, age);
        float hy = yaw * Mth.DEG_TO_RAD + p.headY;
        float hx = pitch * Mth.DEG_TO_RAD;

        raw.body.xRot = p.bodyX;
        raw.neck.xRot = 0.4f * p.headX;
        raw.head.xRot = hx + 0.6f * p.headX;
        raw.head.yRot = hy;
        raw.head.zRot = p.headZ;
        raw.jaw.xRot = p.jaw;
        raw.rArm.xRot = p.rArmX; raw.rArm.zRot = p.rArmZ;
        raw.lArm.xRot = p.lArmX; raw.lArm.zRot = p.lArmZ;
        raw.rFore.xRot = p.elbow; raw.lFore.xRot = p.elbow;
        raw.rHand.xRot = p.hand;  raw.lHand.xRot = p.hand;
        raw.rLeg.xRot = p.rLegX - 0.3f; raw.lLeg.xRot = p.lLegX - 0.3f;
        raw.rShin.xRot = 0.55f + Mth.abs(p.rLegX) * 0.4f;
        raw.lShin.xRot = 0.55f + Mth.abs(p.lLegX) * 0.4f;

        skinned.body.xRot = 0f;
        skinned.head.xRot = hx + p.headX - 0.3f * p.jaw; skinned.head.yRot = hy; skinned.head.zRot = p.headZ;
        skinned.rightArm.xRot = p.rArmX; skinned.rightArm.zRot = p.rArmZ;
        skinned.leftArm.xRot = p.lArmX; skinned.leftArm.zRot = p.lArmZ;
        skinned.rightLeg.xRot = p.rLegX * 0.8f; skinned.leftLeg.xRot = p.lLegX * 0.8f;
        skinned.hat.copyFrom(skinned.head);
        skinned.jacket.copyFrom(skinned.body);
        skinned.rightSleeve.copyFrom(skinned.rightArm);
        skinned.leftSleeve.copyFrom(skinned.leftArm);
        skinned.rightPants.copyFrom(skinned.rightLeg);
        skinned.leftPants.copyFrom(skinned.leftLeg);
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay,
                               float r, float g, float b, float a) {
        if (useSkinned) {
            float s = 1.7f; // растянутая кожа игрока
            ps.pushPose();
            ps.translate(0f, 1.5f * (1f - s), 0f);
            ps.scale(0.95f, s, 0.95f);
            skinned.renderToBuffer(ps, vc, light, overlay, r, g, b, a);
            ps.popPose();
        } else {
            raw.root.render(ps, vc, light, overlay, r, g, b, a);
        }
    }

    /** Слой "кожи" поверх мяса: части кожи исчезают по очереди (голова, руки, ноги, торс). t - тики анимации. */
    public void renderSkin(PoseStack ps, VertexConsumer vc, int light, int overlay, float t) {
        if (useSkinned || t >= 66f) return;
        raw.head.visible = t < 24f;
        raw.rArm.visible = raw.lArm.visible = t < 40f;
        raw.rLeg.visible = raw.lLeg.visible = t < 56f;
        raw.root.render(ps, vc, light, overlay, 1f, 1f, 1f, 1f);
        raw.head.visible = raw.rArm.visible = raw.lArm.visible = raw.rLeg.visible = raw.lLeg.visible = true;
    }
}
