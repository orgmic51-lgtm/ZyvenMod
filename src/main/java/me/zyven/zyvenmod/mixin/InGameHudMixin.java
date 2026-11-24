package me.zyven.zyvenmod.mixin;

import me.zyven.zyvenmod.ZyvenMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    private static final int HUD_HEIGHT = 30;

    @Inject(method = "render", at = @At("TAIL"))
    private void zyven$drawOverlays(DrawContext draw, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || client.options.hudHidden) return;

        drawProfitAndFps(draw, client);
        drawPaymentAlert(draw, client);
    }

    private static void drawProfitAndFps(DrawContext draw, MinecraftClient client) {
        boolean fpsEnabled = ZyvenMod.showFps;
        boolean profitEnabled = ZyvenMod.showProfit;
        if (!fpsEnabled && !profitEnabled) return;

        String fpsText = client.getCurrentFps() + " FPS";
        String profitValue = ZyvenMod.PROFIT.formatted();
        String profitLabel = "PROFIT";
        int horizontalPadding = 15;
        int fpsWidth = fpsEnabled ? client.textRenderer.getWidth(fpsText) + horizontalPadding * 2 : 0;
        int profitWidth = profitEnabled ? horizontalPadding * 2
                + client.textRenderer.getWidth(profitLabel) + 7
                + client.textRenderer.getWidth(profitValue) : 0;
        int gap = fpsEnabled && profitEnabled ? 8 : 0;
        int totalWidth = fpsWidth + profitWidth + gap;
        int x = (draw.getScaledWindowWidth() - totalWidth) / 2;
        int y = 12;

        if (fpsEnabled) {
            drawPill(draw, x, y, fpsWidth, HUD_HEIGHT, opacityColor(0xC5090D14));
            draw.drawCenteredTextWithShadow(client.textRenderer, Text.literal(fpsText),
                    x + fpsWidth / 2, y + 10, 0xFFFFFFFF);
            x += fpsWidth + gap;
        }

        if (profitEnabled) {
            int profitColor = ZyvenMod.PROFIT.value() > 0 ? 0xFF62E89A
                    : ZyvenMod.PROFIT.value() < 0 ? 0xFFFF6878 : 0xFFE7EAF0;
            int accent = ZyvenMod.PROFIT.value() > 0 ? 0xFF39D98A
                    : ZyvenMod.PROFIT.value() < 0 ? 0xFFFF5364 : 0xFF8B9AAA;
            drawPill(draw, x, y, profitWidth, HUD_HEIGHT, opacityColor(0xC5090D14));
            rounded(draw, x + 5, y + 7, 3, HUD_HEIGHT - 14, 2, accent);
            int textX = x + horizontalPadding + 3;
            draw.drawTextWithShadow(client.textRenderer, Text.literal(profitLabel),
                    textX, y + 10, 0xFFB7C0CC);
            draw.drawTextWithShadow(client.textRenderer, Text.literal(profitValue),
                    textX + client.textRenderer.getWidth(profitLabel) + 7, y + 10, profitColor);
        }
    }

    private static void drawPaymentAlert(DrawContext draw, MinecraftClient client) {
        ZyvenMod.PaymentAlert alert = ZyvenMod.getActivePaymentAlert();
        if (alert == null) return;

        long elapsed = System.currentTimeMillis() - alert.createdAt;
        float duration = ZyvenMod.payAlertDurationSeconds * 1000.0f;
        float progress = Math.min(1.0f, Math.max(0.0f, elapsed / duration));
        float fade = progress > 0.82f ? (1.0f - progress) / 0.18f : 1.0f;
        float entrance = Math.min(1.0f, elapsed / 260.0f);
        entrance = 1.0f - (float) Math.pow(1.0f - entrance, 3.0);
        int screenWidth = draw.getScaledWindowWidth();
        int screenHeight = draw.getScaledWindowHeight();
        int panelWidth = Math.min(340, screenWidth - 20);
        int panelHeight = 104;
        int panelX = (screenWidth - panelWidth) / 2;
        int targetY = screenHeight / 2 - panelHeight / 2;
        int panelY = targetY - Math.round((1.0f - entrance) * 26.0f);
        int alpha = Math.max(0, Math.min(255, Math.round(fade * 255.0f)));
        int accent = applyAlpha(ZyvenMod.payAlertColor, fade);
        int textWhite = applyAlpha(0xFFFFFFFF, fade);
        int muted = applyAlpha(0xFF9BAABD, fade);

        // Floating sparkle particles around the card.
        if (ZyvenMod.payAlertParticles) {
            for (ZyvenMod.AlertParticle particle : ZyvenMod.getAlertParticles()) {
                float particleFade = Math.min(1.0f, particle.life / 12.0f) * fade;
                int particleColor = applyAlpha(
                        (particle.life % 3 == 0) ? 0xFFFFFFFF : ZyvenMod.payAlertColor, particleFade);
                int px = screenWidth / 2 + (int) particle.x;
                int py = targetY + 54 + (int) particle.y;
                draw.fill(px - 1, py - 4, px + 1, py + 5, particleColor);
                draw.fill(px - 4, py - 1, px + 5, py + 1, particleColor);
            }
        }

        rounded(draw, panelX, panelY + 3, panelWidth, panelHeight, 13, applyAlpha(0x70000000, fade));
        rounded(draw, panelX, panelY, panelWidth, panelHeight, 13, applyAlpha(0xF00B111B, fade));
        rounded(draw, panelX + 1, panelY + 1, panelWidth - 2, 3, 2, accent);
        draw.drawCenteredTextWithShadow(client.textRenderer,
                Text.literal("PAYMENT RECEIVED"), screenWidth / 2, panelY + 12, accent);

        int faceSize = 34;
        int faceX = panelX + 22;
        int faceY = panelY + 35;
        rounded(draw, faceX - 3, faceY - 3, faceSize + 6, faceSize + 6, 9,
                applyAlpha(0xFF243546, fade));
        PlayerListEntry payerEntry = client.getNetworkHandler() == null
                ? null : client.getNetworkHandler().getPlayerListEntry(alert.payer);
        if (payerEntry != null) {
            PlayerSkinDrawer.draw(draw, payerEntry.getSkinTextures(), faceX, faceY, faceSize, 0xFFFFFFFF);
        } else {
            rounded(draw, faceX, faceY, faceSize, faceSize, 7, applyAlpha(0xFF31516A, fade));
            String initial = alert.payer.isEmpty() ? "?" : alert.payer.substring(0, 1).toUpperCase();
            draw.drawCenteredTextWithShadow(client.textRenderer, initial,
                    faceX + faceSize / 2, faceY + 12, textWhite);
        }

        int nameX = faceX + faceSize + 13;
        int nameMaxWidth = Math.max(30, panelWidth - (nameX - panelX) - 15);
        String payerName = client.textRenderer.trimToWidth(alert.payer, nameMaxWidth);
        draw.drawTextWithShadow(client.textRenderer, Text.literal(payerName),
                nameX, faceY + 12, accent);

        draw.drawCenteredTextWithShadow(client.textRenderer, Text.literal(alert.amount),
                screenWidth / 2, panelY + 75, textWhite);
        draw.fill(panelX + 18, panelY + panelHeight - 8, panelX + panelWidth - 18,
                panelY + panelHeight - 7, applyAlpha(0x3348D8FF, fade));
        // Small progress marker gives the notification a subtle timed finish.
        int progressWidth = Math.max(0, Math.round((panelWidth - 36) * progress));
        if (progressWidth > 0) {
            draw.fill(panelX + 18, panelY + panelHeight - 8,
                    panelX + 18 + progressWidth, panelY + panelHeight - 7, accent);
        }
    }

    private static int applyAlpha(int color, float alpha) {
        int originalAlpha = (color >>> 24) & 255;
        int resultAlpha = Math.max(0, Math.min(255, Math.round(originalAlpha * alpha)));
        return (resultAlpha << 24) | (color & 0x00FFFFFF);
    }

    private static int opacityColor(int color) {
        int alpha = (color >>> 24) & 255;
        alpha = alpha * ZyvenMod.hudOpacity / 100;
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    private static void drawPill(DrawContext draw, int x, int y, int width, int height, int color) {
        if (!ZyvenMod.hudBackground) return;
        rounded(draw, x, y + 2, width, height, 9, 0x44000000);
        rounded(draw, x, y, width, height, 9, color);
        draw.fill(x + 9, y + 1, x + width - 9, y + 2, 0x20FFFFFF);
        draw.fill(x + 9, y + height - 1, x + width - 9, y + height, 0x1800C8FF);
    }

    private static void rounded(DrawContext draw, int x, int y, int width, int height,
                                int radius, int color) {
        if (width <= 0 || height <= 0) return;
        for (int row = 0; row < height; row++) {
            int inset = 0;
            if (row < radius) {
                double dy = radius - row - 0.5;
                inset = Math.max(0, radius - (int) Math.sqrt(Math.max(0, radius * radius - dy * dy)));
            } else if (row >= height - radius) {
                double dy = row - (height - radius) + 0.5;
                inset = Math.max(0, radius - (int) Math.sqrt(Math.max(0, radius * radius - dy * dy)));
            }
            draw.fill(x + inset, y + row, x + width - inset, y + row + 1, color);
        }
    }
}
