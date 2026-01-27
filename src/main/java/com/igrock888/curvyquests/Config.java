package com.igrock888.curvyquests;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = CurvyQuests.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue SAMPLING_RATE = BUILDER
            .comment("Curve sampling rate (Higher the number, smoother the curve)")
            .defineInRange("sampling_rate", 25, 5, 100);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int samplingRate;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        samplingRate = SAMPLING_RATE.get();
    }
}
