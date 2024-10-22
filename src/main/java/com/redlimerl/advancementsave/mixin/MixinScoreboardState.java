package com.redlimerl.advancementsave.mixin;

import com.redlimerl.advancementsave.AdvancedAdvancementSave;
import net.minecraft.scoreboard.ScoreboardState;
import net.minecraft.world.PersistentState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ScoreboardState.class)
public abstract class MixinScoreboardState extends PersistentState {
    @Override
    public boolean isDirty() {
        return !AdvancedAdvancementSave.UPDATING_SETS.contains("scoreboard") && super.isDirty();
    }
}
