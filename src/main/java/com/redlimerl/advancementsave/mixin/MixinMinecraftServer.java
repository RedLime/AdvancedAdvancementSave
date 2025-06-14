package com.redlimerl.advancementsave.mixin;

import com.redlimerl.advancementsave.AdvancedAdvancementSave;
import net.minecraft.command.DataCommandStorage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.scoreboard.ScoreboardState;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
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

    @Shadow public abstract PlayerManager getPlayerManager();

    @Shadow public abstract DataCommandStorage getDataCommandStorage();

    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At("RETURN"))
    public void onTick(CallbackInfo ci) {
        boolean shouldUpdateScoreboard = false;
        if (!AdvancedAdvancementSave.UPDATED_ADVANCEMENT_PLAYER_MAP.isEmpty()) {
            for (ServerPlayerEntity serverPlayer : this.getPlayerManager().getPlayerList()) {
                if (AdvancedAdvancementSave.UPDATED_ADVANCEMENT_PLAYER_MAP.containsKey(serverPlayer.getUuid())) {
                    AdvancedAdvancementSave.THREAD_EXECUTOR.submit(AdvancedAdvancementSave.UPDATED_ADVANCEMENT_PLAYER_MAP.get(serverPlayer.getUuid()));
                    AdvancedAdvancementSave.UPDATED_ADVANCEMENT_PLAYER_MAP.remove(serverPlayer.getUuid());
                    this.pushPlayerStatUpdate(serverPlayer);
                    shouldUpdateScoreboard = true;
                }
            }
        }

        if (this.statUpdateTick++ >= 20 * 60) {
            this.statUpdateTick = 0;

            for (ServerPlayerEntity serverPlayer : this.getPlayerManager().getPlayerList()) {
                this.pushPlayerStatUpdate(serverPlayer);
            }
            shouldUpdateScoreboard = true;
        }

        if (!AdvancedAdvancementSave.UPDATED_STAT_PLAYER_MAP.isEmpty()) {
            for (ServerPlayerEntity serverPlayer : this.getPlayerManager().getPlayerList()) {
                if (AdvancedAdvancementSave.UPDATED_STAT_PLAYER_MAP.containsKey(serverPlayer.getUuid())) {
                    AdvancedAdvancementSave.THREAD_EXECUTOR.submit(AdvancedAdvancementSave.UPDATED_STAT_PLAYER_MAP.get(serverPlayer.getUuid()));
                    AdvancedAdvancementSave.UPDATED_STAT_PLAYER_MAP.remove(serverPlayer.getUuid());
                    shouldUpdateScoreboard = true;
                }
            }
        }

        if (shouldUpdateScoreboard) {
            PersistentStateManager stateManager = this.getDataCommandStorage().stateManager;
            ScoreboardState scoreboardState = stateManager.getOrCreate(ServerScoreboard.STATE_TYPE);

            NbtCompound nbtCompound = stateManager.encode(ServerScoreboard.STATE_TYPE, scoreboardState, stateManager.registries.getOps(NbtOps.INSTANCE));

            AdvancedAdvancementSave.THREAD_EXECUTOR.submit(() -> {
                AdvancedAdvancementSave.UPDATING_SETS.add("scoreboard");
                try {
                    NbtIo.writeCompressed(nbtCompound, stateManager.getFile(ServerScoreboard.STATE_TYPE.id()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                AdvancedAdvancementSave.UPDATING_SETS.remove("scoreboard");
            });
        }
    }

    @Unique
    private void pushPlayerStatUpdate(final ServerPlayerEntity player) {
        final String statData = player.getStatHandler().asString();
        AdvancedAdvancementSave.UPDATED_STAT_PLAYER_MAP.put(player.getUuid(), () -> {
            AdvancedAdvancementSave.UPDATING_SETS.add("st-" + player.getUuidAsString());
            try {
                FileUtils.writeStringToFile(player.getStatHandler().file, statData, Charset.defaultCharset());
            } catch (IOException e) {
                e.printStackTrace();
            }
            AdvancedAdvancementSave.UPDATING_SETS.remove("st-" + player.getUuidAsString());
        });
    }
}
