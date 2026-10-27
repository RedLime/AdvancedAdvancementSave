package com.redlimerl.advancementsave.mixin;

import com.redlimerl.advancementsave.AdvancedAdvancementSave;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.minecraft.world.scores.ScoreboardSaveData;
import org.apache.commons.io.FileUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.charset.Charset;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {


    @Unique private int statUpdateTick = 0;


    @Shadow public abstract ServerScoreboard getScoreboard();

    @Shadow public abstract PlayerList getPlayerList();

    @Shadow public abstract SavedDataStorage getDataStorage();

    @Inject(method = "tickServer(Ljava/util/function/BooleanSupplier;)V", at = @At("RETURN"))
    public void onTick(CallbackInfo ci) {
        boolean shouldUpdateScoreboard = false;
        if (!AdvancedAdvancementSave.UPDATED_ADVANCEMENT_PLAYER_MAP.isEmpty()) {
            for (ServerPlayer serverPlayer : this.getPlayerList().getPlayers()) {
                if (AdvancedAdvancementSave.UPDATED_ADVANCEMENT_PLAYER_MAP.containsKey(serverPlayer.getUUID())) {
                    AdvancedAdvancementSave.THREAD_EXECUTOR.submit(AdvancedAdvancementSave.UPDATED_ADVANCEMENT_PLAYER_MAP.get(serverPlayer.getUUID()));
                    AdvancedAdvancementSave.UPDATED_ADVANCEMENT_PLAYER_MAP.remove(serverPlayer.getUUID());
                    this.pushPlayerStatUpdate(serverPlayer);
                    shouldUpdateScoreboard = true;
                }
            }
        }

        if (this.statUpdateTick++ >= 20 * 60) {
            this.statUpdateTick = 0;

            for (ServerPlayer serverPlayer : this.getPlayerList().getPlayers()) {
                this.pushPlayerStatUpdate(serverPlayer);
            }
            shouldUpdateScoreboard = true;
        }

        if (!AdvancedAdvancementSave.UPDATED_STAT_PLAYER_MAP.isEmpty()) {
            for (ServerPlayer serverPlayer : this.getPlayerList().getPlayers()) {
                if (AdvancedAdvancementSave.UPDATED_STAT_PLAYER_MAP.containsKey(serverPlayer.getUUID())) {
                    AdvancedAdvancementSave.THREAD_EXECUTOR.submit(AdvancedAdvancementSave.UPDATED_STAT_PLAYER_MAP.get(serverPlayer.getUUID()));
                    AdvancedAdvancementSave.UPDATED_STAT_PLAYER_MAP.remove(serverPlayer.getUUID());
                    shouldUpdateScoreboard = true;
                }
            }
        }

        if (shouldUpdateScoreboard) {
            SavedDataStorage stateManager = this.getDataStorage();
            ScoreboardSaveData scoreboardState = stateManager.computeIfAbsent(ScoreboardSaveData.TYPE);
            this.getScoreboard().storeToSaveDataIfDirty(scoreboardState);

            CompoundTag nbtCompound = stateManager.encodeUnchecked(ScoreboardSaveData.TYPE, scoreboardState, stateManager.registries.createSerializationContext(NbtOps.INSTANCE));

            AdvancedAdvancementSave.THREAD_EXECUTOR.submit(() -> {
                AdvancedAdvancementSave.UPDATING_SETS.add("scoreboard");
                try {
                    NbtIo.writeCompressed(nbtCompound, stateManager.getDataFile(ScoreboardSaveData.TYPE.id()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                AdvancedAdvancementSave.UPDATING_SETS.remove("scoreboard");
            });
        }
    }

    @Unique
    private void pushPlayerStatUpdate(final ServerPlayer player) {
        final String statData = AdvancedAdvancementSave.GSON.toJson(player.getStats().toJson());
        AdvancedAdvancementSave.UPDATED_STAT_PLAYER_MAP.put(player.getUUID(), () -> {
            AdvancedAdvancementSave.UPDATING_SETS.add("st-" + player.getStringUUID());
            try {
                FileUtils.writeStringToFile(player.getStats().file.toFile(), statData, Charset.defaultCharset());
            } catch (IOException e) {
                e.printStackTrace();
            }
            AdvancedAdvancementSave.UPDATING_SETS.remove("st-" + player.getStringUUID());
        });
    }
}
