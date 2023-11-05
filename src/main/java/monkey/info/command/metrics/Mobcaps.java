package monkey.info.command.metrics;

import carpet.utils.SpawnReporter;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;

public class Mobcaps extends Metric {
    public Mobcaps() {
        super("mobcaps", "minecraft mobcaps data", "type", "world");
    }

    @Override
    public void update(MinecraftServer server) {
        // edit from: https://github.com/gnembon/fabric-carpet/blob/7486670918f733aaf6ae3b595290947802024d02/src/main/java/carpet/utils/SpawnReporter.java#L105
        for (ServerWorld world : server.getWorlds()) {
            SpawnHelper.Info lastSpawner = world.getChunkManager().getSpawnInfo();

            if (lastSpawner != null) {
                RegistryKey<World> dim = world.getRegistryKey();
                String worldName = world.getRegistryKey().getValue().toString();
                int chunkcount = SpawnReporter.chunkCounts.getOrDefault(dim, -1);

                for (SpawnGroup enumcreaturetype : SpawnGroup.values()) {
                    Object2IntMap<SpawnGroup> dimCounts = lastSpawner.getGroupToCount();
                    String enumcreature = enumcreaturetype.toString();

                    int cur = dimCounts.getOrDefault(enumcreaturetype, -1);
                    int max = (int) (chunkcount * ((double) enumcreaturetype.getCapacity() / SpawnReporter.currentMagicNumber()));

                    this.getGauge().labelValues(enumcreature, worldName).set(cur);
                    this.getGauge().labelValues(enumcreature + "_MAX", worldName).set(max);
                }
            }
        }
    }
}
