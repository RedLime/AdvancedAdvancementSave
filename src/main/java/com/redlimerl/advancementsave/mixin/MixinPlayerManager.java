package com.redlimerl.advancementsave.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redlimerl.advancementsave.AdvancedAdvancementSave;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {

    @WrapOperation(method = "savePlayerData", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/PlayerAdvancementTracker;save()V"))
    public void onSavePlayerAdvancement(PlayerAdvancementTracker instance, Operation<Void> original) {
        if (AdvancedAdvancementSave.UPDATING_SETS.contains(instance.owner.getUuidAsString())) return;
        original.call(instance);
    }
}
