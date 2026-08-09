package net.pillow.lmaddons.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Monster;

public class LMAUtil {

    public static boolean isHostile(LivingEntity target) {
        return target instanceof Monster
                || target.getType().getCategory() == MobCategory.MONSTER;
    }
}
