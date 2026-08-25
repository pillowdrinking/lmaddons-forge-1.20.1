package net.pillow.lmaddons.event;

import net.miauczel.legendary_monsters.Particle.ModParticles;
import net.miauczel.legendary_monsters.effect.ModEffects;
import net.miauczel.legendary_monsters.entity.AnimatedMonster.AnimatedEntity.FallingSoulBladeEntity;
import net.miauczel.legendary_monsters.entity.AnimatedMonster.AnimatedEntity.SoulBladeEntity;
import net.miauczel.legendary_monsters.entity.AnimatedMonster.IAnimatedBoss.PossessedPaladin.PossessedPaladinEntity;
import net.miauczel.legendary_monsters.entity.AnimatedMonster.Projectile.SoulPillarEntity;
import net.miauczel.legendary_monsters.entity.AnimatedMonster.Projectile.ThrownPhantomDaggerEntity;
import net.miauczel.legendary_monsters.item.ModItems;
import net.miauczel.legendary_monsters.util.EntityUtil;
import net.miauczel.legendary_monsters.util.MathUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.pillow.lmaddons.LMAddons;
import net.pillow.lmaddons.config.LMAConfig;
import net.pillow.lmaddons.util.LMAUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = LMAddons.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvents {

    private static final Map<Integer, DaggerKillRecord> lmaddons$daggerKillCandidate = new ConcurrentHashMap<>();
    private static final Map<Integer, PendingDaggerHit> lmaddons$pendingDaggerHit = new ConcurrentHashMap<>();

    private record DaggerKillRecord(ThrownPhantomDaggerEntity dagger, long tick) {}
    private record PendingDaggerHit(ThrownPhantomDaggerEntity dagger, long tick) {}

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        Level world = target.level();
        DamageSource source = event.getSource();
        if (world.isClientSide) return;

        PendingDaggerHit pending = lmaddons$pendingDaggerHit.remove(target.getId());
        if (pending != null && world.getGameTime() - pending.tick() <= 0) {
            lmaddons$handleDaggerHit(pending.dagger(), target, event.getAmount());
            return;
        }

        LivingEntity attacker = findSoulGreatSwordAttacker(source);
        if (attacker == null) return;
        if (attacker instanceof PossessedPaladinEntity) return;
        if (target == attacker) return;

        int durationTicks = MathUtils.toTicks(LMAConfig.SOUL_FRACTURE_DURATION_SECONDS.get().floatValue());
        int maxLevel = LMAConfig.SOUL_FRACTURE_MAX_LEVEL.get();

        EntityUtil.applyStackingEffect(
                target,
                ModEffects.SOUL_FRACTURE.get(),
                1,
                maxLevel - 1,
                durationTicks
        );
    }

    @Nullable
    private static LivingEntity findSoulGreatSwordAttacker(DamageSource source) {
        Entity attackerE = source.getEntity();
        Entity attackerDE = source.getDirectEntity();

        if (attackerE == attackerDE && attackerE instanceof LivingEntity le) {
            if (le instanceof Player p) {
                if (p.getMainHandItem().getItem() == ModItems.SOUL_GREAT_SWORD.get()) {
                    return p;
                }
            } else {
                if (isHoldingSoulGreatSword(le)) {
                    return le;
                }
            }
        }
        return null;
    }

    private static boolean isHoldingSoulGreatSword(LivingEntity le) {
        ItemStack mainHand = le.getMainHandItem();
        ItemStack offHand  = le.getOffhandItem();
        return mainHand.getItem() == ModItems.SOUL_GREAT_SWORD.get()
                || offHand.getItem() == ModItems.SOUL_GREAT_SWORD.get();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingAttackParryInvul(LivingAttackEvent event) {
        LivingEntity le = event.getEntity();
        if (le.level().isClientSide) return;

        long endTick = le.getPersistentData().getLong("lmaddons:parry_invul_end");
        if (endTick != 0) {
            if (le.tickCount >= endTick) {
                le.getPersistentData().remove("lmaddons:parry_invul_end");
            } else if (!isDamageTypeBypass(event.getSource())) {
                event.setCanceled(true);
            }
        }
    }

    private static boolean isDamageTypeBypass(DamageSource source) {
        // 配置列表
        List<? extends String> bypassTypes = LMAConfig.PARRY_INVUL_BYPASS_DAMAGE_TYPES.get();
        for (String typeStr : bypassTypes) {
            ResourceLocation rl = ResourceLocation.tryParse(typeStr);
            if (rl != null) {
                ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE, rl);
                if (source.typeHolder().is(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player p) {
            p.getPersistentData().remove("lmaddons:parry_invul_end");
        }
    }

    /**
     * 玩家死亡后重生（Clone 事件）时，清除从旧实体复制过来的无敌帧标记。
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {  // 只处理死亡后重生
            event.getEntity().getPersistentData().remove("lmaddons:parry_invul_end");
        }
    }

    @SubscribeEvent
    public static void onDaggerProjectileImpact(ProjectileImpactEvent event) {
        Entity projectile = event.getEntity();
        if (projectile.level().isClientSide) return;
        if (!(projectile instanceof ThrownPhantomDaggerEntity dagger)) return;
        if (!(event.getRayTraceResult() instanceof EntityHitResult entityHit)) return;
        if (!(entityHit.getEntity() instanceof LivingEntity target)) return;
        if (!(dagger.getOwner() instanceof Player p)) return;

        if (LMAConfig.DAGGER_AUTO_TARGET.get()) {
            if (LMAUtil.shouldIgnoreTarget(target, p)) {
                event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
                return;
            }

            if (LMAUtil.isExcluded(target)) {
                event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
                return;
            }

            if (LMAConfig.DAGGER_ONLY_HOSTILE.get() && !LMAUtil.isHostile(target)) {
                event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
                return;
            }
        }

        lmaddons$pendingDaggerHit.put(target.getId(),
                new PendingDaggerHit(dagger, target.level().getGameTime()));
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onDaggerHitParticle(LivingAttackEvent event) {
        LivingEntity target = event.getEntity();
        Level world = target.level();
        if (world.isClientSide) return;
        if (lmaddons$pendingDaggerHit.get(target.getId()) == null) return;
        if (!(world instanceof ServerLevel sl)) return;

        double centerX = target.getX();
        double centerY = target.getY() + target.getBbHeight() * 0.5;
        double centerZ = target.getZ();
        double convergeSpeed = 0.6;

        for (int i = 0; i < 4; i++) {
            double angle = Math.toRadians(i * 90.0 + world.random.nextDouble() * 20);
            double radius = 0.9;
            double px = centerX + Math.cos(angle) * radius;
            double pz = centerZ + Math.sin(angle) * radius;
            double py = target.getY() + target.getBbHeight() * world.random.nextDouble();

            Vec3 spawnPos = new Vec3(px, py, pz);
            Vec3 targetPos = new Vec3(centerX, centerY, centerZ);
            Vec3 vf = targetPos.subtract(spawnPos);
            Vec3 v = vf.scale(convergeSpeed);

            sl.sendParticles(ModParticles.GHOSTLY_SOUL.get(), px, py, pz, 0, v.x, v.y, v.z, 1.0);
        }
    }

    private static void lmaddons$handleDaggerHit(ThrownPhantomDaggerEntity dagger, LivingEntity target, float actualDamage) {
        if (!(dagger.getOwner() instanceof Player p)) return;
        if (target instanceof TamableAnimal pet && pet.getOwner() == p) return;

        int durationTicks = MathUtils.toTicks(LMAConfig.SOUL_FRACTURE_DURATION_SECONDS.get().floatValue());
        int maxLevel = LMAConfig.SOUL_FRACTURE_MAX_LEVEL.get();
        EntityUtil.applyStackingEffect(target, ModEffects.SOUL_FRACTURE.get(), 1, maxLevel - 1, durationTicks);

        if (LMAConfig.DAGGER_LIFESTEAL_ENABLED.get()) {
            double maxHP = target.getMaxHealth();
            float healAmount = (float) (
                    actualDamage * LMAConfig.DAGGER_LIFESTEAL_FLAT_RATIO.get()
                            + maxHP * LMAConfig.DAGGER_LIFESTEAL_MAXHP_RATIO.get()
            );
            p.heal(healAmount);
        }

        lmaddons$daggerKillCandidate.put(target.getId(),
                new DaggerKillRecord(dagger, target.level().getGameTime()));
    }

    @SubscribeEvent
    public static void onDaggerKillEffect(LivingDeathEvent event) {
        LivingEntity target = event.getEntity();
        DaggerKillRecord record = lmaddons$daggerKillCandidate.remove(target.getId());
        if (record == null) return;

        if (target.level().getGameTime() - record.tick() != 0) return;

        if (!(record.dagger().getOwner() instanceof Player p)) return;
        lmaddons$spawnDaggerKillEffect(target, p);
    }

    private static void lmaddons$spawnDaggerKillEffect(LivingEntity target, Player owner) {
        Level world = target.level();
        double centerX = target.getX();
        double centerY = target.getY() + target.getBbHeight() * 0.5;
        double centerZ = target.getZ();

        if (world instanceof ServerLevel sl) {
            double burstSpeed = 0.5;
            int count = 20;
            for (int i = 0; i < count; i++) {
                // 球面均匀采样一个随机方向
                double theta = world.random.nextDouble() * Math.PI * 2;
                double phi = Math.acos(2 * world.random.nextDouble() - 1);
                double vx = Math.sin(phi) * Math.cos(theta) * burstSpeed;
                double vy = Math.cos(phi) * burstSpeed;
                double vz = Math.sin(phi) * Math.sin(theta) * burstSpeed;

                sl.sendParticles(ModParticles.GHOSTLY_SOUL.get(), centerX, centerY, centerZ, 0, vx, vy, vz, 1.0);
            }
        }

        // 随机选择一种特效实体
        RandomSource rand = world.random;
        Entity specialEffect = switch (rand.nextInt(3)) {
            case 0 -> new SoulBladeEntity(world, centerX, target.getY(), centerZ, 0, 10, owner, 0.0F, false);
            case 1 -> new SoulPillarEntity(world, centerX, target.getY(), centerZ, 0, 10, owner, 20, 0.0F, false);
            default -> new FallingSoulBladeEntity(world, centerX, target.getY(), centerZ, 0, 10, owner, 0.0F, false);
        };

        specialEffect.getPersistentData().putBoolean("lmaddons:cosmetic", true);
        world.addFreshEntity(specialEffect);
    }
}