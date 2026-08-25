package net.pillow.lmaddons.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import net.pillow.lmaddons.config.LMAConfig;

import java.util.List;

public class LMAUtil {

    public static boolean isHostile(LivingEntity target) {
        return target instanceof Monster
                || target.getType().getCategory() == MobCategory.MONSTER;
    }

    public static boolean shouldIgnoreTarget(LivingEntity target, LivingEntity owner) {
        if (target == owner) return true;
        if (!target.isAlive()) return true;
        if (target instanceof Player p && (p.isCreative() || p.isSpectator())) return true;
        if (target instanceof TamableAnimal pet && pet.getOwner() == owner) return true;
        return target.isAlliedTo(owner);
    }

    public static boolean isExcluded(LivingEntity target) {
        List<? extends String> excluded = LMAConfig.DAGGER_EXCLUDED_ENTITIES.get();
        for (String entry : excluded) {
            ResourceLocation rl = ResourceLocation.tryParse(entry);
            if (rl == null) continue;
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(rl);
            if (type != null && target.getType() == type) {
                return true;
            }
        }
        return false;
    }
}
