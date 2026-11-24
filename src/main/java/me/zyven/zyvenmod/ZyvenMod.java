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
    public static int hudOpacity = 70;
    public static int profitHudOpacity = 85;
    public static int fpsHudOpacity = 85;
    public static int paymentHudOpacity = 92;
    public static double fpsHudX = 0.02, fpsHudY = 0.68;
    public static double profitHudX = 0.02, profitHudY = 0.88;
    public static double paymentHudX = 0.34, paymentHudY = 0.39;
    public static int hatStyle = 0;
    public static boolean fakeItemsEnabled = false;
    public static boolean viewmodelEnabled = false;
    public static float viewmodelX = 0.0f, viewmodelY = 0.0f, viewmodelZ = 0.0f;
    public static float viewmodelScale = 1.0f;
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
    private static KeyBinding hudLayoutKey;

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
        hudLayoutKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zyvenmod.hud_layout", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_L, category));

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
            while (hudLayoutKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new HudLayoutScreen());
                else if (client.currentScreen instanceof HudLayoutScreen) client.setScreen(null);
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
            if (Double.isFinite(loadedMinimum) && loadedMinimum >= 0) payAlertMinimum = loadedMinimum;
            hudOpacity = Math.max(0, Math.min(100, Integer.parseInt(properties.getProperty("hudOpacity", "70"))));
            profitHudOpacity = Math.max(0, Math.min(100, Integer.parseInt(properties.getProperty("profitHudOpacity", "85"))));
            fpsHudOpacity = Math.max(0, Math.min(100, Integer.parseInt(properties.getProperty("fpsHudOpacity", "85"))));
            paymentHudOpacity = Math.max(0, Math.min(100, Integer.parseInt(properties.getProperty("paymentHudOpacity", "92"))));
            fpsHudX = clamp01(Double.parseDouble(properties.getProperty("fpsHudX", "0.02")));
            fpsHudY = clamp01(Double.parseDouble(properties.getProperty("fpsHudY", "0.68")));
            profitHudX = clamp01(Double.parseDouble(properties.getProperty("profitHudX", "0.02")));
            profitHudY = clamp01(Double.parseDouble(properties.getProperty("profitHudY", "0.88")));
            paymentHudX = clamp01(Double.parseDouble(properties.getProperty("paymentHudX", "0.34")));
            paymentHudY = clamp01(Double.parseDouble(properties.getProperty("paymentHudY", "0.39")));
            hatStyle = Math.max(0, Math.min(4, Integer.parseInt(properties.getProperty("hatStyle", "0"))));
            showProfit = Boolean.parseBoolean(properties.getProperty("showProfit", "true"));
            showFps = Boolean.parseBoolean(properties.getProperty("showFps", "true"));
            hudBackground = Boolean.parseBoolean(properties.getProperty("hudBackground", "true"));
            fakeItemsEnabled = Boolean.parseBoolean(properties.getProperty("fakeItemsEnabled", "false"));
            viewmodelEnabled = Boolean.parseBoolean(properties.getProperty("viewmodelEnabled", "false"));
            customSkyEnabled = Boolean.parseBoolean(properties.getProperty("customSkyEnabled", "false"));
            hatsEnabled = Boolean.parseBoolean(properties.getProperty("hatsEnabled", "false"));
            zyvenCapeEnabled = Boolean.parseBoolean(properties.getProperty("zyvenCapeEnabled", "true"));
            crosshairStyleEnabled = Boolean.parseBoolean(properties.getProperty("crosshairStyleEnabled", "false"));
            cleanHudEnabled = Boolean.parseBoolean(properties.getProperty("cleanHudEnabled", "false"));
            payAlertsEnabled = Boolean.parseBoolean(properties.getProperty("payAlertsEnabled", "true"));
            payAlertParticles = Boolean.parseBoolean(properties.getProperty("payAlertParticles", "true"));
            payAlertColor = Integer.parseInt(properties.getProperty("payAlertColor", Integer.toString(payAlertColor)));
            payAlertDurationSeconds = Math.max(2, Math.min(10, Integer.parseInt(properties.getProperty("payAlertDurationSeconds", "4"))));
            viewmodelX = clampFloat(Float.parseFloat(properties.getProperty("viewmodelX", "0")), -0.5f, 0.5f);
            viewmodelY = clampFloat(Float.parseFloat(properties.getProperty("viewmodelY", "0")), -0.5f, 0.5f);
            viewmodelZ = clampFloat(Float.parseFloat(properties.getProperty("viewmodelZ", "0")), -0.5f, 0.5f);
            viewmodelScale = clampFloat(Float.parseFloat(properties.getProperty("viewmodelScale", "1")), 0.5f, 1.5f);
        } catch (IOException | NumberFormatException ignored) {
            // A damaged config should never prevent the client from starting.
        }
    }

    public static void setHudOpacity(int value) {
        hudOpacity = Math.max(0, Math.min(100, value));
        saveSettings();
    }

    public static void setProfitHudOpacity(int value) {
        profitHudOpacity = Math.max(0, Math.min(100, value));
        saveSettings();
    }

    public static void setFpsHudOpacity(int value) {
        fpsHudOpacity = Math.max(0, Math.min(100, value));
        saveSettings();
    }

    public static void setPaymentHudOpacity(int value) {
        paymentHudOpacity = Math.max(0, Math.min(100, value));
        saveSettings();
    }

    private static float clampFloat(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void saveUserSettings() {
        saveSettings();
    }

    public static void setViewmodelValue(int axis, float value) {
        switch (axis) {
            case 0 -> viewmodelX = clampFloat(value, -0.5f, 0.5f);
            case 1 -> viewmodelY = clampFloat(value, -0.5f, 0.5f);
            case 2 -> viewmodelZ = clampFloat(value, -0.5f, 0.5f);
            case 3 -> viewmodelScale = clampFloat(value, 0.5f, 1.5f);
        }
        saveSettings();
    }

    public static void setPayAlertEnabled(boolean enabled) {
        payAlertsEnabled = enabled;
        saveSettings();
    }

    public static void setPayAlertParticles(boolean enabled) {
        payAlertParticles = enabled;
        saveSettings();
    }

    public static void setPayAlertDuration(int seconds) {
        payAlertDurationSeconds = Math.max(2, Math.min(10, seconds));
        saveSettings();
    }

    public static void setPayAlertColor(int color) {
        payAlertColor = color;
        saveSettings();
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    public static void setHudPosition(int id, double x, double y) {
        double nx = clamp01(x), ny = clamp01(y);
        switch (id) {
            case 0 -> { fpsHudX = nx; fpsHudY = ny; }
            case 1 -> { profitHudX = nx; profitHudY = ny; }
            case 2 -> { paymentHudX = nx; paymentHudY = ny; }
        }
        saveSettings();
    }

    public static void setHatStyle(int style) {
        hatStyle = Math.floorMod(style, 5);
        saveSettings();
    }

    public static void setPayAlertMinimum(double amount) {
        if (!Double.isFinite(amount) || amount < 0) return;
        payAlertMinimum = amount;
        saveSettings();
    }

    private static void saveSettings() {
        Properties properties = new Properties();
        properties.setProperty("payAlertMinimum", Double.toString(payAlertMinimum));
        properties.setProperty("hudOpacity", Integer.toString(hudOpacity));
        properties.setProperty("profitHudOpacity", Integer.toString(profitHudOpacity));
        properties.setProperty("fpsHudOpacity", Integer.toString(fpsHudOpacity));
        properties.setProperty("paymentHudOpacity", Integer.toString(paymentHudOpacity));
        properties.setProperty("fpsHudX", Double.toString(fpsHudX));
        properties.setProperty("fpsHudY", Double.toString(fpsHudY));
        properties.setProperty("profitHudX", Double.toString(profitHudX));
        properties.setProperty("profitHudY", Double.toString(profitHudY));
        properties.setProperty("paymentHudX", Double.toString(paymentHudX));
        properties.setProperty("paymentHudY", Double.toString(paymentHudY));
        properties.setProperty("hatStyle", Integer.toString(hatStyle));
        properties.setProperty("showProfit", Boolean.toString(showProfit));
        properties.setProperty("showFps", Boolean.toString(showFps));
        properties.setProperty("hudBackground", Boolean.toString(hudBackground));
        properties.setProperty("fakeItemsEnabled", Boolean.toString(fakeItemsEnabled));
        properties.setProperty("viewmodelEnabled", Boolean.toString(viewmodelEnabled));
        properties.setProperty("customSkyEnabled", Boolean.toString(customSkyEnabled));
        properties.setProperty("hatsEnabled", Boolean.toString(hatsEnabled));
        properties.setProperty("zyvenCapeEnabled", Boolean.toString(zyvenCapeEnabled));
        properties.setProperty("crosshairStyleEnabled", Boolean.toString(crosshairStyleEnabled));
        properties.setProperty("cleanHudEnabled", Boolean.toString(cleanHudEnabled));
        properties.setProperty("payAlertsEnabled", Boolean.toString(payAlertsEnabled));
        properties.setProperty("payAlertParticles", Boolean.toString(payAlertParticles));
        properties.setProperty("payAlertColor", Integer.toString(payAlertColor));
        properties.setProperty("payAlertDurationSeconds", Integer.toString(payAlertDurationSeconds));
        properties.setProperty("viewmodelX", Float.toString(viewmodelX));
        properties.setProperty("viewmodelY", Float.toString(viewmodelY));
        properties.setProperty("viewmodelZ", Float.toString(viewmodelZ));
        properties.setProperty("viewmodelScale", Float.toString(viewmodelScale));
        Path path = settingsPath();
        try {
            Files.createDirectories(path.getParent());
            try (OutputStream output = Files.newOutputStream(path)) {
                properties.store(output, "ZyvenMod settings");
            }
        } catch (IOException ignored) {
            // Keep selected settings for the session if storage is unavailable.
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
            for (int i = 0; i < 96; i++) {
                double angle = random.nextDouble(0, Math.PI * 2);
                double speed = random.nextDouble(0.35, 1.65);
                ALERT_PARTICLES.add(new AlertParticle(
                        random.nextDouble(-185, 186),
                        random.nextDouble(-75, 76),
                        Math.cos(angle) * speed * 1.35,
                        Math.sin(angle) * speed * 1.35 - 0.45,
                        random.nextInt(24, 52)
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
                "(?i)\\b(?:from|by|payer\\s*:)\\s*([A-Za-z0-9_]{3,16})\\b");
        private static final Pattern PAYER_PREFIX = Pattern.compile(
                "(?i)^\\s*([A-Za-z0-9_]{3,16})\\s+(?:paid|sent|gave)\\s+(?:you|a payment)\\b");
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
            Matcher prefix = PAYER_PREFIX.matcher(message);
            if (prefix.find()) return prefix.group(1);
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
