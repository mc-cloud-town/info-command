package monkey.info.command;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import monkey.info.command.metrics.Mobcaps;
import monkey.info.command.metrics.Tick;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Timer;

public class InfoCommand implements CarpetExtension, ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("info-command");
    public static MinecraftServer server;
    private final Timer metricUpdateLoop;
    private HTTPServer httpServer;

    public InfoCommand() {
        metricUpdateLoop = new Timer("Metric Update Loop");

    }

    @Override
    public void onInitialize() {
        CarpetServer.manageExtension(new InfoCommand());
        ServerLifecycleEvents.SERVER_STARTING.register(server -> InfoCommand.server = server);
    }

//    @Override
//    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
//    }

    @Override
    public void onServerLoaded(MinecraftServer server) {
        JvmMetrics.builder().register();

        MetricUpdate metricUpdater = new MetricUpdate();
        metricUpdater.registerMetric(new Tick());
        metricUpdater.registerMetric(new Mobcaps());

        this.getMetricUpdateLoop().schedule(metricUpdater, 0, InfoCommandSettings.prometheusUpdateInterval);

        try {
            HTTPServer httpServer = HTTPServer.builder()
                    .port(InfoCommandSettings.prometheusPort)
                    .buildAndStart();

            this.setHttpServer(httpServer);
        } catch (Exception e) {
            LOGGER.error("Start HttpServer error:", e);
        }
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(InfoCommandSettings.class);
    }

    @Override
    public void onServerClosed(MinecraftServer server) {
        this.getHttpServer().close();
        this.getMetricUpdateLoop().cancel();
    }


    public Timer getMetricUpdateLoop() {
        return metricUpdateLoop;
    }

    public HTTPServer getHttpServer() {
        return httpServer;
    }

    public void setHttpServer(HTTPServer httpServer) {
        this.httpServer = httpServer;
    }
}