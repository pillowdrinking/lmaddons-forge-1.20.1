package net.pillow.lmaddons.mixin;

import net.miauczel.legendary_monsters.effect.ModEffects;
import net.miauczel.legendary_monsters.event.ForgeEvents;
import net.miauczel.legendary_monsters.item.custom.SoulGreatSwordItem;
import net.miauczel.legendary_monsters.util.MathUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.pillow.lmaddons.config.LMAConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgeEvents.class)
public abstract class MixinForgeEvents {

    @Redirect(
            method = "onLivingHurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z"
            )
    )
    private static boolean redirectBypassCheck(DamageSource source, TagKey<DamageType> pDamageTypeKey) {
        if (LMAConfig.PARRY_IGNORE_BYPASS_TAGS.get()) {
            if (pDamageTypeKey == DamageTypeTags.BYPASSES_INVULNERABILITY
                    || pDamageTypeKey == DamageTypeTags.BYPASSES_ARMOR) {
                return false;
            }
        }
        return source.is(pDamageTypeKey);
    }


    @Inject(method = "onLivingHurt", at = @At("TAIL"), remap = false)
    private static void onParrySuccessImmediate(LivingAttackEvent event, CallbackInfo ci) {
        if (!event.isCanceled()) return;
        if (!(event.getEntity() instanceof Player p)) return;

        ItemStack useItem = p.getUseItem();
        if (!(useItem.getItem() instanceof SoulGreatSwordItem sword)) return;
        if (!sword.parrySucced) return;

        int invulTicks = LMAConfig.PARRY_INVUL_TICKS.get();
        if (invulTicks > 0) {
            p.getPersistentData().putLong("lmaddons:parry_invul_end",
                    p.tickCount + invulTicks);
        }

        int perfectWin = LMAConfig.PARRY_PERFECT_WINDOW_TICKS.get();
        boolean isPerfect = perfectWin > 0 && sword.timeUsed <= perfectWin;

        if (isPerfect) {
            for (String entry : LMAConfig.PARRY_PERFECT_EFFECTS.get()) {
                String[] parts = entry.split(";");
                if (parts.length != 3) continue;

                ResourceLocation rl = ResourceLocation.tryParse(parts[0]);
                if (rl == null) continue;

                MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(rl);
                if (effect != null) {
                    int durationTicks = MathUtils.toTicks(Float.parseFloat(parts[1]));
                    int maxLevel = Integer.parseInt(parts[2]) - 1;
                    p.addEffect(new MobEffectInstance(effect, durationTicks, maxLevel, false, true));
                }
            }
            p.getPersistentData().putInt("lmaddons:parry_time_used", sword.timeUsed);
        }
        int durationTicks = MathUtils.toTicks(
                LMAConfig.PARRY_SOUL_RAGE_DURATION_SECONDS.get().floatValue());
        int maxLevel = LMAConfig.PARRY_SOUL_RAGE_MAX_LEVEL.get() - 1;
        p.addEffect(new MobEffectInstance(
                ModEffects.SOUL_RAGE.get(),
                durationTicks,
                maxLevel,
                false,
                true));

    }
}
