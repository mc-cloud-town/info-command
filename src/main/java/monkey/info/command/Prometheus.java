package monkey.info.command;

import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import monkey.info.command.metrics.Metric;
import monkey.info.command.metrics.Mobcaps;
import monkey.info.command.metrics.Tick;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Prometheus extends TimerTask {
    private final List<Metric> metrics = new ArrayList<>();
    private final PrometheusRegistry registry;
    private @Nullable Timer metricUpdateLoop;
    private @Nullable HTTPServer httpServer;

    public Prometheus() {
        registry = new PrometheusRegistry();
        JvmMetrics.builder().register(registry);
    }

    @Override
    public void run() {
        if (InfoCommand.server == null || !InfoCommandSettings.prometheusEnable) return;

        for (Metric metric : this.getMetrics()) {
            try {
                metric.update(InfoCommand.server);
            } catch (Exception e) {
                InfoCommand.LOGGER.error("Error updating metric " + metric.getName(), e);
            }
        }
    }

    public void start() {
        this.stop();
        this.registerMetric(new Tick());
        this.registerMetric(new Mobcaps());

        if (metricUpdateLoop == null) metricUpdateLoop = new Timer("Metric Update Loop");
        metricUpdateLoop.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (InfoCommand.server == null || !InfoCommandSettings.prometheusEnable) return;

                for (Metric metric : getMetrics()) {
                    try {
                        metric.update(InfoCommand.server);
                    } catch (Exception e) {
                        InfoCommand.LOGGER.error("Error updating metric " + metric.getName(), e);
                    }
                }
            }
        }, 0, InfoCommandSettings.prometheusUpdateInterval);

        try {
            httpServer = HTTPServer.builder()
                    .port(InfoCommandSettings.prometheusPort)
                    .registry(registry)
                    .buildAndStart();
        } catch (Exception e) {
            InfoCommand.LOGGER.error("Start HttpServer error:", e);
        }
    }

    public void stop() {
        for (Metric metrics : this.metrics) {
            registry.unregister(metrics.getGauge());
        }
        this.metrics.clear();
        if (httpServer != null) {
            httpServer.close();
            httpServer = null;
        }
        if (metricUpdateLoop != null) {
            try {
                metricUpdateLoop.cancel();
            } catch (Exception ignored) {
            }
        }
        metricUpdateLoop = null;
    }

    public void registerMetric(Metric metric) {
        this.metrics.add(metric);
        registry.register(metric.getGauge());
    }

    public List<Metric> getMetrics() {
        return metrics;
    }
}
