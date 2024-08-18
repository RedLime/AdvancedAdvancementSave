package com.redlimerl.advancementsave.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redlimerl.advancementsave.AdvancedAdvancementSave;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.PlayerManager;
import net.minecraft.stat.ServerStatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {

    @WrapOperation(method = "savePlayerData", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/PlayerAdvancementTracker;save()V"))
    public void onSavePlayerAdvancement(PlayerAdvancementTracker instance, Operation<Void> original) {
        if (AdvancedAdvancementSave.UPDATING_SETS.contains("ad-" + instance.owner.getUuidAsString())) return;
        original.call(instance);
    }

    @WrapOperation(method = "savePlayerData", at = @At(value = "INVOKE", target = "Lnet/minecraft/stat/ServerStatHandler;save()V"))
    public void onSavePlayerStat(ServerStatHandler instance, Operation<Void> original) {
        try {
            UUID uuid = UUID.fromString(instance.file.getName().split("\\.")[0]);
            if (AdvancedAdvancementSave.UPDATING_SETS.contains("st-" + uuid)) return;
            original.call(instance);
        } catch (IllegalArgumentException e) {
            // maybe offline server
        }
    }
}
