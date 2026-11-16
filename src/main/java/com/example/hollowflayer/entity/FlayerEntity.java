package com.example.hollowflayer.entity;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Освежеватель. Фаза 1: высокий бледный монстр в "коже".
 * Фаза 2 (при HP <= 50%): срывает с себя кожу, становится быстрее, охотится на игрока,
 * хватает, разрывает и надевает кожу жертвы.
 */
public class FlayerEntity extends Monster {
    public static final int A_NONE = 0, A_SCREAM = 1, A_ATTACK = 2, A_GRAB = 3, A_SHED = 4, A_TEAR = 5;

    private static final EntityDataAccessor<Integer> PHASE =
            SynchedEntityData.defineId(FlayerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ANIM =
            SynchedEntityData.defineId(FlayerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> VICTIM =
            SynchedEntityData.defineId(FlayerEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.hollowflayer.flayer"),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

    private int stateTicks, screamCooldown, grabCooldown;
    private float grabDamage;
    private UUID grabbedId;
    private LivingEntity pendingTarget;
    private int animStartTick; // client

    public FlayerEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 120.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7)
                .add(Attributes.ARMOR, 4.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(PHASE, 1);
        entityData.define(ANIM, A_NONE);
        entityData.define(VICTIM, Optional.empty());
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1D, true) {
            @Override
            protected double getAttackReachSqr(LivingEntity t) {
                return 9.0 + t.getBbWidth(); // длинные руки
            }
        });
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ---------- getters (используются клиентом) ----------
    public int getPhase() { return entityData.get(PHASE); }
    public int getAnim() { return entityData.get(ANIM); }
    public Optional<UUID> getVictim() { return entityData.get(VICTIM); }
    private void setPhase(int p) { entityData.set(PHASE, p); }

    public float animTime(float partial) { return tickCount - animStartTick + partial; }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (ANIM.equals(key)) animStartTick = tickCount;
    }

    // ---------- NBT ----------
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("FlayerPhase", getPhase());
        getVictim().ifPresent(id -> tag.putUUID("FlayerVictim", id));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setPhase(Math.max(1, tag.getInt("FlayerPhase")));
        if (tag.hasUUID("FlayerVictim")) entityData.set(VICTIM, Optional.of(tag.getUUID("FlayerVictim")));
        applyPhaseStats();
        if (hasCustomName()) bossEvent.setName(getDisplayName());
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        bossEvent.setName(getDisplayName());
    }

    private void applyPhaseStats() {
        boolean p2 = getPhase() >= 2;
        AttributeInstance sp = getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance dm = getAttribute(Attributes.ATTACK_DAMAGE);
        if (sp != null) sp.setBaseValue(p2 ? 0.38 : 0.27);
        if (dm != null) dm.setBaseValue(p2 ? 12.0 : 8.0);
    }

    // ---------- boss bar ----------
    @Override public void startSeenByPlayer(ServerPlayer p) { super.startSeenByPlayer(p); bossEvent.addPlayer(p); }
    @Override public void stopSeenByPlayer(ServerPlayer p) { super.stopSeenByPlayer(p); bossEvent.removePlayer(p); }
    @Override public void remove(RemovalReason r) { super.remove(r); bossEvent.removeAllPlayers(); }

    // ---------- поведение ----------
    @Override
    protected boolean isImmobile() {
        int a = getAnim();
        return super.isImmobile() || a == A_SCREAM || a == A_SHED || a == A_GRAB || a == A_TEAR;
    }

    @Override
    public boolean removeWhenFarAway(double d) { return false; }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dims) { return 3.0f; }

    @Override
    public void setTarget(LivingEntity t) {
        boolean first = getTarget() == null && t != null;
        super.setTarget(t);
        if (first && !level().isClientSide && getAnim() == A_NONE && screamCooldown <= 0) {
            screamCooldown = 600;
            startState(A_SCREAM, 40);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (getAnim() != A_NONE) return false;
        // Фаза 2: шанс схватить игрока
        if (getPhase() >= 2 && grabCooldown <= 0 && target instanceof ServerPlayer sp
                && !sp.getAbilities().instabuild && random.nextFloat() < 0.45f) {
            grabbedId = sp.getUUID();
            grabDamage = 0f;
            startState(A_GRAB, 40);
            playSound(SoundEvents.ENDERMAN_SCREAM, 2f, 0.4f);
            return true;
        }
        if (target instanceof LivingEntity le) {
            pendingTarget = le;               // урон наносится в середине анимации
            entityData.set(ANIM, A_ATTACK);
            stateTicks = 14;
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource src, float amount) {
        int a = getAnim();
        if (a == A_SHED && !src.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        boolean r = super.hurt(src, amount);
        if (r && a == A_GRAB && !level().isClientSide) {
            grabDamage += amount;
            if (grabDamage >= 25f) releaseGrab(); // игрок может вырваться, если сильно бить
        }
        return r;
    }

    private void startState(int anim, int ticks) {
        entityData.set(ANIM, anim);
        stateTicks = ticks;
        getNavigation().stop();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        bossEvent.setProgress(getHealth() / getMaxHealth());
        if (screamCooldown > 0) screamCooldown--;
        if (grabCooldown > 0) grabCooldown--;

        // Фаза 2 ищет игроков даже без прямой видимости
        if (getPhase() >= 2 && getTarget() == null && tickCount % 40 == 0) {
            Player p = level().getNearestPlayer(getX(), getY(), getZ(), 64.0, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
            if (p != null) setTarget(p);
        }

        int anim = getAnim();
        if (anim == A_NONE) {
            if (getPhase() == 1 && getHealth() <= getMaxHealth() * 0.5f) startState(A_SHED, 80);
            return;
        }

        stateTicks--;
        switch (anim) {
            case A_SCREAM -> tickScream();
            case A_SHED -> tickShed();
            case A_ATTACK -> {
                if (stateTicks == 6 && pendingTarget != null && pendingTarget.isAlive()
                        && distanceToSqr(pendingTarget) < 20.0) {
                    super.doHurtTarget(pendingTarget);
                }
            }
            case A_GRAB -> tickGrab();
            case A_TEAR -> tickTear();
            default -> { }
        }
        if (stateTicks <= 0 && getAnim() == anim) endState(anim);
    }

    private void endState(int anim) {
        switch (anim) {
            case A_GRAB -> {
                if (grabbedId != null) startState(A_TEAR, 25);
                else entityData.set(ANIM, A_NONE);
            }
            case A_TEAR -> releaseGrab();
            default -> { pendingTarget = null; entityData.set(ANIM, A_NONE); }
        }
    }

    private void releaseGrab() {
        grabbedId = null;
        grabDamage = 0f;
        grabCooldown = 120;
        stateTicks = 0;
        entityData.set(ANIM, A_NONE);
    }

    // ---------- состояния ----------
    private void tickScream() {
        LivingEntity t = getTarget();
        if (t != null) faceTarget(t);
        if (stateTicks == 38) { roar(); darkenNearby(); }
    }

    /** Срыв кожи (80 тиков): голова -> руки и грудь -> ноги -> финальный рёв. */
    private void tickShed() {
        LivingEntity tg = getTarget();
        if (tg != null) faceTarget(tg);
        int t = 80 - stateTicks;
        if (t == 1) { roar(); darkenNearby(); }
        if (t >= 10 && t < 64 && t % 3 == 0) {
            double y = t < 34 ? 3.0 : (t < 48 ? 2.0 : 0.9);
            skinChunks(y);
            blood(getX(), getY() + y, getZ(), 6);
            playSound(SoundEvents.SLIME_SQUISH, 1.5f, 0.4f + random.nextFloat() * 0.2f);
        }
        if (t == 64) {
            spawnAtLocation(new ItemStack(Items.LEATHER, 3));
            spawnAtLocation(new ItemStack(Items.ROTTEN_FLESH, 4));
        }
        if (t == 76) { // кожа полностью снята - вторая фаза
            setPhase(2);
            applyPhaseStats();
            setHealth(Math.min(getMaxHealth(), getHealth() + 30f));
            blood(getX(), getY() + 1.5, getZ(), 90);
            roar();
        }
    }

    private void skinChunks(double y) {
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.LEATHER)),
                    getX(), getY() + y, getZ(), 8, 0.4, 0.3, 0.4, 0.12);
        }
    }

    private void tickGrab() {
        Player p = grabbedPlayer();
        if (p == null) { releaseGrab(); return; }
        if (!p.isAlive()) { onVictimDied(p); return; }
        holdVictim(p);
        if (stateTicks % 10 == 0) {
            p.hurt(damageSources().mobAttack(this), 2f);
            blood(p.getX(), p.getY() + 1, p.getZ(), 10);
            playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.5f, 0.5f);
        }
    }

    private void tickTear() {
        Player p = grabbedPlayer();
        if (p == null) { releaseGrab(); return; }
        if (!p.isAlive()) { onVictimDied(p); return; }
        holdVictim(p);
        if (stateTicks == 10) { // момент разрыва
            blood(p.getX(), p.getY() + 1, p.getZ(), 70);
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR,
                    SoundSource.HOSTILE, 3f, 0.5f);
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.RAVAGER_ROAR,
                    SoundSource.HOSTILE, 3f, 0.5f);
            p.hurt(damageSources().mobAttack(this), 1000f);
            if (!p.isAlive()) onVictimDied(p); else releaseGrab(); // тотем бессмертия спасает
        }
    }

    /** Игрок убит: монстр надевает его кожу. */
    private void onVictimDied(Player p) {
        blood(p.getX(), p.getY() + 1, p.getZ(), 100);
        entityData.set(VICTIM, Optional.of(p.getUUID()));
        setCustomName(p.getName());
        setCustomNameVisible(true);
        grabbedId = null;
        grabDamage = 0f;
        grabCooldown = 100;
        startState(A_SCREAM, 40);
    }

    // ---------- помощники ----------
    private Player grabbedPlayer() {
        return grabbedId == null ? null : level().getPlayerByUUID(grabbedId);
    }

    private void holdVictim(Player p) {
        faceTarget(p);
        Vec3 fwd = Vec3.directionFromRotation(0f, getYRot());
        Vec3 pos = position().add(fwd.scale(1.2)).add(0, 1.4, 0);
        AABB box = p.getBoundingBox().move(pos.subtract(p.position()));
        if (!level().noCollision(p, box)) pos = position().add(0, 1.0, 0);
        if (p instanceof ServerPlayer sp) sp.connection.teleport(pos.x, pos.y, pos.z, getYRot() + 180f, -8f);
        p.setDeltaMovement(Vec3.ZERO);
        p.fallDistance = 0f;
    }

    private void faceTarget(LivingEntity t) {
        double dx = t.getX() - getX(), dz = t.getZ() - getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90f;
        setYRot(yaw);
        yBodyRot = yaw;
        setYHeadRot(yaw);
    }

    private void roar() {
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 5f, 0.4f);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, 4f, 0.5f);
    }

    private void darkenNearby() {
        for (Player pl : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(24))) {
            pl.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false));
        }
    }

    private void blood(double x, double y, double z, int n) {
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(new DustParticleOptions(new Vector3f(0.6f, 0f, 0f), 1.6f),
                    x, y, z, n, 0.5, 0.8, 0.5, 0.05);
        }
    }

    // ---------- звуки / лут ----------
    @Override protected SoundEvent getAmbientSound() {
        return getPhase() >= 2 ? SoundEvents.WITHER_AMBIENT : SoundEvents.ZOMBIE_AMBIENT;
    }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvents.RAVAGER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.RAVAGER_DEATH; }
    @Override protected float getVoicePitch() { return 0.5f; }
    @Override protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState st) {
        playSound(SoundEvents.RAVAGER_STEP, 0.2f, 0.6f);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource src, int looting, boolean recent) {
        super.dropCustomDeathLoot(src, looting, recent);
        spawnAtLocation(new ItemStack(Items.ROTTEN_FLESH, 3 + random.nextInt(4)));
        if (getVictim().isPresent() && hasCustomName()) { // голова съеденного игрока
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            head.getOrCreateTag().putString("SkullOwner", getCustomName().getString());
            spawnAtLocation(head);
        }
    }
}
