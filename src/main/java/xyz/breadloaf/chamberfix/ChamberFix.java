package xyz.breadloaf.chamberfix;

import de.maxhenkel.admiral.MinecraftAdmiral;
import de.maxhenkel.configbuilder.ConfigBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import xyz.breadloaf.chamberfix.commands.AdminCommands;
import xyz.breadloaf.chamberfix.config.Config;

import java.nio.file.Path;

public class ChamberFix implements ModInitializer {

    public static Config CONFIG;

    @Override
    public void onInitialize() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("ChamberFix");
        CONFIG = ConfigBuilder.builder(Config::new).path(configPath.resolve("chamber_fix.properties")).build();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            MinecraftAdmiral.builder(dispatcher, registryAccess).addCommandClasses(
                    AdminCommands.class
            ).build();
        });
    }
}
