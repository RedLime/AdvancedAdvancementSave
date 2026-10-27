package com.redlimerl.advancementsave.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redlimerl.advancementsave.AdvancedAdvancementSave;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.ServerStatsCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

@Mixin(PlayerList.class)
public class MixinPlayerManager {

    @WrapOperation(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerAdvancements;save()V"))
    public void onSavePlayerAdvancement(PlayerAdvancements instance, Operation<Void> original) {
        if (AdvancedAdvancementSave.UPDATING_SETS.contains("ad-" + instance.player.getStringUUID())) return;
        original.call(instance);
    }

    @WrapOperation(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/ServerStatsCounter;save()V"))
    public void onSavePlayerStat(ServerStatsCounter instance, Operation<Void> original) {
        try {
            UUID uuid = UUID.fromString(instance.file.getFileName().toString().split("\\.")[0]);
            if (AdvancedAdvancementSave.UPDATING_SETS.contains("st-" + uuid)) return;
            original.call(instance);
        } catch (IllegalArgumentException e) {
            // maybe offline server
        }
    }
}
