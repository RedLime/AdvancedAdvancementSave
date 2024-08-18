package com.redlimerl.advancementsave.mixin;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.redlimerl.advancementsave.AdvancedAdvancementSave;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PathUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(PlayerAdvancementTracker.class)
public abstract class MixinPlayerAdvancementTracker {

    @Shadow public ServerPlayerEntity owner;
    @Final @Shadow private Codec<PlayerAdvancementTracker.ProgressMap> progressMapCodec;
    @Final @Shadow private Path filePath;
    @Shadow protected abstract PlayerAdvancementTracker.ProgressMap createProgressMap();

    @Inject(method = "onStatusUpdate", at = @At("RETURN"))
    public void onUpdateStatus(CallbackInfo ci) {
        JsonElement jsonElement = this.progressMapCodec.encodeStart(JsonOps.INSTANCE, this.createProgressMap()).getOrThrow();
        AdvancedAdvancementSave.UPDATED_PLAYER_MAP.put(this.owner.getUuid(), () -> {
            AdvancedAdvancementSave.UPDATING_SETS.add(this.owner.getUuidAsString());
            try {
                PathUtil.createDirectories(this.filePath.getParent());
                Writer writer = Files.newBufferedWriter(this.filePath, StandardCharsets.UTF_8);
                try {
                    AdvancedAdvancementSave.GSON.toJson(jsonElement, AdvancedAdvancementSave.GSON.newJsonWriter(writer));
                } catch (Throwable var6) {
                    try {
                        writer.close();
                    } catch (Throwable var5) {
                        var6.addSuppressed(var5);
                    }
                    throw var6;
                }

                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            AdvancedAdvancementSave.UPDATING_SETS.remove(this.owner.getUuidAsString());
        });
    }
}
