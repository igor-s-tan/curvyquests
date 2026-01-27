package com.igrock888.curvyquests.mixins;

import com.igrock888.curvyquests.CurvyQuests;
import dev.ftb.mods.ftbquests.quest.*;
import net.minecraft.nbt.*;
import org.joml.Vector4d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(Quest.class)
public abstract class MixinQuest {
    @Shadow
    public abstract boolean hasDependencies();

    @Shadow
    @Final
    private List<QuestObject> dependencies;

    @Inject(
            remap = false,
            method = "writeData",
            at = @At("TAIL")
    )
    private void onWriteData(CompoundTag nbt, CallbackInfo ci) {
        if (this.hasDependencies()) {
            CompoundTag curves = new CompoundTag();
            for (QuestObject superiorQuest: this.dependencies) {
                String superiorCode = superiorQuest.getCodeString();
                ListTag curve = new ListTag();
                if (CurvyQuests.curves.get(((Quest)(Object)this).getCodeString()) == null
                        || CurvyQuests.curves.get(((Quest) (Object) this).getCodeString()).get(superiorCode) == null) {
                    for (int i = 0; i < 4; ++i) curve.add(DoubleTag.valueOf(0d));
                    curves.put(superiorCode, curve);
                }
                else {
                    Vector4d vec = CurvyQuests.curves.get(((Quest) (Object) this).getCodeString()).get(superiorCode);
                    curve.add(DoubleTag.valueOf(vec.x));
                    curve.add(DoubleTag.valueOf(vec.y));
                    curve.add(DoubleTag.valueOf(vec.z));
                    curve.add(DoubleTag.valueOf(vec.w));
                    curves.put(superiorCode, curve);
                }
            }
            nbt.put("curves", curves);
        }
    }

    @Inject(
            remap = false,
            method = "readData",
            at = @At("TAIL")
    )
    private void onReadData(CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains("curves")) {
            CompoundTag curves = nbt.getCompound("curves");
            CurvyQuests.curves.put(((Quest)(Object)this).getCodeString(), new HashMap<>());
            for (String superior: curves.getAllKeys()) {
                ListTag curve = curves.getList(superior, 6);
                Vector4d vec = new Vector4d(
                        curve.getDouble(0),
                        curve.getDouble(1),
                        curve.getDouble(2),
                        curve.getDouble(3));
                CurvyQuests.curves.get(((Quest)(Object)this).getCodeString()).put(superior, vec);
            }
        }
    }
}
