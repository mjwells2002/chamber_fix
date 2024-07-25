package xyz.breadloaf.chamberfix;

import de.maxhenkel.admiral.MinecraftAdmiral;
import de.maxhenkel.configbuilder.ConfigBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import xyz.breadloaf.chamberfix.commands.AdminCommands;
import xyz.breadloaf.chamberfix.config.Config;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

public class ChamberFix implements ModInitializer {
    public static Path configPath;
    public static Config CONFIG;

    public static HashMap<BlockPos, HashMap<UUID, Long>> VAULT_RESET_TIMERS = new HashMap<>();

    @Override
    public void onInitialize() {
        configPath = FabricLoader.getInstance().getConfigDir().resolve("ChamberFix");
        CONFIG = ConfigBuilder.builder(Config::new).path(configPath.resolve("chamber_fix.properties")).build();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            MinecraftAdmiral.builder(dispatcher, registryAccess).addCommandClasses(
                    AdminCommands.class
            ).build();
        });
    }
}
