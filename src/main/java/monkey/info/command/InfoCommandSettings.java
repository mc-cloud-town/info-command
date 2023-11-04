package monkey.info.command;

import carpet.settings.ParsedRule;
import carpet.settings.Rule;
import carpet.settings.Validator;
import carpet.utils.Messenger;
import net.minecraft.server.command.ServerCommandSource;

public class InfoCommandSettings {
    public static final String INFO_COMMAND = "info-command";
    @Rule(
            category = {INFO_COMMAND},
            desc = "enable prometheus server"
    )
    public static boolean prometheusEnable = false;
    @Rule(
            options = {"9060", "9061", "9062", "9063"},
            category = {INFO_COMMAND},
            desc = "prometheus server port",
            validate = ValidatePrometheusPort.class
    )
    public static int prometheusPort = 9060;
    @Rule(
            category = {INFO_COMMAND},
            desc = "prometheus update interval",
            validate = ValidatePrometheusPort.class
    )
    public static int prometheusUpdateInterval = 1000;

    public static class ValidatePrometheusPort extends Validator<Integer> {
        @Override
        public Integer validate(ServerCommandSource source, ParsedRule<Integer> currentRule, Integer newValue, String string) {
            return newValue >= 0 && newValue <= 65535 ? newValue : null;
        }

        @Override
        public void notifyFailure(ServerCommandSource source, ParsedRule<Integer> currentRule, String providedValue) {
            Messenger.m(source, "r Wrong value for " + currentRule.name + ": " + providedValue + ", should be 0-65535");
        }
    }
}
