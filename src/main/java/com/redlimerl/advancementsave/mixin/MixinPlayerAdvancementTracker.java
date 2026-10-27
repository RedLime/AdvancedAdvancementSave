package com.redlimerl.advancementsave.mixin;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.redlimerl.advancementsave.AdvancedAdvancementSave;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FileUtil;
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

@Mixin(PlayerAdvancements.class)
public abstract class MixinPlayerAdvancementTracker {

    @Shadow public ServerPlayer player;
    @Final @Shadow private Codec<PlayerAdvancements.Data> codec;
    @Final @Shadow private Path playerSavePath;
    @Shadow protected abstract PlayerAdvancements.Data asData();

    @Inject(method = "markForVisibilityUpdate", at = @At("RETURN"))
    public void onUpdateStatus(CallbackInfo ci) {
        final JsonElement jsonElement = this.codec.encodeStart(JsonOps.INSTANCE, this.asData()).getOrThrow();
        AdvancedAdvancementSave.UPDATED_ADVANCEMENT_PLAYER_MAP.put(this.player.getUUID(), () -> {
            AdvancedAdvancementSave.UPDATING_SETS.add("ad-" + this.player.getStringUUID());
            try {
                FileUtil.createDirectoriesSafe(this.playerSavePath.getParent());
                Writer writer = Files.newBufferedWriter(this.playerSavePath, StandardCharsets.UTF_8);
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
            AdvancedAdvancementSave.UPDATING_SETS.remove("ad-" + this.player.getStringUUID());
        });
    }
}
