package monkey.info.command;

import monkey.info.command.metrics.Metric;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class MetricUpdate extends TimerTask {
    private final List<Metric> metrics = new ArrayList<>();

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

    public void registerMetric(Metric metric) {
        this.metrics.add(metric);
    }

    public List<Metric> getMetrics() {
        return metrics;
    }
}
