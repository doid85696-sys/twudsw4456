package com.example.hollowflayer.client;

import java.util.Optional;
import java.util.UUID;

import com.example.hollowflayer.HollowFlayerMod;
import com.example.hollowflayer.entity.FlayerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;

public class FlayerRenderer extends MobRenderer<FlayerEntity, FlayerModel> {
    private static final ResourceLocation SKIN =
            new ResourceLocation(HollowFlayerMod.MODID, "textures/entity/flayer.png");
    private static final ResourceLocation RAW =
            new ResourceLocation(HollowFlayerMod.MODID, "textures/entity/flayer_raw.png");

    public FlayerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new FlayerModel(ctx), 0.6f);
        addLayer(new FlayerSkinLayer(this));
        addLayer(new FlayerEyesLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(FlayerEntity e) {
        Optional<UUID> victim = e.getVictim();
        if (victim.isPresent()) {
            ClientPacketListener conn = Minecraft.getInstance().getConnection();
            if (conn != null) {
                PlayerInfo info = conn.getPlayerInfo(victim.get());
                if (info != null) return info.getSkinLocation();
            }
            return DefaultPlayerSkin.getDefaultSkin(victim.get());
        }
        return (e.getPhase() >= 2 || e.getAnim() == FlayerEntity.A_SHED) ? RAW : SKIN;
    }
}
