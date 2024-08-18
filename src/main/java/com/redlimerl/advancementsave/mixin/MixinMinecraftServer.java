package com.redlimerl.advancementsave.mixin;

import com.redlimerl.advancementsave.AdvancedAdvancementSave;
import net.minecraft.command.DataCommandStorage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.scoreboard.ScoreboardState;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {

    @Shadow public abstract ServerScoreboard getScoreboard();

    @Shadow public abstract PlayerManager getPlayerManager();

    @Shadow public abstract DataCommandStorage getDataCommandStorage();

    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At("RETURN"))
    public void onTick(CallbackInfo ci) {
        if (!AdvancedAdvancementSave.UPDATED_PLAYER_MAP.isEmpty()) {
            boolean updated = false;
            for (ServerPlayerEntity serverPlayer : this.getPlayerManager().getPlayerList()) {
                if (AdvancedAdvancementSave.UPDATED_PLAYER_MAP.containsKey(serverPlayer.getUuid())) {
                    AdvancedAdvancementSave.THREAD_EXECUTOR.submit(AdvancedAdvancementSave.UPDATED_PLAYER_MAP.get(serverPlayer.getUuid()));
                    AdvancedAdvancementSave.UPDATED_PLAYER_MAP.remove(serverPlayer.getUuid());
                    updated = true;
                }
            }
            if (updated) {
                PersistentStateManager stateManager = this.getDataCommandStorage().stateManager;
                ScoreboardState scoreboardState = stateManager.getOrCreate(this.getScoreboard().getPersistentStateType(), "scoreboard");

                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.put("data", scoreboardState.writeNbt(new NbtCompound(), stateManager.registryLookup));
                NbtHelper.putDataVersion(nbtCompound);

                AdvancedAdvancementSave.THREAD_EXECUTOR.submit(() -> {
                    AdvancedAdvancementSave.UPDATING_SETS.add("scoreboard");
                    try {
                        NbtIo.writeCompressed(nbtCompound, stateManager.getFile("scoreboard").toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    AdvancedAdvancementSave.UPDATING_SETS.remove("scoreboard");
                });
            }
        }
    }
}
