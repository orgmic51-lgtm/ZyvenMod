package me.zyven.zyvenmod.mixin;

import me.zyven.zyvenmod.ZyvenMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    private static final int PANEL_HEIGHT = 30;
    private static final int CORNER_RADIUS = 8;
    private static final int HORIZONTAL_PADDING = 15;
    private static final int PANEL_GAP = 8;

    @Inject(method = "render", at = @At("TAIL"))
    private void zyven$drawOverlays(DrawContext draw, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || client.options.hudHidden) return;

        boolean fpsEnabled = ZyvenMod.showFps;
        boolean profitEnabled = ZyvenMod.showProfit;
        if (!fpsEnabled && !profitEnabled) return;

        String fpsText = client.getCurrentFps() + " FPS";
        String profitValue = ZyvenMod.PROFIT.formatted();
        String profitLabel = "PROFIT";
        int fpsWidth = fpsEnabled ? client.textRenderer.getWidth(fpsText) + HORIZONTAL_PADDING * 2 : 0;
        int labelWidth = client.textRenderer.getWidth(profitLabel);
        int valueWidth = client.textRenderer.getWidth(profitValue);
        int profitWidth = profitEnabled ? HORIZONTAL_PADDING * 2 + labelWidth + 7 + valueWidth : 0;
        int gap = fpsEnabled && profitEnabled ? PANEL_GAP : 0;
        int totalWidth = fpsWidth + profitWidth + gap;
        int x = (draw.getScaledWindowWidth() - totalWidth) / 2;
        int y = 12;

        if (fpsEnabled) {
            drawPill(draw, x, y, fpsWidth, PANEL_HEIGHT);
            draw.drawCenteredTextWithShadow(client.textRenderer, Text.literal(fpsText),
                    x + fpsWidth / 2, y + (PANEL_HEIGHT - 9) / 2, 0xFFFFFFFF);
            x += fpsWidth + gap;
        }

        if (profitEnabled) {
            int profitColor = ZyvenMod.PROFIT.value() > 0 ? 0xFF62E89A
                    : ZyvenMod.PROFIT.value() < 0 ? 0xFFFF6878 : 0xFFE7EAF0;
            int accentColor = ZyvenMod.PROFIT.value() > 0 ? 0xFF39D98A
                    : ZyvenMod.PROFIT.value() < 0 ? 0xFFFF5364 : 0xFF8B9AAA;
            drawPill(draw, x, y, profitWidth, PANEL_HEIGHT);
            drawRoundedRect(draw, x + 5, y + 7, 3, PANEL_HEIGHT - 14, 2, accentColor);
            int textX = x + HORIZONTAL_PADDING + 3;
            int textY = y + (PANEL_HEIGHT - 9) / 2;
            draw.drawTextWithShadow(client.textRenderer, Text.literal(profitLabel),
                    textX, textY, 0xFFB7C0CC);
            draw.drawTextWithShadow(client.textRenderer, Text.literal(profitValue),
                    textX + labelWidth + 7, textY, profitColor);
        }
    }

    private static void drawPill(DrawContext draw, int x, int y, int width, int height) {
        if (!ZyvenMod.hudBackground) return;
        drawRoundedRect(draw, x, y + 2, width, height, CORNER_RADIUS, 0x44000000);
        drawRoundedRect(draw, x, y, width, height, CORNER_RADIUS, 0xC5090D14);
        draw.fill(x + CORNER_RADIUS, y + 1, x + width - CORNER_RADIUS, y + 2, 0x20FFFFFF);
        draw.fill(x + CORNER_RADIUS, y + height - 1, x + width - CORNER_RADIUS, y + height, 0x1800C8FF);
    }

    private static void drawRoundedRect(DrawContext draw, int x, int y, int width, int height,
                                        int radius, int color) {
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
