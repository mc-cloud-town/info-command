package monkey.info.command.metrics;

import carpet.helpers.TickSpeed;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;


public class Tick extends Metric {
    public Tick() {
        super("tick", "server mspt and tps", "type");
    }

    public static double getMSPT(MinecraftServer server) {
        return MathHelper.average(server.lastTickLengths) * 1.0E-6D;
    }

    public static double getTPS(double MSPT) {
        return 1000.0D / Math.max(TickSpeed.time_warp_start_time != 0 ? 0.0 : TickSpeed.mspt, MSPT);
    }

    @Override
    public void update(MinecraftServer server) {
        double mspt = getMSPT(server);

        this.getGauge().labelValues("mspt").set(mspt);
        this.getGauge().labelValues("tps").set(getTPS(mspt));
    }
}
