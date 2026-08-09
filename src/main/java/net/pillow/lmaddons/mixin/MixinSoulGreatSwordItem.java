package net.pillow.lmaddons.mixin;

import net.miauczel.legendary_monsters.entity.AnimatedMonster.Projectile.ThrownPhantomDaggerEntity;
import net.miauczel.legendary_monsters.entity.ModEntities;
import net.miauczel.legendary_monsters.item.custom.SoulGreatSwordItem;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.pillow.lmaddons.config.LMAConfig;
import net.pillow.lmaddons.util.LMAUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;

@Mixin(value = SoulGreatSwordItem.class, priority = 500)
public abstract class MixinSoulGreatSwordItem {

    @Shadow
    public boolean parrySucced;

    @Unique
    private boolean lmaddons$cooldownHandled = false;

    @Shadow
    public double endPosX;
    @Shadow
    public double endPosY;
    @Shadow
    public double endPosZ;

    @Unique
    private int lmaddons$parryTimeUsed = -1;

    // 格挡窗口时长(原3tick)
    @Inject(method = "maxUseDuration", at = @At("RETURN"), cancellable = true, remap = false)
    private void modifyMaxUseDuration(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(LMAConfig.PARRY_WINDOW_TICKS.get());
    }

    // 弹反成功冷却(原70tick)
    @Inject(method = "getCooldown", at = @At("RETURN"), cancellable = true, remap = false)
    private void modifyGetCooldown(CallbackInfoReturnable<Integer> cir) {
        int perfectWin = LMAConfig.PARRY_PERFECT_WINDOW_TICKS.get();
        if (perfectWin > 0 && lmaddons$parryTimeUsed >= 0 && lmaddons$parryTimeUsed <= perfectWin) {
            cir.setReturnValue(LMAConfig.PARRY_PERFECT_COOLDOWN_TICKS.get());
        } else {
            cir.setReturnValue(LMAConfig.PARRY_SUCCESS_COOLDOWN_TICKS.get());
        }
        this.lmaddons$parryTimeUsed = -1;
    }

    // 追踪幻影匕首默认追踪距离
    @Inject(method = "calculateEndPos", at = @At("TAIL"), remap = false)
    private void recalculateEndPos(Player player, CallbackInfo ci) {
        double yawValue = player.yHeadRot + 90.0F;
        double pitchValue = -player.getXRot();
        double yawRad = Math.toRadians(yawValue);
        double pitchRad = Math.toRadians(pitchValue);
        double r = LMAConfig.DAGGER_RAY_RANGE.get();
        this.endPosX = player.getX() + r * Math.cos(yawRad) * Math.cos(pitchRad);
        this.endPosZ = player.getZ() + r * Math.sin(yawRad) * Math.cos(pitchRad);
        this.endPosY = player.getY() + r * Math.sin(pitchRad);
    }

    @ModifyArg(
            method = "releaseUsing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/miauczel/legendary_monsters/item/custom/SoulGreatSwordItem;spreadDaggers(Lnet/minecraft/world/entity/player/Player;I)V"
            ),
            index = 1,
            remap = false
    )
    private int modifyDaggerCount(int count) {
        return LMAConfig.DAGGER_RAY_COUNT.get();
    }

    @Inject(method = "spreadDaggers", at = @At("HEAD"), cancellable = true, remap = false)
    private void injectSpreadDaggers(Player player, int count, CallbackInfo ci) {
        if (LMAConfig.DAGGER_AUTO_TARGET.get()) {
            ci.cancel();
            autoSpreadDaggers(player, LMAConfig.DAGGER_AUTO_TARGET_COUNT.get());
        }
    }

    @Unique
    private void autoSpreadDaggers(Player player, int count) {
        Level world = player.level();
        double range = LMAConfig.DAGGER_AUTO_TARGET_RANGE.get();

        AABB box = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = world.getEntitiesOfClass(
                LivingEntity.class, box,
                target -> target != player
                        && target.isAlive()
                        && !(target instanceof TamableAnimal && ((TamableAnimal) target).getOwner() == player)
                        && !(target instanceof Player p && (p.isCreative() || p.isSpectator()))
                        && !target.isAlliedTo(player)
                        && (!LMAConfig.DAGGER_ONLY_HOSTILE.get() || LMAUtil.isHostile(target))
        );
        targets.sort(Comparator.comparingDouble(player::distanceToSqr));

        for (int i = 0; i < count; i++) {
            float throwAngle = (float) i * (float) Math.PI / (float) (count / 2);
            double sx = player.getX() + Mth.cos(throwAngle);
            double sy = player.getY() + (double) player.getBbHeight() * 0.2;
            double sz = player.getZ() + Mth.sin(throwAngle);

            double vx = Mth.cos(throwAngle);
            double vz = Mth.sin(throwAngle);

            ThrownPhantomDaggerEntity projectile = new ThrownPhantomDaggerEntity(
                    ModEntities.THROWN_PHANTOM_DAGGER.get(), world
            );

            if (!targets.isEmpty()) {
                projectile.setReturnEntity(targets.get(i % targets.size()));
            }

            projectile.setOwner(player);
            projectile.setDamage(8.0F);
            projectile.setReturnTick(10);
            projectile.moveTo(sx, sy, sz, (float) i * 11.25F, player.getXRot());
            projectile.shoot(vx, 0.0F, vz, 0.7F, 1.0F);
            world.addFreshEntity(projectile);
        }
    }

    // 弹反失败冷却(原40tick)
    @ModifyArg(
            method = "onUseTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V",
                    ordinal = 0
            ),
            index = 1
    )
    private int modifyOnUseTickFailureCooldown(int ticks) {
        if (!parrySucced && ticks == 20) {
            return LMAConfig.PARRY_FAILURE_COOLDOWN_TICKS.get();
        }
        return ticks;
    }

    @Inject(method = "onUseTick", at = @At("HEAD"), remap = false)
    private void loadParryTimeUsed(Level level, LivingEntity entity, ItemStack stack,
                                   int remainingUseDuration, CallbackInfo ci) {
        loadParryTimeUsedFromData(entity);
    }

    @Inject(method = "releaseUsing", at = @At("HEAD"), remap = false)
    private void loadParryTimeUsedRelease(ItemStack pStack, Level level, LivingEntity pLivingEntity,
                                          int pTimeCharged, CallbackInfo ci) {
        loadParryTimeUsedFromData(pLivingEntity);
    }

    @ModifyArg(
            method = "releaseUsing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V",
                    ordinal = 0
            ),
            index = 1
    )
    private int modifyReleaseFailureCooldown(int ticks) {
        if (!parrySucced && ticks == 20) {
            return LMAConfig.PARRY_FAILURE_COOLDOWN_TICKS.get();
        }
        return ticks;
    }

    @Redirect(
            method = "releaseUsing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V"
            )
    )
    private void redirectReleaseCooldown(ItemCooldowns instance, Item pItem, int pTicks) {
        boolean shouldSkip = lmaddons$cooldownHandled && pTicks != 120;
        lmaddons$cooldownHandled = false;
        if (shouldSkip) return;
        instance.addCooldown(pItem, pTicks);
    }

    @Inject(
            method = "onUseTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;stopUsingItem()V",
                    shift = At.Shift.AFTER
            )
    )
    private void markCooldownHandled(Level level, LivingEntity entity, ItemStack stack,
                                     int remainingUseDuration, CallbackInfo ci) {
        lmaddons$cooldownHandled = true;
    }

    @Unique
    private void loadParryTimeUsedFromData(LivingEntity le) {
        if (le instanceof Player p) {
            var tag = p.getPersistentData();
            if (tag.contains("lmaddons:parry_time_used")) {
                this.lmaddons$parryTimeUsed = tag.getInt("lmaddons:parry_time_used");
                tag.remove("lmaddons:parry_time_used");
            }
        }
    }
}
