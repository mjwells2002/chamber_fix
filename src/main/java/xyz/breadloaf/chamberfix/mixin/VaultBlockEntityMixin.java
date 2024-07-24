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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static xyz.breadloaf.chamberfix.ChamberFix.CONFIG;
import static xyz.breadloaf.chamberfix.ChamberFix.VAULT_RESET_TIMERS;

@Mixin(VaultBlockEntity.class)
public class VaultBlockEntityMixin extends BlockEntity {

    public VaultBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "saveAdditional", at = @At("HEAD"))
    private void injectSaveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        HashMap<UUID, Long> reset_timers = VAULT_RESET_TIMERS.get(this.worldPosition);
        if (reset_timers != null) {
            ListTag listTag = new ListTag();
            for (Map.Entry<UUID, Long> entry : reset_timers.entrySet()) {
                CompoundTag aaaa = new CompoundTag();
                aaaa.putLong("time", entry.getValue());
                aaaa.putUUID("uuid", entry.getKey());
                listTag.add(aaaa);
            }
            compoundTag.put("reset_timers", listTag);
        }

    }

    @Inject(method = "loadAdditional", at = @At("HEAD"))
    private void injectLoadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider, CallbackInfo ci) {
        ListTag listTag = compoundTag.getList("reset_timers", 10); // 10 is the compound type
        HashMap<UUID, Long> reset_timers = new HashMap<>();
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag aaaa = listTag.getCompound(i);
            reset_timers.put(aaaa.getUUID("uuid"), aaaa.getLong("time"));
        }
        VAULT_RESET_TIMERS.put(this.worldPosition, reset_timers);
    }


    @Mixin(VaultBlockEntity.Server.class)
    public static class ServerMixin {
        @Inject(method = "tryInsertKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/vault/VaultBlockEntity$Server;unlock(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/vault/VaultConfig;Lnet/minecraft/world/level/block/entity/vault/VaultServerData;Lnet/minecraft/world/level/block/entity/vault/VaultSharedData;Ljava/util/List;)V"))
        private static void injectUnlock(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, VaultConfig vaultConfig, VaultServerData vaultServerData, VaultSharedData vaultSharedData, Player player, ItemStack itemStack, CallbackInfo ci) {
            HashMap<UUID, Long> reset_timers = VAULT_RESET_TIMERS.getOrDefault(blockPos, new HashMap<>());
            reset_timers.put(player.getUUID(), serverLevel.getGameTime());
            VAULT_RESET_TIMERS.put(blockPos, reset_timers);
        }

        @Inject(method = "tick", at = @At("HEAD"))
        private static void injectTick(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, VaultConfig vaultConfig, VaultServerData vaultServerData, VaultSharedData vaultSharedData, CallbackInfo ci) {
            HashMap<UUID, Long> reset_timers = VAULT_RESET_TIMERS.getOrDefault(blockPos, new HashMap<>());
            VaultServerDataAccessor accessor = ((VaultServerDataAccessor) vaultServerData);
            boolean is_dirty = false;
            Set<UUID> uuids = accessor.getRewardedPlayers();
            for (UUID uuid : uuids) {
                Long timer = reset_timers.get(uuid);
                if (timer == null) {
                    timer = 0L;
                    is_dirty = true;
                }
                if (timer < 0L) {
                    reset_timers.remove(uuid);
                    is_dirty = true;
                } else if (serverLevel.getGameTime() > timer + CONFIG.resetTimeTicks.get()) {
                    reset_timers.remove(uuid);
                    is_dirty = true;
                }
            }
            VAULT_RESET_TIMERS.put(blockPos, reset_timers);

            if (is_dirty) {
                uuids.clear();
                uuids.addAll(reset_timers.keySet());
                accessor.setIsDirty(true);
            }
        }
    }
}
