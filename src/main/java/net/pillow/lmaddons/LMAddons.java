package net.pillow.lmaddons;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.pillow.lmaddons.config.LMAConfig;
import org.slf4j.Logger;

@Mod(LMAddons.MOD_ID)
@Mod.EventBusSubscriber(modid = LMAddons.MOD_ID)
public class LMAddons {
   public static final String MOD_ID = "lm_addons";
   private static final Logger LOGGER = LogUtils.getLogger();

   public LMAddons(FMLJavaModLoadingContext context)
   {
      MinecraftForge.EVENT_BUS.register(this);
      IEventBus bus = context.getModEventBus();
      bus.addListener(this::commonSetup);
      bus.addListener(this::addCreative);

      ModLoadingContext.get().registerConfig(
              ModConfig.Type.COMMON,
              LMAConfig.COMMON_SPEC,
              "lmaddons-common.toml");
   }

   private void commonSetup(final FMLCommonSetupEvent event) {}

   private void addCreative(BuildCreativeModeTabContentsEvent event) {}

   @SubscribeEvent
   public void onServerStarting(ServerStartingEvent event)
   {
      LOGGER.info("HELLO from server starting");
   }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
   public static class ClientModEvents
   {
      @SubscribeEvent
      public static void onClientSetup(FMLClientSetupEvent event)
      {
         LOGGER.info("HELLO FROM CLIENT SETUP");
         LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
      }
   }

}

