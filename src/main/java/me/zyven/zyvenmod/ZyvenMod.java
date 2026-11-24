package me.zyven.zyvenmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ZyvenMod implements ClientModInitializer {
    public static final ProfitTracker PROFIT = new ProfitTracker();

    public static boolean showProfit = true;
    public static boolean showFps = true;
    public static boolean hudBackground = true;
    public static boolean fakeItemsEnabled = false;
    public static boolean viewmodelEnabled = false;
    public static boolean customSkyEnabled = false;
    public static boolean hatsEnabled = false;
    public static boolean zyvenCapeEnabled = true;
    public static boolean crosshairStyleEnabled = false;
    public static boolean cleanHudEnabled = false;

    public static boolean payAlertsEnabled = true;
    public static boolean payAlertParticles = true;
    public static int payAlertColor = 0xFF38CFFF;
    public static int payAlertDurationSeconds = 4;
    // Only incoming payments at or above this amount trigger the popup.
    public static double payAlertMinimum = 1_000_000;

    private static KeyBinding modulesKey;
    private static KeyBinding auctionKey;
    private static KeyBinding payAlertSettingsKey;

    private static PaymentAlert activePaymentAlert;
    private static final List<AlertParticle> ALERT_PARTICLES = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        loadSettings();
        KeyBinding.Category category = new KeyBinding.Category(Identifier.of("zyvenmod", "keys"));
        modulesKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zyvenmod.modules", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        auctionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zyvenmod.auction", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, category));
        payAlertSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zyvenmod.pay_alert_settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Y, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (modulesKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new ModulesScreen());
                else if (client.currentScreen instanceof ModulesScreen) client.setScreen(null);
            }
            while (auctionKey.wasPressed()) {
                if (client.currentScreen == null && client.player != null) {
                    client.player.networkHandler.sendChatCommand("ah");
                } else if (client.currentScreen instanceof AuctionScreen) {
                    client.setScreen(null);
                }
            }
            while (payAlertSettingsKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new PayAlertSettingsScreen());
            }
            tickPaymentAlert();
        });

        // Message parsing must never interfere with chat or server command handling.
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityRenderer instanceof PlayerEntityRenderer playerRenderer) {
                registrationHelper.register(new ZyvenHatFeatureRenderer(playerRenderer));
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            try {
                PROFIT.readIncoming(message.getString());
            } catch (RuntimeException ignored) {
                // Ignore malformed or unexpected server messages.
            }
        });
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            try {
                PROFIT.readOutgoing(message);
            } catch (RuntimeException ignored) {
                // Never block sending a chat message because of HUD bookkeeping.
            }
            return true;
        });
    }


    private static Path settingsPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("zyvenmod.properties");
    }

    private static void loadSettings() {
        Path path = settingsPath();
        if (!Files.isRegularFile(path)) return;
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
            double loadedMinimum = Double.parseDouble(properties.getProperty("payAlertMinimum", "1000000"));
            if (Double.isFinite(loadedMinimum) && loadedMinimum >= 0) {
                payAlertMinimum = loadedMinimum;
            }
        } catch (IOException | NumberFormatException ignored) {
            // A damaged config should never prevent the client from starting.
        }
    }

    public static void setPayAlertMinimum(double amount) {
        if (!Double.isFinite(amount) || amount < 0) return;
        payAlertMinimum = amount;
        Properties properties = new Properties();
        properties.setProperty("payAlertMinimum", Double.toString(amount));
        Path path = settingsPath();
        try {
            Files.createDirectories(path.getParent());
            try (OutputStream output = Files.newOutputStream(path)) {
                properties.store(output, "ZyvenMod settings");
            }
        } catch (IOException ignored) {
            // Keep the selected value for this session even if storage is unavailable.
        }
    }

    public static void showPaymentAlert(String payer, String amount) {
        if (!payAlertsEnabled) return;
        activePaymentAlert = new PaymentAlert(
                payer == null || payer.isBlank() ? "Someone" : payer,
                amount == null || amount.isBlank() ? "Payment received" : amount,
                System.currentTimeMillis()
        );
        ALERT_PARTICLES.clear();
        if (payAlertParticles) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int i = 0; i < 30; i++) {
                double angle = random.nextDouble(0, Math.PI * 2);
                double speed = random.nextDouble(0.35, 1.65);
                ALERT_PARTICLES.add(new AlertParticle(
                        random.nextDouble(-105, 106),
                        random.nextDouble(-18, 19),
                        Math.cos(angle) * speed,
                        Math.sin(angle) * speed - 0.35,
                        random.nextInt(18, 37)
                ));
            }
        }
    }

    private static void tickPaymentAlert() {
        Iterator<AlertParticle> iterator = ALERT_PARTICLES.iterator();
        while (iterator.hasNext()) {
            AlertParticle particle = iterator.next();
            particle.x += particle.vx;
            particle.y += particle.vy;
            particle.vy += 0.025;
            particle.life--;
            if (particle.life <= 0) iterator.remove();
        }
        if (activePaymentAlert != null
                && System.currentTimeMillis() - activePaymentAlert.createdAt
                > payAlertDurationSeconds * 1000L) {
            activePaymentAlert = null;
            ALERT_PARTICLES.clear();
        }
    }

    public static PaymentAlert getActivePaymentAlert() {
        return payAlertsEnabled ? activePaymentAlert : null;
    }

    public static List<AlertParticle> getAlertParticles() {
        return ALERT_PARTICLES;
    }

    public static String formatCompactAmount(double value) {
        double abs = Math.abs(value);
        String sign = value > 0 ? "+" : value < 0 ? "-" : "";
        if (abs >= 1_000_000_000) return sign + compactNumber(abs / 1_000_000_000) + "B";
        if (abs >= 1_000_000) return sign + compactNumber(abs / 1_000_000) + "M";
        if (abs >= 1_000) return sign + compactNumber(abs / 1_000) + "K";
        return sign + String.format(Locale.US, "%.0f", abs);
    }

    private static String compactNumber(double n) {
        return String.format(Locale.US, n >= 100 ? "%.0f" : n >= 10 ? "%.1f" : "%.2f", n)
                .replaceAll("\\.0+$", "").replaceAll("(\\.\\d*[1-9])0+$", "$1");
    }

    public static int[] alertColorPresets() {
        return new int[] {
                0xFF38CFFF, 0xFF598BFF, 0xFF9B78FF, 0xFFFF72B6,
                0xFFFFB454, 0xFF55D69A, 0xFFFFFFFF
        };
    }

    public static final class PaymentAlert {
        public final String payer;
        public final String amount;
        public final long createdAt;

        private PaymentAlert(String payer, String amount, long createdAt) {
            this.payer = payer;
            this.amount = amount;
            this.createdAt = createdAt;
        }
    }

    public static final class AlertParticle {
        public double x, y, vx, vy;
        public int life;

        private AlertParticle(double x, double y, double vx, double vy, int life) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
        }
    }

    public static final class ProfitTracker {
        private static final Pattern AMOUNT = Pattern.compile(
                "(?i)\\$?([+-]?\\d[\\d,]*(?:\\.\\d+)?\\s*[kmb]?)");
        private static final Pattern PAYER_PAID = Pattern.compile(
                "(?i)\\b([A-Za-z0-9_]{3,16})\\s+(?:has\\s+)?(?:paid|sent|given)\\s+you\\b");
        private static final Pattern PAYER_FROM = Pattern.compile(
                "(?i)\\b(?:from|by)\\s+([A-Za-z0-9_]{3,16})\\b");
        private double total;

        public void readIncoming(String rawMessage) {
            String message = rawMessage.replaceAll("§[0-9A-FK-ORa-fk-or]", "").trim();
            String lower = message.toLowerCase(Locale.ROOT);
            boolean payment = lower.contains("received") || lower.contains("paid you")
                    || lower.contains("sent you") || lower.contains("has given you")
                    || lower.contains("gave you") || lower.contains("payment from")
                    || lower.contains("paid by");
            if (!payment) return;

            Matcher amountMatcher = AMOUNT.matcher(message);
            if (!amountMatcher.find()) return;
            double amount = Math.abs(parse(amountMatcher.group(1)));
            if (amount <= 0) return;
            total += amount;

            if (amount < payAlertMinimum) return;
            String payer = findPayer(message);
            showPaymentAlert(payer, formatCompactAmount(amount));
        }

        private String findPayer(String message) {
            Matcher paid = PAYER_PAID.matcher(message);
            if (paid.find()) return paid.group(1);
            Matcher from = PAYER_FROM.matcher(message);
            if (from.find()) return from.group(1);
            return "Someone";
        }

        public void readOutgoing(String message) {
            String trimmed = message.trim();
            if (!trimmed.toLowerCase(Locale.ROOT).startsWith("/pay ")) return;
            Matcher matcher = AMOUNT.matcher(trimmed.substring(5));
            if (matcher.find()) total -= Math.abs(parse(matcher.group(1)));
        }

        private double parse(String raw) {
            String s = raw.toLowerCase(Locale.ROOT).replace(",", "").replaceAll("\\s+", "");
            double multiplier = 1;
            if (s.endsWith("k")) { multiplier = 1_000; s = s.substring(0, s.length() - 1); }
            else if (s.endsWith("m")) { multiplier = 1_000_000; s = s.substring(0, s.length() - 1); }
            else if (s.endsWith("b")) { multiplier = 1_000_000_000; s = s.substring(0, s.length() - 1); }
            try { return Double.parseDouble(s) * multiplier; }
            catch (NumberFormatException ignored) { return 0; }
        }

        public double value() { return total; }
        public String formatted() { return formatAmount(total); }

        private static String formatAmount(double value) {
            double abs = Math.abs(value);
            String sign = value > 0 ? "+" : value < 0 ? "-" : "";
            if (abs >= 1_000_000_000) return sign + shortNumber(abs / 1_000_000_000) + "B";
            if (abs >= 1_000_000) return sign + shortNumber(abs / 1_000_000) + "M";
            if (abs >= 1_000) return sign + shortNumber(abs / 1_000) + "K";
            return sign + String.format(Locale.US, "%.0f", abs);
        }

        private static String shortNumber(double number) {
            return String.format(Locale.US, number >= 100 ? "%.0f" : number >= 10 ? "%.1f" : "%.2f", number)
                    .replaceAll("\\.0+$", "").replaceAll("(\\.\\d*[1-9])0+$", "$1");
        }
    }
}
