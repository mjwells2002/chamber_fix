package xyz.breadloaf.chamberfix.commands;

import com.mojang.brigadier.context.CommandContext;
import de.maxhenkel.admiral.annotations.Command;
import de.maxhenkel.admiral.annotations.Name;
import de.maxhenkel.admiral.annotations.RequiresPermissionLevel;
import de.maxhenkel.admiral.arguments.Players;
import de.maxhenkel.admiral.arguments.Time;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import xyz.breadloaf.chamberfix.ChamberFix;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import static xyz.breadloaf.chamberfix.ChamberFix.VAULT_RESET_TIMERS;

@RequiresPermissionLevel(4)
@Command("chamberfix")
public class AdminCommands {

    @RequiresPermissionLevel(4)
    @Command("set_reset_time")
    public void setResetTime(CommandContext<CommandSourceStack> context, @Name("reset_time") Time time) {
        ChamberFix.CONFIG.resetTimeTicks.set(Long.valueOf(time.get())).save();
        context.getSource().sendSuccess(() -> Component.literal("Vault reset time set to %s ticks".formatted(time.get())), false);
    }

    @RequiresPermissionLevel(4)
    @Command("reset")
    public int resetPlayer(CommandContext<CommandSourceStack> context, @Name("vault_location") BlockPos pos, @Name("targets") Players players) {
        HashMap<UUID, Long> reset_timer = VAULT_RESET_TIMERS.computeIfAbsent(pos, p -> new HashMap<>());
        int playersReset = (int) players.stream().map(player -> reset_timer.remove(player.getUUID())).filter(Objects::nonNull).count();
        context.getSource().sendSuccess(() -> Component.literal("Cooldown reset for %s player(s)".formatted(playersReset)), false);
        return playersReset;
    }

}
