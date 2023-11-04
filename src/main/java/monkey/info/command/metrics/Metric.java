package monkey.info.command.metrics;

import io.prometheus.metrics.core.metrics.Gauge;
import net.minecraft.server.MinecraftServer;

public abstract class Metric {
    private final String name;
    private final Gauge gauge;

    public Metric(String name, String help, String... labels) {
        this.name = name;
        this.gauge = Gauge.builder().name("minecraft_" + name).help(help).labelNames(labels).build();
    }

    public abstract void update(MinecraftServer server);

    public Gauge getGauge() {
        return gauge;
    }

    public String getName() {
        return name;
    }
}
