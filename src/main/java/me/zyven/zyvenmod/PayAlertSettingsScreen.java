package me.zyven.zyvenmod;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class PayAlertSettingsScreen extends Screen {
    private static final int[] COLORS = ZyvenMod.alertColorPresets();

    public PayAlertSettingsScreen() { super(Text.literal("Payment Alert Settings")); }

    @Override
    public void render(DrawContext d, int mouseX, int mouseY, float delta) {
        renderBackground(d, mouseX, mouseY, delta);
        boolean compact = height < 310;
        int w = Math.min(width - 20, 440);
        int h = compact ? Math.min(height - 12, 270) : Math.min(height - 24, 390);
        int x = (width - w) / 2, y = (height - h) / 2;

        rounded(d, x, y + 3, w, h, 14, 0x60000000);
        rounded(d, x, y, w, h, 14, 0xF00B111B);
        int headerH = compact ? 43 : 66;
        rounded(d, x + 1, y + 1, w - 2, headerH, 13, 0xFF142333);
        d.fill(x + 16, y + headerH - 1, x + w - 16, y + headerH, 0x5538CFFF);

        d.drawText(textRenderer, "ZYVEN  /  ALERT STUDIO", x + 17, y + (compact ? 9 : 15),
                0xFF38CFFF, true);
        d.drawText(textRenderer, compact ? "Tap to customize" : "Payment notification settings",
                x + 17, y + (compact ? 25 : 34), 0xFFA5B4C5, false);
        if (!compact) d.drawText(textRenderer, "Y  •  CLOSE", x + w - 89, y + 17, 0xFF8797A9, false);

        int rowX = x + 13, rowW = w - 26;
        int rowHeight = compact ? 26 : 34;
        int rowStart = y + (compact ? 49 : 82);
        int rowStep = compact ? 30 : 40;
        row(d, rowX, rowStart, rowW, rowHeight, "PAYMENT ALERTS",
                ZyvenMod.payAlertsEnabled ? "ON" : "OFF",
                ZyvenMod.payAlertsEnabled ? 0xFF5BE4A0 : 0xFFFF7481);
        row(d, rowX, rowStart + rowStep, rowW, rowHeight, "PARTICLE BURST",
                ZyvenMod.payAlertParticles ? "ON" : "OFF",
                ZyvenMod.payAlertParticles ? 0xFF5BE4A0 : 0xFFFF7481);
        row(d, rowX, rowStart + rowStep * 2, rowW, rowHeight, "DURATION",
                ZyvenMod.payAlertDurationSeconds + " SEC", 0xFFB9D9EA);
        row(d, rowX, rowStart + rowStep * 3, rowW, rowHeight, "MINIMUM PAYMENT",
                ZyvenMod.formatCompactAmount(ZyvenMod.payAlertMinimum), 0xFF38CFFF);

        int colorLabelY = y + (compact ? 174 : 264);
        d.drawText(textRenderer, "ACCENT COLOR", rowX + 2, colorLabelY, 0xFFE5EDF5, true);
        if (!compact) d.drawText(textRenderer, "Name + particle color", rowX + 2, colorLabelY + 15, 0xFF8495A9, false);

        int swatchSize = compact ? 22 : 25;
        int gap = Math.max(3, (rowW - 7 * swatchSize) / 6);
        int totalWidth = 7 * swatchSize + 6 * gap;
        int swatchX = x + (w - totalWidth) / 2;
        int swatchY = y + (compact ? 192 : 296);
        for (int i = 0; i < COLORS.length; i++) {
            int sx = swatchX + i * (swatchSize + gap);
            boolean selected = ZyvenMod.payAlertColor == COLORS[i];
            rounded(d, sx - (selected ? 2 : 0), swatchY - (selected ? 2 : 0),
                    swatchSize + (selected ? 4 : 0), swatchSize + (selected ? 4 : 0), 7,
                    selected ? 0xFFFFFFFF : 0xFF303B4A);
            rounded(d, sx + 2, swatchY + 2, swatchSize - 4, swatchSize - 4, 5, COLORS[i]);
        }

        if (!compact) {
            int previewY = Math.min(y + h - 45, swatchY + 43);
            rounded(d, x + 16, previewY, w - 32, 31, 8, 0xFF101A26);
            d.drawCenteredTextWithShadow(textRenderer, Text.literal("PREVIEW   Alex paid you   +1B"),
                    width / 2, previewY + 11, ZyvenMod.payAlertColor);
            d.drawCenteredTextWithShadow(textRenderer, "Tap a row to change it  •  ESC to close",
                    width / 2, y + h - 14, 0xFF718398);
        }
        super.render(d, mouseX, mouseY, delta);
    }

    private void row(DrawContext d, int x, int y, int w, int h,
                     String label, String value, int valueColor) {
        rounded(d, x, y, w, h, 8, 0xFF172230);
        int textY = y + (h - 9) / 2;
        d.drawText(textRenderer, label, x + 10, textY, 0xFFEAF1F8, true);
        d.drawText(textRenderer, value, x + w - textRenderer.getWidth(value) - 10,
                textY, valueColor, true);
    }

    private static void rounded(DrawContext d, int x, int y, int w, int h, int radius, int color) {
        if (w <= 0 || h <= 0) return;
        for (int row = 0; row < h; row++) {
            int inset = 0;
            if (row < radius) {
                double dy = radius - row - 0.5;
                inset = Math.max(0, radius - (int) Math.sqrt(Math.max(0, radius * radius - dy * dy)));
            } else if (row >= h - radius) {
                double dy = row - (h - radius) + 0.5;
                inset = Math.max(0, radius - (int) Math.sqrt(Math.max(0, radius * radius - dy * dy)));
            }
            d.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        boolean compact = height < 310;
        int w = Math.min(width - 20, 440);
        int h = compact ? Math.min(height - 12, 270) : Math.min(height - 24, 390);
        int x = (width - w) / 2, y = (height - h) / 2;
        int rowX = x + 13, rowW = w - 26;
        int rowStart = y + (compact ? 49 : 82);
        int rowStep = compact ? 30 : 40;
        int rowHeight = compact ? 26 : 34;
        double mx = click.x(), my = click.y();

        if (mx >= rowX && mx <= rowX + rowW && my >= rowStart && my <= rowStart + rowHeight) {
            ZyvenMod.payAlertsEnabled = !ZyvenMod.payAlertsEnabled;
            return true;
        }
        if (mx >= rowX && mx <= rowX + rowW
                && my >= rowStart + rowStep && my <= rowStart + rowStep + rowHeight) {
            ZyvenMod.payAlertParticles = !ZyvenMod.payAlertParticles;
            return true;
        }
        if (mx >= rowX && mx <= rowX + rowW
                && my >= rowStart + rowStep * 2 && my <= rowStart + rowStep * 2 + rowHeight) {
            ZyvenMod.payAlertDurationSeconds = switch (ZyvenMod.payAlertDurationSeconds) {
                case 3 -> 4;
                case 4 -> 6;
                default -> 3;
            };
            return true;
        }

        if (mx >= rowX && mx <= rowX + rowW
                && my >= rowStart + rowStep * 3 && my <= rowStart + rowStep * 3 + rowHeight) {
            double[] minimums = {0, 1_000, 10_000, 100_000, 1_000_000,
                    10_000_000, 100_000_000, 1_000_000_000};
            int current = 0;
            for (int i = 0; i < minimums.length; i++) {
                if (minimums[i] == ZyvenMod.payAlertMinimum) current = i;
            }
            ZyvenMod.payAlertMinimum = minimums[(current + 1) % minimums.length];
            return true;
        }

        int swatchSize = compact ? 22 : 25;
        int gap = Math.max(3, (rowW - 7 * swatchSize) / 6);
        int total = 7 * swatchSize + 6 * gap;
        int swatchX = x + (w - total) / 2;
        int swatchY = y + (compact ? 192 : 296);
        if (my >= swatchY - 4 && my <= swatchY + swatchSize + 4) {
            for (int i = 0; i < COLORS.length; i++) {
                int sx = swatchX + i * (swatchSize + gap);
                if (mx >= sx - 3 && mx <= sx + swatchSize + 3) {
                    ZyvenMod.payAlertColor = COLORS[i];
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE || input.key() == GLFW.GLFW_KEY_Y) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override public boolean shouldPause() { return false; }
}
