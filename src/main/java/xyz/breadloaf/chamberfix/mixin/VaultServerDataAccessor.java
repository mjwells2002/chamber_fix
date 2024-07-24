package xyz.breadloaf.chamberfix.mixin;

import net.minecraft.world.level.block.entity.vault.VaultServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;
import java.util.UUID;

@Mixin(VaultServerData.class)
public interface VaultServerDataAccessor {
    @Accessor("isDirty")
    void setIsDirty(boolean isDirty);

    @Accessor("rewardedPlayers")
    Set<UUID> getRewardedPlayers();

}
