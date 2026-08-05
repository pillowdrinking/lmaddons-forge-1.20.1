package net.pillow.lmaddons.client;

import net.miauczel.legendary_monsters.item.custom.SoulGreatSwordItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.pillow.lmaddons.LMAddons;
import net.pillow.lmaddons.config.LMAConfig;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.ListIterator;

@Mod.EventBusSubscriber(modid = LMAddons.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof SoulGreatSwordItem)) return;

        List<Component> tooltip = event.getToolTip();
        ListIterator<Component> it = tooltip.listIterator();

        while (it.hasNext()) {
            int idx = it.nextIndex();
            Component line = it.next();

            if (!(line instanceof MutableComponent c)) continue;
            if (!(c.getContents() instanceof TranslatableContents tc)) continue;

            String key = tc.getKey();

            // 替换格挡相关文本（原第3行）
            if (key.equals("item.legendary_monsters.soul_great_sword3")) {
                if (LMAConfig.PARRY_IGNORE_BYPASS_TAGS.get()) {
                    it.set(Component.translatable("tooltip.lmaddons.soul_great_sword.parry.bypass")
                            .withStyle(ChatFormatting.GRAY));
                }
            }

            // 替换幻影匕首文本（原第5行），根据配置选择文本，动态填距离
            if (key.equals("item.legendary_monsters.soul_great_sword5")) {
                int range = LMAConfig.DAGGER_AUTO_TARGET.get()
                        ? LMAConfig.DAGGER_AUTO_TARGET_RANGE.get().intValue()
                        : LMAConfig.DAGGER_RAY_RANGE.get().intValue();

                String langKey = LMAConfig.DAGGER_AUTO_TARGET.get()
                        ? "tooltip.lmaddons.soul_great_sword.dagger.auto_target"
                        : "tooltip.lmaddons.soul_great_sword.dagger.ray_range";

                it.set(Component.translatable(langKey, range)
                        .withStyle(ChatFormatting.GRAY));
            }
        }
    }

}
