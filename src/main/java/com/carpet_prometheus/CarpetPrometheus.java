package com.carpet_prometheus;


import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.settings.ParsedRule;
import com.carpet_prometheus.metrics.Tick;
import com.carpet_prometheus.utils.CarpetExtraTranslations;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.Map;

public class CarpetPrometheus implements CarpetExtension, ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("info-command");
    public static MinecraftServer server;
    public final Prometheus prometheus;

    public CarpetPrometheus() {
        prometheus = new Prometheus();
    }

    @Override
    public void onInitialize() {
        CarpetServer.manageExtension(this);
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        Tick.registerCommand(dispatcher);
    }

    @Override
    public void onServerLoaded(MinecraftServer server) {
        CarpetPrometheus.server = server;
        this.prometheus.start();
    }

    @Override
    public void onServerClosed(MinecraftServer server) {
        this.prometheus.stop();
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(InfoCommandSettings.class);
        CarpetServer.settingsManager.addRuleObserver(new OnConfigChange(this));
    }


    @Override
    public Map<String, String> canHasTranslations(String lang) {
        return CarpetExtraTranslations.getTranslationFromResourcePath(lang);
    }

    private record OnConfigChange(
            CarpetPrometheus infoCommand
    ) implements TriConsumer<ServerCommandSource, ParsedRule<?>, String> {

        @Override
        public void accept(ServerCommandSource serverCommandSource, ParsedRule<?> parsedRule, String s) {
            if (!parsedRule.categories.contains(InfoCommandSettings.INFO_COMMAND)) {
                return;
            }

            String name = parsedRule.name;
            if (name.equals("prometheusEnable") && !parsedRule.getBoolValue()) {
                infoCommand.prometheus.stop();
            } else if (name.startsWith("prometheus")) {
                infoCommand.prometheus.start(); // reload prometheus server
            }
        }
    }
}