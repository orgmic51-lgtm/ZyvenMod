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
    private static final int PANEL_HEIGHT = 28;
    private static final int CORNER_RADIUS = 6;
    private static final int HORIZONTAL_PADDING = 18;
    private static final int PANEL_GAP = 7;

    @Inject(method = "render", at = @At("TAIL"))
    private void zyven$drawOverlays(DrawContext draw, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || client.options.hudHidden) return;

        boolean fpsEnabled = ZyvenMod.showFps;
        boolean profitEnabled = ZyvenMod.showProfit;
        if (!fpsEnabled && !profitEnabled) return;

        String fpsText = client.getCurrentFps() + " FPS";
        String profitText = "PROFIT " + ZyvenMod.PROFIT.formatted();
        int fpsWidth = fpsEnabled ? client.textRenderer.getWidth(fpsText) + HORIZONTAL_PADDING * 2 : 0;
        int profitWidth = profitEnabled ? client.textRenderer.getWidth(profitText) + HORIZONTAL_PADDING * 2 : 0;
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
            drawPill(draw, x, y, profitWidth, PANEL_HEIGHT);
            int profitColor = ZyvenMod.PROFIT.value() > 0 ? 0xFF64E99A
                    : ZyvenMod.PROFIT.value() < 0 ? 0xFFFF737D : 0xFFFFFFFF;
            draw.drawCenteredTextWithShadow(client.textRenderer, Text.literal(profitText),
                    x + profitWidth / 2, y + (PANEL_HEIGHT - 9) / 2, profitColor);
        }
    }

    private static void drawPill(DrawContext draw, int x, int y, int width, int height) {
        if (!ZyvenMod.hudBackground) return;

        // Soft shadow and a rounded, translucent glass-like panel.
        drawRoundedRect(draw, x, y + 2, width, height, CORNER_RADIUS, 0x44000000);
        drawRoundedRect(draw, x, y, width, height, CORNER_RADIUS, 0xB8000000);
        // A faint top highlight keeps the panel from looking like a plain square.
        draw.fill(x + CORNER_RADIUS, y + 1, x + width - CORNER_RADIUS, y + 2, 0x20FFFFFF);
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
