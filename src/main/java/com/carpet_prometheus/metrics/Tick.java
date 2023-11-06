package com.carpet_prometheus.metrics;

import carpet.helpers.TickSpeed;
import carpet.utils.Messenger;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.MathHelper;

import java.util.Locale;

import static net.minecraft.server.command.CommandManager.literal;

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

    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("tps").executes((c) -> {
                    double MSPT = getMSPT(c.getSource().getServer());
                    String color = Messenger.heatmap_color(MSPT, TickSpeed.mspt);

                    Messenger.m(
                            c.getSource(),
                            "g TPS: ", String.format(Locale.US, "%s %.1f", color, getTPS(MSPT)),
                            "g  MSPT: ", String.format(Locale.US, "%s %.1f", color, MSPT)
                    );
                    return (int) TickSpeed.tickrate;
                })
        );
    }


    @Override
    public void update(MinecraftServer server) {
        double mspt = getMSPT(server);

        this.getGauge().labelValues("mspt").set(mspt);
        this.getGauge().labelValues("tps").set(getTPS(mspt));
    }
}
