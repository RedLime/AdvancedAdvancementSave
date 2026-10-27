package com.redlimerl.advancementsave.mixin;

import com.redlimerl.advancementsave.AdvancedAdvancementSave;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.scores.ScoreboardSaveData;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ScoreboardSaveData.class)
public abstract class MixinScoreboardState extends SavedData {
    @Override
    public boolean isDirty() {
        return !AdvancedAdvancementSave.UPDATING_SETS.contains("scoreboard") && super.isDirty();
    }
}
