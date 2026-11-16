package com.example.hollowflayer.client;

import com.example.hollowflayer.entity.FlayerEntity;
import net.minecraft.util.Mth;

/** Вся процедурная анимация: ходьба, дёргание головы, крик, атака, захват, разрыв. */
public final class FlayerPose {
    public float headX, headY, headZ, jaw, rArmX, rArmZ, lArmX, lArmZ, bodyX, rLegX, lLegX, elbow, hand;

    private static float smooth(float x) { x = Mth.clamp(x, 0f, 1f); return x * x * (3f - 2f * x); }
    private static float hash(int n) {
        float s = (float) Math.sin(n * 12.9898) * 43758.5453f;
        return s - (float) Math.floor(s);
    }

    private static float mix(float base, float[] w, float[] v) {
        float tot = 0f, sum = 0f;
        for (int i = 0; i < w.length; i++) { tot += w[i]; sum += w[i] * v[i]; }
        float rem = Math.max(0f, 1f - tot);
        return (sum + rem * base) / (tot + rem);
    }

    public static FlayerPose compute(FlayerEntity e, float swing, float amt, float age) {
        FlayerPose p = new FlayerPose();
        boolean p2 = e.getPhase() >= 2;
        float t = e.animTime(age - e.tickCount);

        // ходьба + покачивание
        float w = swing * 0.6662f;
        p.rLegX = Mth.cos(w) * 1.3f * amt;
        p.lLegX = Mth.cos(w + Mth.PI) * 1.3f * amt;
        p.rArmX = Mth.cos(w + Mth.PI) * 0.7f * amt;
        p.lArmX = Mth.cos(w) * 0.7f * amt;
        float sway = Mth.sin(age * 0.06f) * 0.04f;
        p.rArmZ = 0.06f + sway;
        p.lArmZ = -0.06f - sway;
        p.bodyX = p2 ? 0.3f + amt * 0.2f : 0.1f;
        p.headX = p2 ? 0.35f : 0.15f;
        p.headZ = p2 ? 0.3f : 0.18f;
        p.jaw = p2 ? 0.25f + Mth.sin(age * 0.3f) * 0.05f : 0.05f;

        p.elbow = -0.3f + Mth.sin(age * 0.08f) * 0.05f;
        p.hand = 0.25f + Mth.sin(age * 0.1f) * 0.1f;

        // рваное дёргание головой
        float rnd = hash((int) (age / 3f) + e.getId() * 31);
        if (rnd > 0.8f) {
            float k = (rnd - 0.8f) / 0.2f - 0.5f;
            p.headY = k * 2.4f;
            p.headZ += k * 1.0f;
        }

        switch (e.getAnim()) {
            case FlayerEntity.A_SCREAM -> {
                float in = smooth(t / 8f);
                p.rLegX = Mth.lerp(in, p.rLegX, 0f);
                p.lLegX = Mth.lerp(in, p.lLegX, 0f);
                p.jaw = Mth.lerp(in, p.jaw, 1.15f);
                p.headX = Mth.lerp(in, p.headX, -0.95f);
                p.headY *= (1f - in);
                p.headZ = Mth.lerp(in, p.headZ, 0f);
                p.rArmX = Mth.lerp(in, p.rArmX, -2.5f);
                p.lArmX = Mth.lerp(in, p.lArmX, -2.5f);
                p.rArmZ = Mth.lerp(in, p.rArmZ, 0.8f);
                p.lArmZ = Mth.lerp(in, p.lArmZ, -0.8f);
                p.bodyX = Mth.lerp(in, p.bodyX, -0.3f);
                p.elbow = Mth.lerp(in, p.elbow, -0.45f);
                p.hand = Mth.lerp(in, p.hand, 0.5f);
                float sh = Mth.sin(age * 2.9f) * 0.09f * in;
                p.headY += sh; p.rArmZ += sh; p.lArmZ -= sh;
            }
            case FlayerEntity.A_SHED -> {
                float sc  = smooth(t / 6f) * (1f - smooth((t - 10f) / 6f));
                float hd  = smooth((t - 10f) / 6f) * (1f - smooth((t - 34f) / 6f));
                float ch  = smooth((t - 34f) / 5f) * (1f - smooth((t - 48f) / 5f));
                float lg  = smooth((t - 48f) / 6f) * (1f - smooth((t - 64f) / 6f));
                float fin = smooth((t - 64f) / 8f);
                float[] w = {sc, hd, ch, lg, fin};
                float tear = Mth.sin(age * 1.3f);
                float busy = hd + ch + lg;
                p.rLegX = p.lLegX = 0f;
                p.rArmX = mix(p.rArmX, w, new float[]{-2.5f, -2.6f + tear * 0.25f, -1.2f, -0.1f, -2.5f});
                p.lArmX = mix(p.lArmX, w, new float[]{-2.5f, -2.6f - tear * 0.25f, -1.2f, -0.1f, -2.5f});
                p.rArmZ = mix(p.rArmZ, w, new float[]{0.8f, 0.25f, -0.5f, 0.15f, 0.8f});
                p.lArmZ = mix(p.lArmZ, w, new float[]{-0.8f, -0.25f, 0.5f, -0.15f, -0.8f});
                p.elbow = mix(p.elbow, w, new float[]{-0.45f, -2.1f, -1.6f, -0.5f, -0.45f});
                p.hand = mix(p.hand, w, new float[]{0.5f, -1.2f, -1.0f, -1.1f, 0.5f});
                p.bodyX = mix(p.bodyX, w, new float[]{-0.3f, 0.1f, 0.25f, 0.95f, -0.3f})
                        + Mth.sin(age * 2.2f) * 0.08f * busy;
                p.headX = mix(p.headX, w, new float[]{-0.95f, -0.3f, 0.5f, 0.8f, -0.95f});
                p.jaw = mix(p.jaw, w, new float[]{1.15f, 0.9f, 0.7f, 0.6f, 1.25f});
                p.headY = Mth.sin(age * 2.7f) * 0.7f * busy + p.headY * (1f - busy);
                p.headZ = Mth.sin(age * 2.1f) * 0.5f * busy + p.headZ * (1f - busy);
            }
            case FlayerEntity.A_ATTACK -> {
                float k = t / 14f;
                float raise = smooth(k / 0.45f);
                float slash = smooth((k - 0.45f) / 0.25f);
                float rec = smooth((k - 0.8f) / 0.2f);
                float rx = Mth.lerp(slash, -2.8f * raise, 0.7f);
                float lx = Mth.lerp(slash, -2.4f * raise, 0.4f);
                p.rArmX = Mth.lerp(rec, rx, p.rArmX);
                p.lArmX = Mth.lerp(rec, lx, p.lArmX);
                p.rArmZ += 0.3f * raise * (1f - rec);
                p.lArmZ -= 0.3f * raise * (1f - rec);
                p.bodyX += (0.5f * slash - 0.25f * raise) * (1f - rec);
                p.jaw = Mth.lerp(slash * (1f - rec), p.jaw, 0.9f);
                p.elbow = Mth.lerp(rec, Mth.lerp(slash, -0.5f * raise, -0.1f), p.elbow);
                p.hand = Mth.lerp(rec, Mth.lerp(slash, 0.2f, -0.9f), p.hand);
            }
            case FlayerEntity.A_GRAB -> {
                float in = smooth(t / 6f);
                p.rLegX = p.lLegX = 0f;
                p.rArmX = Mth.lerp(in, p.rArmX, -1.55f);
                p.lArmX = Mth.lerp(in, p.lArmX, -1.55f);
                p.rArmZ = Mth.lerp(in, p.rArmZ, 0.25f);
                p.lArmZ = Mth.lerp(in, p.lArmZ, -0.25f);
                p.headX = Mth.lerp(in, p.headX, 0.55f);
                p.jaw = Mth.lerp(in, p.jaw, 0.35f + Mth.sin(age * 1.4f) * 0.25f);
                p.bodyX = Mth.lerp(in, p.bodyX, 0.25f);
                p.elbow = Mth.lerp(in, p.elbow, -1.0f);
                p.hand = Mth.lerp(in, p.hand, -1.0f);
                float sh = Mth.sin(age * 2.5f) * 0.04f;
                p.rArmZ += sh; p.lArmZ -= sh;
            }
            case FlayerEntity.A_TEAR -> {
                float sp = smooth((t - 4f) / 8f);
                p.rLegX = p.lLegX = 0f;
                p.rArmX = Mth.lerp(sp, -1.55f, -1.95f);
                p.lArmX = p.rArmX;
                p.rArmZ = 0.25f + 1.45f * sp;
                p.lArmZ = -p.rArmZ;
                p.headX = Mth.lerp(sp, 0.55f, -0.9f);
                p.headY *= (1f - sp);
                p.jaw = Mth.lerp(sp, 0.4f, 1.25f);
                p.bodyX = Mth.lerp(sp, 0.25f, -0.25f);
                p.elbow = Mth.lerp(sp, -1.0f, -0.5f);
                p.hand = Mth.lerp(sp, -1.0f, -1.3f);
                float sh = Mth.sin(age * 3.4f) * 0.12f * sp;
                p.rArmZ += sh; p.lArmZ -= sh;
            }
            default -> { }
        }
        return p;
    }
}
