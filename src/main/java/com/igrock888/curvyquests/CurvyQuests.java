package com.igrock888.curvyquests;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.joml.Vector4d;

import java.util.HashMap;

@Mod(CurvyQuests.MODID)
public class CurvyQuests
{
    public static final String MODID = "curvyquests";

    public static final HashMap<String, HashMap<String, Vector4d>> curves = new HashMap<>();

    public CurvyQuests(FMLJavaModLoadingContext context)
    {
        MinecraftForge.EVENT_BUS.register(this);
        context.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }
}
