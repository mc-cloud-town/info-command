package com.carpet_prometheus;

import com.carpet_prometheus.metrics.Metric;
import com.carpet_prometheus.metrics.Mobcaps;
import com.carpet_prometheus.metrics.Player;
import com.carpet_prometheus.metrics.Tick;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
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
    private int _oldInterval;

    public Prometheus() {
        registry = new PrometheusRegistry();
        JvmMetrics.builder().register(registry);
    }

    @Override
    public void run() {
        if (CarpetPrometheus.server == null || !InfoCommandSettings.prometheusEnable) return;

        for (Metric metric : this.getMetrics()) {
            try {
                metric.update(CarpetPrometheus.server);
            } catch (Exception e) {
                CarpetPrometheus.LOGGER.error("Error updating metric " + metric.getName(), e);
            }
        }
    }

    public void start() {
        this.stop();
        this.registerMetric(new Mobcaps());
        this.registerMetric(new Player());
        this.registerMetric(new Tick());

        if (metricUpdateLoop == null) metricUpdateLoop = new Timer("Metric Update Loop");
        _oldInterval = InfoCommandSettings.prometheusUpdateInterval;
        metricUpdateLoop.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (CarpetPrometheus.server == null || !InfoCommandSettings.prometheusEnable) return;

                for (Metric metric : getMetrics()) {
                    try {
                        metric.update(CarpetPrometheus.server);
                    } catch (Exception e) {
                        CarpetPrometheus.LOGGER.error("Error updating metric " + metric.getName(), e);
                    }
                }
            }
        }, 0, _oldInterval);

        try {
            httpServer = HTTPServer.builder()
                    .port(InfoCommandSettings.prometheusPort)
                    .registry(registry)
                    .buildAndStart();
        } catch (Exception e) {
            CarpetPrometheus.LOGGER.error("Start HttpServer error:", e);
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

    public @Nullable HTTPServer getHttpServer() {
        return httpServer;
    }

    public int getOldIInterval() {
        return _oldInterval;
    }
}
