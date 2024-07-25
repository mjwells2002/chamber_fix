package xyz.breadloaf.chamberfix.commands;

import de.maxhenkel.admiral.annotations.Command;
import de.maxhenkel.admiral.annotations.Name;
import de.maxhenkel.admiral.annotations.RequiresPermissionLevel;
import de.maxhenkel.admiral.arguments.Players;
import de.maxhenkel.admiral.arguments.Time;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import xyz.breadloaf.chamberfix.ChamberFix;

import java.util.HashMap;
import java.util.UUID;

@RequiresPermissionLevel(4)
@Command("chamberfix")
public class AdminCommands {

    @RequiresPermissionLevel(4)
    @Command("set_reset_time")
    public void setResetTime(@Name("reset_time") Time time) {
        ChamberFix.CONFIG.resetTimeTicks.set(Long.valueOf(time.get())).save();
    }

    @RequiresPermissionLevel(4)
    @Command("reset")
    public void resetPlayer(@Name("vault_location") BlockPos pos, @Name("targets") Players players) {
        for (Player player : players) {
            HashMap<UUID,Long> reset_timer = ChamberFix.VAULT_RESET_TIMERS.get(pos);
            if (reset_timer != null) {
                reset_timer.put(player.getUUID(), -1L);
            }
        }
    }

}
