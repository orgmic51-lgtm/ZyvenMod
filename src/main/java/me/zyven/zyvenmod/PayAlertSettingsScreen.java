package me.zyven.zyvenmod;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class PayAlertSettingsScreen extends Screen {
    private static final int[] COLORS = ZyvenMod.alertColorPresets();

    public PayAlertSettingsScreen() {
        super(Text.literal("Payment Alert Settings"));
    }

    @Override
    public void render(DrawContext d, int mouseX, int mouseY, float delta) {
        renderBackground(d, mouseX, mouseY, delta);
        int w = Math.min(width - 24, 440);
        int h = Math.min(height - 24, 370);
        int x = (width - w) / 2;
        int y = (height - h) / 2;

        rounded(d, x, y + 3, w, h, 14, 0x60000000);
        rounded(d, x, y, w, h, 14, 0xF00B111B);
        rounded(d, x + 1, y + 1, w - 2, 66, 13, 0xFF142333);
        d.fill(x + 16, y + 64, x + w - 16, y + 65, 0x3338CFFF);

        d.drawText(textRenderer, "ZYVEN  /  ALERT STUDIO", x + 19, y + 15, 0xFF38CFFF, true);
        d.drawText(textRenderer, "Payment notification settings", x + 19, y + 34, 0xFFA5B4C5, false);
        d.drawText(textRenderer, "Y  •  CLOSE", x + w - 89, y + 17, 0xFF8797A9, false);

        int rowX = x + 16, rowW = w - 32;
        int rowY = y + 82;
        row(d, rowX, rowY, rowW, "PAYMENT ALERTS",
                ZyvenMod.payAlertsEnabled ? "ON" : "OFF",
                ZyvenMod.payAlertsEnabled ? 0xFF5BE4A0 : 0xFFFF7481);
        rowY += 43;
        row(d, rowX, rowY, rowW, "PARTICLE BURST",
                ZyvenMod.payAlertParticles ? "ON" : "OFF",
                ZyvenMod.payAlertParticles ? 0xFF5BE4A0 : 0xFFFF7481);
        rowY += 43;
        row(d, rowX, rowY, rowW, "ALERT DURATION",
                ZyvenMod.payAlertDurationSeconds + " SECONDS", 0xFFB9D9EA);

        rowY += 57;
        d.drawText(textRenderer, "ACCENT COLOR", rowX + 2, rowY, 0xFFE5EDF5, true);
        d.drawText(textRenderer, "Name + particle color", rowX + 2, rowY + 15, 0xFF8495A9, false);

        int swatchSize = 25;
        int gap = Math.max(6, (rowW - 7 * swatchSize) / 6);
        int swatchY = rowY + 7;
        int totalSwatchWidth = 7 * swatchSize + 6 * gap;
        int swatchX = x + (w - totalSwatchWidth) / 2;
        for (int i = 0; i < COLORS.length; i++) {
            int sx = swatchX + i * (swatchSize + gap);
            boolean selected = ZyvenMod.payAlertColor == COLORS[i];
            rounded(d, sx - (selected ? 2 : 0), swatchY - (selected ? 2 : 0),
                    swatchSize + (selected ? 4 : 0), swatchSize + (selected ? 4 : 0), 8,
                    selected ? 0xFFFFFFFF : 0xFF303B4A);
            rounded(d, sx + 2, swatchY + 2, swatchSize - 4, swatchSize - 4, 6, COLORS[i]);
        }

        int previewY = Math.min(y + h - 45, swatchY + 43);
        rounded(d, x + 16, previewY, w - 32, 31, 8, 0xFF101A26);
        d.drawCenteredTextWithShadow(textRenderer,
                Text.literal("PREVIEW   Alex paid you   +1B"),
                width / 2, previewY + 11, ZyvenMod.payAlertColor);

        d.drawCenteredTextWithShadow(textRenderer, "Tap a row to change it  •  ESC to close",
                width / 2, y + h - 14, 0xFF718398);
        super.render(d, mouseX, mouseY, delta);
    }

    private void row(DrawContext d, int x, int y, int w, String label, String value, int valueColor) {
        rounded(d, x, y, w, 35, 8, 0xFF172230);
        d.drawText(textRenderer, label, x + 12, y + 13, 0xFFEAF1F8, true);
        d.drawText(textRenderer, value, x + w - textRenderer.getWidth(value) - 12,
                y + 13, valueColor, true);
    }

    private static void rounded(DrawContext d, int x, int y, int w, int h, int radius, int color) {
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
        int w = Math.min(width - 24, 440);
        int h = Math.min(height - 24, 370);
        int x = (width - w) / 2, y = (height - h) / 2;
        int rowX = x + 16, rowW = w - 32;
        double mx = click.x(), my = click.y();

        if (mx >= rowX && mx <= rowX + rowW && my >= y + 82 && my <= y + 117) {
            ZyvenMod.payAlertsEnabled = !ZyvenMod.payAlertsEnabled;
            return true;
        }
        if (mx >= rowX && mx <= rowX + rowW && my >= y + 125 && my <= y + 160) {
            ZyvenMod.payAlertParticles = !ZyvenMod.payAlertParticles;
            return true;
        }
        if (mx >= rowX && mx <= rowX + rowW && my >= y + 168 && my <= y + 203) {
            ZyvenMod.payAlertDurationSeconds = switch (ZyvenMod.payAlertDurationSeconds) {
                case 3 -> 4;
                case 4 -> 6;
                default -> 3;
            };
            return true;
        }

        int swatchSize = 25;
        int gap = Math.max(6, (rowW - 7 * swatchSize) / 6);
        int total = 7 * swatchSize + 6 * gap;
        int swatchX = x + (w - total) / 2;
        int swatchY = y + 82 + 43 * 3 + 57 + 7;
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
