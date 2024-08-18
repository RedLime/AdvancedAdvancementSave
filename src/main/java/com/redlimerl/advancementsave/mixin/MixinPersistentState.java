package com.redlimerl.advancementsave.mixin;

import com.redlimerl.advancementsave.AdvancedAdvancementSave;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.ScoreboardState;
import net.minecraft.world.PersistentState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(PersistentState.class)
public class MixinPersistentState {

    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    public void onSave(File file, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        if (((Object) this) instanceof ScoreboardState) {
            if (AdvancedAdvancementSave.UPDATING_SETS.contains("scoreboard")) ci.cancel();
        }
    }
}
