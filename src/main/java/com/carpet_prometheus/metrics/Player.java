package com.carpet_prometheus.metrics;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.List;

public class Player extends Metric {
    public Player() {
        super("players_online", "Online players", "type", "world");
    }

    @Override
    public void update(MinecraftServer server) {
        // edit from: https://github.com/gnembon/fabric-carpet/blob/7486670918f733aaf6ae3b595290947802024d02/src/main/java/carpet/utils/SpawnReporter.java#L105
        for (ServerWorld world : server.getWorlds()) {
            List<ServerPlayerEntity> players = world.getPlayers();
            String worldName = world.getRegistryKey().getValue().toString();

            long fakePlayers = players.stream().filter(player -> player instanceof EntityPlayerMPFake).count();
            long realPlayers = players.size() - fakePlayers;

            this.getGauge().labelValues("FAKE", worldName).set(fakePlayers);
            this.getGauge().labelValues("REAL", worldName).set(realPlayers);
        }
    }
}
