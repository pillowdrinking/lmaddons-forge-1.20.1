package net.pillow.lmaddons.mixin;

import net.miauczel.legendary_monsters.entity.AnimatedMonster.Projectile.ThrownPhantomDaggerEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.pillow.lmaddons.config.LMAConfig;
import net.pillow.lmaddons.util.LMAUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ThrownPhantomDaggerEntity.class)
public abstract class MixinThrownPhantomDaggerEntity {

    @Shadow
    protected abstract float getReturnTick();

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;subtract(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private Vec3 redirectSubtract(Vec3 returnPos, Vec3 pVec) {
        ThrownPhantomDaggerEntity self = (ThrownPhantomDaggerEntity) (Object) this;

        if (!(self.getOwner() instanceof Player)) {
            return returnPos.subtract(pVec);
        }

        LivingEntity target = self.returnEntity();
        if (target != null) {
            double ratio = LMAConfig.DAGGER_AIM_HEIGHT_RATIO.get();
            double newY = target.getY() + target.getBbHeight() * ratio;
            returnPos = new Vec3(returnPos.x, newY, returnPos.z);
        }

        Vec3 vec3 = returnPos.subtract(pVec);

        double strength = LMAConfig.DAGGER_VERTICAL_TRACKING_STRENGTH.get();
        if (strength != 1.0) {
            vec3 = new Vec3(vec3.x, vec3.y * strength, vec3.z);
        }
        return vec3;
    }

    @ModifyVariable(
            method = "tick",
            at = @At(value = "STORE", ordinal = 0),
            name = "finalTick",
            remap = false
    )
    private double modifyFinalTick(double finalTick) {
        ThrownPhantomDaggerEntity self = (ThrownPhantomDaggerEntity) (Object) this;
        if (self.getOwner() instanceof Player) {
            int customDuration = LMAConfig.DAGGER_TRACKING_DURATION_TICKS.get();
            return (double) (customDuration - self.getLessLifeTicks()) + getReturnTick();
        }
        return finalTick;
    }

    @ModifyVariable(
            method = "tick",
            at = @At(value = "STORE"),
            name = "d0",
            remap = false
    )
    private double modifyD0(double d0) {
        ThrownPhantomDaggerEntity self = (ThrownPhantomDaggerEntity) (Object) this;
        if (self.getOwner() instanceof Player) {
            return d0 * LMAConfig.DAGGER_HORIZONTAL_TRACKING_STRENGTH.get();
        }
        return d0;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void retargetIfNeeded(CallbackInfo ci) {
        ThrownPhantomDaggerEntity self = (ThrownPhantomDaggerEntity) (Object) this;
        if (self.level().isClientSide) return;
        if (self.tickCount % 5 != 0) return;
        if (!(self.getOwner() instanceof Player p)) return;
        if (!LMAConfig.DAGGER_AUTO_TARGET.get()) return;

        LivingEntity current = self.returnEntity();
        if (current != null && current.isAlive()) return;

        double range = LMAConfig.DAGGER_AUTO_TARGET_RANGE.get();
        AABB box = p.getBoundingBox().inflate(range);
        List<LivingEntity> candidates = self.level().getEntitiesOfClass(
                LivingEntity.class, box,
                target -> target != p
                        && target.isAlive()
                        && !(target instanceof TamableAnimal pet && pet.getOwner() == p)
                        && !(target instanceof Player player && (player.isCreative() || player.isSpectator()))
                        && !target.isAlliedTo(p)
                        && (!LMAConfig.DAGGER_ONLY_HOSTILE.get() || LMAUtil.isHostile(target))
        );
        if (candidates.isEmpty()) return;

        LivingEntity nearest = null;
        double nearestDistSq = range * range;
        for (LivingEntity e : candidates) {
            double distSq = self.distanceToSqr(e);
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = e;
            }
        }
        if (nearest != null) {
            self.setReturnEntity(nearest);
        }
    }

}