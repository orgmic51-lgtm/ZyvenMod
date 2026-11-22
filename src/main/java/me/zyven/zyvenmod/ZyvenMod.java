package me.zyven.zyvenmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ZyvenMod implements ClientModInitializer {
    public static final ProfitTracker PROFIT = new ProfitTracker();
    public static boolean showProfit = true;
    public static boolean showFps = true;
    public static boolean fakeItemsEnabled = false;
    public static boolean viewmodelEnabled = false;
    public static boolean customSkyEnabled = false;
    public static boolean hatsEnabled = false;
    public static boolean crosshairStyleEnabled = false;
    public static boolean cleanHudEnabled = false;
    private static KeyBinding modulesKey;
    private static KeyBinding auctionKey;

    @Override
    public void onInitializeClient() {
        modulesKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zyvenmod.modules", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
                new KeyBinding.Category(Identifier.of("zyvenmod", "keys"))));
        auctionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zyvenmod.auction", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O,
                new KeyBinding.Category(Identifier.of("zyvenmod", "keys"))));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (modulesKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new ModulesScreen());
                else if (client.currentScreen instanceof ModulesScreen) client.setScreen(null);
            }
            while (auctionKey.wasPressed()) {
                if (client.currentScreen == null) {
                    if (client.player != null) client.player.networkHandler.sendChatCommand("ah");
                    else client.setScreen(new AuctionScreen());
                } else if (client.currentScreen instanceof AuctionScreen) client.setScreen(null);
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) ->
                PROFIT.readIncoming(message.getString()));
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            PROFIT.readOutgoing(message);
            return true;
        });


    }

    public static final class ProfitTracker {
        private static final Pattern AMOUNT = Pattern.compile("(?i)([+-]?\\d+(?:[,.]\\d+)?\\s*[kmb]?)");
        private double total;

        public void readIncoming(String message) {
            String lower = message.toLowerCase();
            if (!(lower.contains("received") || lower.contains("paid you")
                    || lower.contains("sent you") || lower.contains("has given you"))) return;
            Matcher matcher = AMOUNT.matcher(message);
            if (matcher.find()) total += parse(matcher.group(1));
        }

        public void readOutgoing(String message) {
            String trimmed = message.trim();
            if (!trimmed.toLowerCase().startsWith("/pay ")) return;
            Matcher matcher = AMOUNT.matcher(trimmed.substring(5));
            if (matcher.find()) total -= Math.abs(parse(matcher.group(1)));
        }

        private double parse(String raw) {
            String s = raw.toLowerCase().replace(",", "").replaceAll("\\s+", "");
            double multiplier = 1;
            if (s.endsWith("k")) { multiplier = 1_000; s = s.substring(0, s.length()-1); }
            else if (s.endsWith("m")) { multiplier = 1_000_000; s = s.substring(0, s.length()-1); }
            else if (s.endsWith("b")) { multiplier = 1_000_000_000; s = s.substring(0, s.length()-1); }
            try { return Double.parseDouble(s) * multiplier; }
            catch (NumberFormatException ignored) { return 0; }
        }

        public double value() { return total; }
        public String formatted() {
            double abs = Math.abs(total);
            String sign = total > 0 ? "+" : total < 0 ? "-" : "";
            if (abs >= 1_000_000_000) return sign + shortNumber(abs / 1_000_000_000) + "B";
            if (abs >= 1_000_000) return sign + shortNumber(abs / 1_000_000) + "M";
            if (abs >= 1_000) return sign + shortNumber(abs / 1_000) + "K";
            return sign + String.format(java.util.Locale.US, "%.0f", abs);
        }
        private String shortNumber(double n) {
            return String.format(java.util.Locale.US, n >= 100 ? "%.0f" : n >= 10 ? "%.1f" : "%.2f", n)
                    .replaceAll("\\.0+$", "").replaceAll("(\\.\\d*[1-9])0+$", "$1");
        }
    }
}
