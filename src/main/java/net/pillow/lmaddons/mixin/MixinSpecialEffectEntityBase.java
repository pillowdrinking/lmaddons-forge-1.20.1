package net.pillow.lmaddons.mixin;

import net.miauczel.legendary_monsters.entity.AnimatedMonster.AnimatedEntity.FallingSoulBladeEntity;
import net.miauczel.legendary_monsters.entity.AnimatedMonster.AnimatedEntity.SoulBladeEntity;
import net.miauczel.legendary_monsters.entity.AnimatedMonster.Projectile.SoulPillarEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        {
        SoulPillarEntity.class,
        SoulBladeEntity.class,
        FallingSoulBladeEntity.class
}
)
public abstract class MixinSpecialEffectEntityBase {

    @Inject(
            method = "damage",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void skipCosmeticDamage(LivingEntity ImpactEntity, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.getPersistentData().getBoolean("lmaddons:cosmetic")) {
            ci.cancel();
        }
    }
}
