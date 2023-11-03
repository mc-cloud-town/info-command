package monkey.info.command;

import carpet.helpers.TickSpeed;
import carpet.utils.Messenger;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

import static net.minecraft.server.command.CommandManager.literal;

public class InfoCommand implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("infocommand");

    public static double getMSPT(MinecraftServer server) {
        return MathHelper.average(server.lastTickLengths) * 1.0E-6D;
    }

    public static double getTPS(double MSPT) {
        return 1000.0D / Math.max(TickSpeed.time_warp_start_time != 0 ? 0.0 : TickSpeed.mspt, MSPT);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment environment) {
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
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");
    }
}