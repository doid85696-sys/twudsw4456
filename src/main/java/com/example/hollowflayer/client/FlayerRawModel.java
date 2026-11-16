package com.example.hollowflayer.client;

import com.example.hollowflayer.HollowFlayerMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

/** Модель (сгенерирована tools/gen_model.py): шея, рога, рёбра, шипы, зубы, локти, колени, когти. 54 px = 3.4 блока. */
public class FlayerRawModel {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(HollowFlayerMod.MODID, "flayer"), "main");

    public final ModelPart root, body, neck, head, jaw;
    public final ModelPart rArm, rFore, rHand, lArm, lFore, lHand;
    public final ModelPart rLeg, rShin, lLeg, lShin;

    public FlayerRawModel(ModelPart root) {
        this.root = root;
        body = root.getChild("body");
        neck = body.getChild("neck");
        head = neck.getChild("head");
        jaw = head.getChild("jaw");
        rArm = body.getChild("right_arm");
        rFore = rArm.getChild("right_fore");
        rHand = rFore.getChild("right_hand");
        lArm = body.getChild("left_arm");
        lFore = lArm.getChild("left_fore");
        lHand = lFore.getChild("left_hand");
        rLeg = root.getChild("right_leg");
        rShin = rLeg.getChild("right_shin");
        lLeg = root.getChild("left_leg");
        lShin = lLeg.getChild("left_shin");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 0).addBox(-3f, -18f, -2f, 6f, 18f, 4f), PartPose.offset(0f, 0f, 0f));
        PartDefinition spine = body.addOrReplaceChild("spine", CubeListBuilder.create().texOffs(52, 14).addBox(-0.5f, -17f, 2f, 1f, 3f, 1f).texOffs(52, 14).addBox(-0.5f, -14f, 2f, 1f, 3f, 1f).texOffs(52, 14).addBox(-0.5f, -11f, 2f, 1f, 3f, 1f).texOffs(52, 14).addBox(-0.5f, -8f, 2f, 1f, 3f, 1f).texOffs(52, 14).addBox(-0.5f, -5f, 2f, 1f, 3f, 1f).texOffs(52, 14).addBox(-0.5f, -2f, 2f, 1f, 3f, 1f).texOffs(52, 18).addBox(-1f, -16f, 2f, 2f, 4f, 2f).texOffs(52, 18).addBox(-1f, -10f, 2f, 2f, 4f, 2f).texOffs(52, 18).addBox(-1f, -4f, 2f, 2f, 4f, 2f), PartPose.offset(0f, 0f, 0f));
        PartDefinition ribs = body.addOrReplaceChild("ribs", CubeListBuilder.create().texOffs(52, 8).addBox(-2.5f, -16f, -3f, 5f, 1f, 1f).texOffs(52, 8).addBox(-2.5f, -13f, -3f, 5f, 1f, 1f).texOffs(52, 8).addBox(-2.5f, -10f, -3f, 5f, 1f, 1f).texOffs(52, 8).addBox(-2.5f, -7f, -3f, 5f, 1f, 1f).texOffs(52, 8).addBox(-2.5f, -4f, -3f, 5f, 1f, 1f), PartPose.offset(0f, 0f, 0f));
        PartDefinition sternum = body.addOrReplaceChild("sternum", CubeListBuilder.create().texOffs(52, 18).addBox(-0.5f, -17f, -3f, 1f, 13f, 1f), PartPose.offset(0f, 0f, 0f));
        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(24, 32).addBox(-1f, -4f, -1f, 2f, 4f, 2f), PartPose.offset(0f, -18f, 0f));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4f, -8f, -4f, 8f, 5f, 8f).texOffs(56, 14).addBox(-3.5f, -3f, -4f, 1f, 2f, 1f).texOffs(56, 14).addBox(-1.5f, -3f, -4f, 1f, 2f, 1f).texOffs(56, 14).addBox(0.5f, -3f, -4f, 1f, 2f, 1f).texOffs(56, 14).addBox(2.5f, -3f, -4f, 1f, 2f, 1f).texOffs(32, 22).addBox(-4.5f, -7f, -5f, 9f, 1f, 1f).texOffs(52, 18).addBox(-5f, -6f, -1f, 1f, 1f, 3f).texOffs(52, 18).addBox(4f, -6f, -1f, 1f, 1f, 3f), PartPose.offset(0f, -4f, 0f));
        PartDefinition horn_r = head.addOrReplaceChild("horn_r", CubeListBuilder.create().texOffs(52, 18).addBox(-1f, -5f, -1f, 2f, 5f, 2f), PartPose.offsetAndRotation(-3f, -8f, 0f, 0f, 0f, -0.4f));
        PartDefinition horn_r_tip = horn_r.addOrReplaceChild("horn_r_tip", CubeListBuilder.create().texOffs(52, 0).addBox(-0.5f, -4f, -0.5f, 1f, 4f, 1f), PartPose.offsetAndRotation(0f, -5f, 0f, -0.3f, 0f, 0.6f));
        PartDefinition horn_l = head.addOrReplaceChild("horn_l", CubeListBuilder.create().texOffs(52, 18).addBox(-1f, -5f, -1f, 2f, 5f, 2f), PartPose.offsetAndRotation(3f, -8f, 0f, 0f, 0f, 0.4f));
        PartDefinition horn_l_tip = horn_l.addOrReplaceChild("horn_l_tip", CubeListBuilder.create().texOffs(52, 0).addBox(-0.5f, -4f, -0.5f, 1f, 4f, 1f), PartPose.offsetAndRotation(0f, -5f, 0f, -0.3f, 0f, -0.6f));
        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 16).addBox(-4f, 0f, -7f, 8f, 3f, 8f).texOffs(56, 14).addBox(-3.5f, -2f, -7f, 1f, 2f, 1f).texOffs(56, 14).addBox(-1.5f, -2f, -7f, 1f, 2f, 1f).texOffs(56, 14).addBox(0.5f, -2f, -7f, 1f, 2f, 1f).texOffs(56, 14).addBox(2.5f, -2f, -7f, 1f, 2f, 1f).texOffs(52, 18).addBox(-5f, -5f, -6f, 1f, 5f, 1f).texOffs(52, 18).addBox(4f, -5f, -6f, 1f, 5f, 1f), PartPose.offset(0f, -3f, 3f));
        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 32).addBox(-1.5f, -1f, -1.5f, 3f, 13f, 3f), PartPose.offset(-4.5f, -16f, 0f));
        PartDefinition right_spike = right_arm.addOrReplaceChild("right_spike", CubeListBuilder.create().texOffs(52, 18).addBox(-1f, -5f, -1f, 2f, 5f, 2f), PartPose.offsetAndRotation(-1.5f, -1f, 0f, 0f, 0f, -0.9f));
        PartDefinition right_fore = right_arm.addOrReplaceChild("right_fore", CubeListBuilder.create().texOffs(0, 48).addBox(-1.5f, 0f, -1.5f, 3f, 13f, 3f).texOffs(52, 18).addBox(-0.5f, -1f, 1.5f, 1f, 1f, 3f), PartPose.offset(0f, 12f, 0f));
        PartDefinition right_hand = right_fore.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(56, 0).addBox(-1.5f, 0f, -0.5f, 1f, 3f, 1f).texOffs(56, 0).addBox(-0.5f, 0f, -0.5f, 1f, 3f, 1f).texOffs(56, 0).addBox(0.5f, 0f, -0.5f, 1f, 3f, 1f), PartPose.offset(0f, 13f, 0f));
        PartDefinition right_tips = right_hand.addOrReplaceChild("right_tips", CubeListBuilder.create().texOffs(56, 0).addBox(-1.5f, 0f, -0.5f, 1f, 3f, 1f).texOffs(56, 0).addBox(-0.5f, 0f, -0.5f, 1f, 3f, 1f).texOffs(56, 0).addBox(0.5f, 0f, -0.5f, 1f, 3f, 1f), PartPose.offsetAndRotation(0f, 3f, 0f, -0.5f, 0f, 0f));
        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(12, 32).addBox(-1.5f, -1f, -1.5f, 3f, 13f, 3f), PartPose.offset(4.5f, -16f, 0f));
        PartDefinition left_spike = left_arm.addOrReplaceChild("left_spike", CubeListBuilder.create().texOffs(52, 18).addBox(-1f, -5f, -1f, 2f, 5f, 2f), PartPose.offsetAndRotation(1.5f, -1f, 0f, 0f, 0f, 0.9f));
        PartDefinition left_fore = left_arm.addOrReplaceChild("left_fore", CubeListBuilder.create().texOffs(12, 48).addBox(-1.5f, 0f, -1.5f, 3f, 13f, 3f).texOffs(52, 18).addBox(-0.5f, -1f, 1.5f, 1f, 1f, 3f), PartPose.offset(0f, 12f, 0f));
        PartDefinition left_hand = left_fore.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(56, 0).addBox(-1.5f, 0f, -0.5f, 1f, 3f, 1f).texOffs(56, 0).addBox(-0.5f, 0f, -0.5f, 1f, 3f, 1f).texOffs(56, 0).addBox(0.5f, 0f, -0.5f, 1f, 3f, 1f), PartPose.offset(0f, 13f, 0f));
        PartDefinition left_tips = left_hand.addOrReplaceChild("left_tips", CubeListBuilder.create().texOffs(56, 0).addBox(-1.5f, 0f, -0.5f, 1f, 3f, 1f).texOffs(56, 0).addBox(-0.5f, 0f, -0.5f, 1f, 3f, 1f).texOffs(56, 0).addBox(0.5f, 0f, -0.5f, 1f, 3f, 1f), PartPose.offsetAndRotation(0f, 3f, 0f, -0.5f, 0f, 0f));
        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(32, 32).addBox(-2f, 0f, -2f, 4f, 12f, 4f), PartPose.offset(-1.5f, 0f, 0f));
        PartDefinition right_shin = right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create().texOffs(32, 47).addBox(-1.5f, 0f, -1.5f, 3f, 12f, 3f).texOffs(52, 18).addBox(-0.5f, -1f, -4.5f, 1f, 1f, 3f), PartPose.offset(0f, 12f, 0f));
        PartDefinition right_foot = right_shin.addOrReplaceChild("right_foot", CubeListBuilder.create().texOffs(32, 25).addBox(-2f, -1f, -5f, 4f, 1f, 6f).texOffs(52, 18).addBox(-1.5f, -1f, -8f, 1f, 1f, 3f).texOffs(52, 18).addBox(-0.5f, -1f, -8f, 1f, 1f, 3f).texOffs(52, 18).addBox(0.5f, -1f, -8f, 1f, 1f, 3f).texOffs(52, 18).addBox(-0.5f, -3f, 1f, 1f, 2f, 2f), PartPose.offset(0f, 12f, 0f));
        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(48, 32).addBox(-2f, 0f, -2f, 4f, 12f, 4f), PartPose.offset(1.5f, 0f, 0f));
        PartDefinition left_shin = left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create().texOffs(48, 47).addBox(-1.5f, 0f, -1.5f, 3f, 12f, 3f).texOffs(52, 18).addBox(-0.5f, -1f, -4.5f, 1f, 1f, 3f), PartPose.offset(0f, 12f, 0f));
        PartDefinition left_foot = left_shin.addOrReplaceChild("left_foot", CubeListBuilder.create().texOffs(32, 25).addBox(-2f, -1f, -5f, 4f, 1f, 6f).texOffs(52, 18).addBox(-1.5f, -1f, -8f, 1f, 1f, 3f).texOffs(52, 18).addBox(-0.5f, -1f, -8f, 1f, 1f, 3f).texOffs(52, 18).addBox(0.5f, -1f, -8f, 1f, 1f, 3f).texOffs(52, 18).addBox(-0.5f, -3f, 1f, 1f, 2f, 2f), PartPose.offset(0f, 12f, 0f));
        return LayerDefinition.create(mesh, 64, 64);
    }
}
