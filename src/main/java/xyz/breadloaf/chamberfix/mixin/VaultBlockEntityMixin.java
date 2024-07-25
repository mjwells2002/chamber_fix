package xyz.breadloaf.chamberfix.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import net.minecraft.world.level.block.entity.vault.VaultSharedData;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.breadloaf.chamberfix.ResetTimestampHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static xyz.breadloaf.chamberfix.ChamberFix.CONFIG;

@Mixin(VaultBlockEntity.class)
public class VaultBlockEntityMixin extends BlockEntity implements ResetTimestampHolder {

    @Unique
    private final Map<UUID, Long> resetTimestamps = new HashMap<>();

    public VaultBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "saveAdditional", at = @At("HEAD"))
    private void injectSaveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        if (resetTimestamps != null) {
            ListTag resetTimers = new ListTag();
            for (Map.Entry<UUID, Long> entry : resetTimestamps.entrySet()) {
                CompoundTag playerEntry = new CompoundTag();
                playerEntry.putLong("time", entry.getValue());
                playerEntry.putUUID("uuid", entry.getKey());
                resetTimers.add(playerEntry);
            }
            compoundTag.put("reset_timestamps", resetTimers);
        }
    }

    @Inject(method = "loadAdditional", at = @At("HEAD"))
    private void injectLoadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        ListTag listTag = compoundTag.getList("reset_timestamps", ListTag.TAG_COMPOUND);
        resetTimestamps.clear();
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag playerEntry = listTag.getCompound(i);
            resetTimestamps.put(playerEntry.getUUID("uuid"), playerEntry.getLong("time"));
        }
    }

    @Override
    public Map<UUID, Long> chamber_fix$getResetTimestamps() {
        return resetTimestamps;
    }

    @Mixin(VaultBlockEntity.Server.class)
    public static class ServerMixin {
        @Inject(method = "tryInsertKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/vault/VaultBlockEntity$Server;unlock(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/vault/VaultConfig;Lnet/minecraft/world/level/block/entity/vault/VaultServerData;Lnet/minecraft/world/level/block/entity/vault/VaultSharedData;Ljava/util/List;)V"))
        private static void injectUnlock(ServerLevel level, BlockPos pos, BlockState blockState, VaultConfig vaultConfig, VaultServerData vaultServerData, VaultSharedData vaultSharedData, Player player, ItemStack itemStack, CallbackInfo ci) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof ResetTimestampHolder resetTimestampHolder)) {
                return;
            }
            resetTimestampHolder.chamber_fix$getResetTimestamps().put(player.getUUID(), level.getGameTime());
            VaultServerDataAccessor accessor = ((VaultServerDataAccessor) vaultServerData);
            accessor.setIsDirty(true);
        }

        @Inject(method = "tick", at = @At("HEAD"))
        private static void injectTick(ServerLevel level, BlockPos pos, BlockState blockState, VaultConfig vaultConfig, VaultServerData vaultServerData, VaultSharedData vaultSharedData, CallbackInfo ci) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof ResetTimestampHolder resetTimestampHolder)) {
                return;
            }
            Map<UUID, Long> resetTimestamps = resetTimestampHolder.chamber_fix$getResetTimestamps();
            VaultServerDataAccessor accessor = ((VaultServerDataAccessor) vaultServerData);
            boolean isDirty = false;
            Set<UUID> uuids = accessor.getRewardedPlayers();
            for (UUID uuid : uuids) {
                Long timer = resetTimestamps.get(uuid);
                if (timer == null) {
                    timer = 0L;
                    isDirty = true;
                }
                if (timer < 0L) {
                    resetTimestamps.remove(uuid);
                    isDirty = true;
                } else if (level.getGameTime() > timer + CONFIG.resetTimeTicks.get()) {
                    resetTimestamps.remove(uuid);
                    isDirty = true;
                }
            }

            if (isDirty) {
                uuids.clear();
                uuids.addAll(resetTimestamps.keySet());
                accessor.setIsDirty(true);
            }
        }
    }
}
