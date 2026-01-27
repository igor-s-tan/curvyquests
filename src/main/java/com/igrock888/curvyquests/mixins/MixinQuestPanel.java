package com.igrock888.curvyquests.mixins;

import com.igrock888.curvyquests.Config;
import com.igrock888.curvyquests.CurvyQuests;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import dev.ftb.mods.ftblibrary.math.MathUtils;
import dev.ftb.mods.ftblibrary.ui.Widget;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestPanel;
import dev.ftb.mods.ftbquests.quest.Quest;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.joml.Vector4d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(QuestPanel.class)
public abstract class MixinQuestPanel {
    @Inject(
            remap = false,
            method = "renderConnection",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRenderConnection(Widget widget, QuestButton button, PoseStack poseStack, BufferBuilder buffer, float s, int r, int g, int b, int a, int a1, float mu, Tesselator tesselator, CallbackInfo ci) {
        double sx = (double)widget.getX() + (double)widget.width / (double)2.0F;
        double sy = (double)widget.getY() + (double)widget.height / (double)2.0F;
        double ex = (double)button.getX() + (double)button.width / (double)2.0F;
        double ey = (double)button.getY() + (double)button.height / (double)2.0F;

        float len = (float) MathUtils.dist(sx, sy, ex, ey);
        float effLen;

        poseStack.pushPose();
        poseStack.translate(sx, sy, 0.0);

        Quest superior = ((IMixinQuestButtonAccessor) button).curvyquests$getQuest();
        Quest dependant = ((IMixinQuestButtonAccessor) widget).curvyquests$getQuest();

        HashMap<String, Vector4d> curveTable = CurvyQuests.curves.get(dependant.getCodeString());

        Vector4d vec = null;
        if (curveTable != null) {
            vec = curveTable.get(superior.getCodeString());
        }
        if (vec == null) {
            vec = new Vector4d(0, 0, 0, 0);
        }

        poseStack.mulPose(Axis.ZP.rotation((float)Math.atan2(ey - sy, ex - sx)));
        Matrix4f m = poseStack.last().pose();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        Pair<Float, Float> begin = Pair.of(0f, 0f);
        Pair<Float, Float> end = Pair.of(len, 0f);
        Pair<Float, Float> bezierA = Pair.of(len * (float)vec.x, -s * (float)vec.z);
        Pair<Float, Float> bezierB = Pair.of(len * (float)vec.y, -s * (float)vec.w);

        buffer
            .vertex(m, 0, -s, 0.0F)
            .color(255, 255, 255, 1)
            .uv(0, 1)
            .endVertex();

        buffer
            .vertex(m, 0, s, 0.0F)
            .color(255, 255, 255, 1)
            .uv(0, mu)
            .endVertex();

        Pair<Float, Float> last = Pair.of(0f, 0f);

        for (int i = 1; i <= Config.samplingRate + 1; ++i) {
            float step = (float) i / Config.samplingRate;
            Pair<Float, Float> copy = getBezier(begin, end, bezierA, bezierB, step);
            Pair<Float, Float> newPoint = subPairs(copy, last);

            effLen = (float) Math.hypot(newPoint.getLeft(), newPoint.getRight());

            float alpha = (float) Math.atan2(newPoint.getRight(), newPoint.getLeft());

            Pair<Float, Float> first = CartesianToPolar(Pair.of(0f, s));
            first = Pair.of(first.getLeft(), first.getRight() + alpha);
            first = PolarToCartesian(first);

            Pair<Float, Float> second = CartesianToPolar(Pair.of(0f, -s));
            second = Pair.of(second.getLeft(), second.getRight() + alpha);
            second = PolarToCartesian(second);

            buffer
                .vertex(m, last.getLeft() + first.getLeft(), last.getRight() + first.getRight(), 0.0F)
                .color(r, g, b, a)
                .uv(-effLen / s / 2.0f + mu, 1)
                .endVertex();

            buffer
                .vertex(m, last.getLeft() + second.getLeft(), last.getRight() + second.getRight(), 0.0F)
                .color(r, g, b, a)
                .uv(-effLen / s / 2.0f + mu, 0)
                .endVertex();

            buffer
                .vertex(m, last.getLeft() + second.getLeft(), last.getRight() + second.getRight(), 0.0F)
                .color(r, g, b, a)
                .uv(mu, 0)
                .endVertex();

            buffer
                .vertex(m, last.getLeft() + first.getLeft(), last.getRight() + first.getRight(), 0.0F)
                .color(r, g, b, a)
                .uv(mu, 1)
                .endVertex();

            last = copy;
        }

        tesselator.end();
        poseStack.popPose();
        ci.cancel();
    }


    private Pair<Float, Float> getBezier(
            Pair<Float, Float> begin,
            Pair<Float, Float> end,
            Pair<Float, Float> bezierA,
            Pair<Float, Float> bezierB,
            float t)
    {
        Pair<Float, Float> _a = multPairByNumber(begin, (1 - t) * (1 - t) * (1 - t));
        Pair<Float, Float> _b = multPairByNumber(bezierA, (1 - t) * (1 - t) * t * 3);
        Pair<Float, Float> _c = multPairByNumber(bezierB, (1 - t) * t * t * 3);
        Pair<Float, Float> _d = multPairByNumber(end, t * t * t);

        return addPairs(_a, addPairs(_b, addPairs(_c, _d)));
    }

    private Pair<Float, Float> multPairByNumber(Pair<Float, Float> a, float b) {
        return Pair.of(a.getLeft() * b, a.getRight() * b);
    }

    private Pair<Float, Float> addPairs(Pair<Float, Float> a, Pair<Float, Float> b) {
        return Pair.of(a.getLeft() + b.getLeft(), a.getRight() + b.getRight());
    }

    private Pair<Float, Float> subPairs(Pair<Float, Float> a, Pair<Float, Float> b) {
        return Pair.of(a.getLeft() - b.getLeft(), a.getRight() - b.getRight());
    }

    private Pair<Float, Float> CartesianToPolar(Pair<Float, Float> cartesian) {
        return Pair.of((float) Math.hypot(cartesian.getLeft(), cartesian.getRight()), (float) Math.atan2(cartesian.getRight(), cartesian.getLeft()));
    }

    private Pair<Float, Float> PolarToCartesian(Pair<Float, Float> polar) {
        return Pair.of(polar.getLeft() * (float) Math.cos(polar.getRight()), polar.getLeft() * (float) Math.sin(polar.getRight()));
    }
}
