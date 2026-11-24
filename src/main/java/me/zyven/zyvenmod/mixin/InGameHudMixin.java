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

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void zyven$customCrosshair(DrawContext draw, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!ZyvenMod.crosshairStyleEnabled) return;
        int cx = draw.getScaledWindowWidth() / 2;
        int cy = draw.getScaledWindowHeight() / 2;
        int cyan = 0xFF55D8FF;
        int white = 0xFFEAFBFF;
        draw.fill(cx - 1, cy - 7, cx + 2, cy - 2, cyan);
        draw.fill(cx - 1, cy + 2, cx + 2, cy + 7, cyan);
        draw.fill(cx - 7, cy - 1, cx - 2, cy + 2, cyan);
        draw.fill(cx + 2, cy - 1, cx + 7, cy + 2, cyan);
        draw.fill(cx - 1, cy - 1, cx + 2, cy + 2, white);
        ci.cancel();
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void zyven$cleanSidebar(DrawContext draw, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (ZyvenMod.cleanHudEnabled) ci.cancel();
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void zyven$cleanStatusEffects(DrawContext draw, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (ZyvenMod.cleanHudEnabled) ci.cancel();
    }

    private static void drawProfitAndFps(DrawContext draw, MinecraftClient client) {
        int screenWidth = draw.getScaledWindowWidth();
        int screenHeight = draw.getScaledWindowHeight();
        String fpsText = client.getCurrentFps() + " FPS";
        String profitValue = ZyvenMod.PROFIT.formatted();

        if (ZyvenMod.showFps) {
            int w = client.textRenderer.getWidth(fpsText) + 30;
            int x = Math.round((float) (ZyvenMod.fpsHudX * Math.max(0, screenWidth - w)));
            int y = Math.round((float) (ZyvenMod.fpsHudY * Math.max(0, screenHeight - HUD_HEIGHT)));
            drawPill(draw, x, y, w, HUD_HEIGHT,
                    opacityColor(0xC5090D14, ZyvenMod.fpsHudOpacity));
            draw.drawCenteredTextWithShadow(client.textRenderer, Text.literal(fpsText),
                    x + w / 2, y + 10, 0xFFFFFFFF);
        }

        if (ZyvenMod.showProfit) {
            String label = "PROFIT";
            int w = 30 + client.textRenderer.getWidth(label) + 7
                    + client.textRenderer.getWidth(profitValue);
            int x = Math.round((float) (ZyvenMod.profitHudX * Math.max(0, screenWidth - w)));
            int y = Math.round((float) (ZyvenMod.profitHudY * Math.max(0, screenHeight - HUD_HEIGHT)));
            int profitColor = ZyvenMod.PROFIT.value() > 0 ? 0xFF62E89A
                    : ZyvenMod.PROFIT.value() < 0 ? 0xFFFF6878 : 0xFFE7EAF0;
            int accent = ZyvenMod.PROFIT.value() > 0 ? 0xFF39D98A
                    : ZyvenMod.PROFIT.value() < 0 ? 0xFFFF5364 : 0xFF8B9AAA;
            drawPill(draw, x, y, w, HUD_HEIGHT,
                    opacityColor(0xC5090D14, ZyvenMod.profitHudOpacity));
            rounded(draw, x + 5, y + 7, 3, HUD_HEIGHT - 14, 2, accent);
            int textX = x + 15;
            draw.drawTextWithShadow(client.textRenderer, Text.literal(label),
                    textX, y + 10, 0xFFB7C0CC);
            draw.drawTextWithShadow(client.textRenderer, Text.literal(profitValue),
                    textX + client.textRenderer.getWidth(label) + 7, y + 10, profitColor);
        }
    }

    private static void drawPaymentAlert(DrawContext draw, MinecraftClient client) {
        ZyvenMod.PaymentAlert alert = ZyvenMod.getActivePaymentAlert();
        if (alert == null) return;

        long elapsed = System.currentTimeMillis() - alert.createdAt;
        float duration = Math.max(1.0f, ZyvenMod.payAlertDurationSeconds * 1000.0f);
        float progress = Math.min(1.0f, Math.max(0.0f, elapsed / duration));
        float enterT = Math.min(1.0f, elapsed / 420.0f);
        float enterEase = 1.0f - (float) Math.pow(1.0f - enterT, 3.0);
        float exitT = progress > 0.82f ? (progress - 0.82f) / 0.18f : 0.0f;
        float exitEase = exitT * exitT * (3.0f - 2.0f * exitT);
        float fade = progress > 0.82f ? 1.0f - exitEase : 1.0f;
        float zoom = 0.68f + 0.32f * enterEase;
        zoom *= 1.0f - 0.10f * exitEase;

        int screenWidth = draw.getScaledWindowWidth();
        int screenHeight = draw.getScaledWindowHeight();
        int panelWidth = Math.min(340, screenWidth - 16);
        int panelHeight = 136;
        int actualWidth = Math.max(1, Math.round(panelWidth * zoom));
        int actualHeight = Math.max(1, Math.round(panelHeight * zoom));
        int maxX = Math.max(0, screenWidth - panelWidth);
        int maxY = Math.max(0, screenHeight - panelHeight);
        int baseX = Math.round((float) (ZyvenMod.paymentHudX * maxX));
        int baseY = Math.round((float) (ZyvenMod.paymentHudY * maxY));
        int panelX = baseX + (panelWidth - actualWidth) / 2 + Math.round(exitEase * 78.0f);
        int panelY = baseY + (panelHeight - actualHeight) / 2
                - Math.round((1.0f - enterEase) * 58.0f) + Math.round(exitEase * 24.0f);
        int alpha = Math.max(0, Math.min(255, Math.round(fade * 255.0f)));
        int accent = applyAlpha(ZyvenMod.payAlertColor, fade);
        int textWhite = applyAlpha(0xFFFFFFFF, fade);

        if (ZyvenMod.payAlertParticles) {
            for (ZyvenMod.AlertParticle particle : ZyvenMod.getAlertParticles()) {
                float particleFade = Math.min(1.0f, particle.life / 18.0f) * fade;
                int particleColor = applyAlpha(
                        particle.life % 4 == 0 ? 0xFFFFFFFF : ZyvenMod.payAlertColor, particleFade);
                int px = baseX + panelWidth / 2 + (int) particle.x;
                int py = baseY + panelHeight / 2 + (int) particle.y;
                int size = particle.life % 3 == 0 ? 3 : 2;
                draw.fill(px - size, py, px + size + 1, py + 1, particleColor);
                draw.fill(px, py - size, px + 1, py + size + 1, particleColor);
                if (particle.life % 2 == 0) {
                    draw.fill(px - 1, py - 1, px + 2, py + 2, particleColor);
                }
            }
        }

        rounded(draw, panelX, panelY + 4, actualWidth, actualHeight, 14,
                applyAlpha(opacityColor(0x88000000, ZyvenMod.paymentHudOpacity), fade));
        rounded(draw, panelX, panelY, actualWidth, actualHeight, 14,
                applyAlpha(opacityColor(0xF00B111B, ZyvenMod.paymentHudOpacity), fade));
        rounded(draw, panelX + 1, panelY + 1, actualWidth - 2, 4, 2, accent);
        draw.drawCenteredTextWithShadow(client.textRenderer, Text.literal("PAYMENT RECEIVED"),
                panelX + actualWidth / 2, panelY + 16, accent);

        int faceSize = Math.max(34, Math.round(46 * zoom));
        int faceX = panelX + 20;
        int faceY = panelY + 48;
        rounded(draw, faceX - 4, faceY - 4, faceSize + 8, faceSize + 8, 10,
                applyAlpha(0xFF243546, fade));
        PlayerListEntry payerEntry = findPayerEntry(client, alert.payer);
        if (payerEntry != null) {
            PlayerSkinDrawer.draw(draw, payerEntry.getSkinTextures(), faceX, faceY, faceSize,
                    applyAlpha(0xFFFFFFFF, fade));
        } else {
            // Avoid showing a misleading first-letter avatar when the payer skin is not cached.
            rounded(draw, faceX, faceY, faceSize, faceSize, 7, applyAlpha(0xFF31516A, fade));
            draw.drawCenteredTextWithShadow(client.textRenderer, Text.literal("SKIN"),
                    faceX + faceSize / 2, faceY + faceSize / 2 - 4, textWhite);
        }

        int nameX = faceX + faceSize + 15;
        int nameMaxWidth = Math.max(24, panelX + actualWidth - nameX - 14);
        String payerName = client.textRenderer.trimToWidth(alert.payer, nameMaxWidth);
        draw.drawTextWithShadow(client.textRenderer, Text.literal(payerName),
                nameX, faceY + 13, applyAlpha(0xFFFFFFFF, fade));
        draw.drawTextWithShadow(client.textRenderer, Text.literal(alert.amount),
                nameX, faceY + 36, applyAlpha(0xFFFFD45C, fade));

        draw.fill(panelX + 18, panelY + actualHeight - 9,
                panelX + actualWidth - 18, panelY + actualHeight - 7,
                applyAlpha(0x3348D8FF, fade));
        int progressWidth = Math.max(0,
                Math.round((actualWidth - 36) * progress));
        if (progressWidth > 0) {
            draw.fill(panelX + 18, panelY + actualHeight - 9,
                    panelX + 18 + progressWidth, panelY + actualHeight - 7, accent);
        }
    }

    private static PlayerListEntry findPayerEntry(MinecraftClient client, String payerName) {
        if (client.getNetworkHandler() == null || payerName == null) return null;
        PlayerListEntry exact = client.getNetworkHandler().getPlayerListEntry(payerName);
        if (exact != null) return exact;
        for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
            if (entry.getProfile().name().equalsIgnoreCase(payerName)) return entry;
        }
        return null;
    }

    private static int applyAlpha(int color, float alpha) {
        int originalAlpha = (color >>> 24) & 255;
        int resultAlpha = Math.max(0, Math.min(255, Math.round(originalAlpha * alpha)));
        return (resultAlpha << 24) | (color & 0x00FFFFFF);
    }

    private static int opacityColor(int color, int opacity) {
        int alpha = (color >>> 24) & 255;
        alpha = alpha * Math.max(0, Math.min(100, opacity)) / 100;
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
