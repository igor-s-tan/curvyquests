package com.igrock888.curvyquests.mixins;

import dev.ftb.mods.ftbquests.client.gui.quests.QuestButton;
import dev.ftb.mods.ftbquests.quest.Quest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(QuestButton.class)
public interface IMixinQuestButtonAccessor {
    @Accessor("quest")
    Quest curvyquests$getQuest();
}
