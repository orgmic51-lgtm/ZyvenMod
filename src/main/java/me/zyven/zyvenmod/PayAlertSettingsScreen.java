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

    private boolean compact() {
        // MJ Launcher commonly presents Minecraft in a short landscape viewport.
        return width < 600 || height < 500;
    }

    private int panelWidth() {
        return Math.min(width - 16, compact() ? 360 : 440);
    }

    private int panelHeight() {
        return Math.min(height - 14, compact() ? 224 : 370);
    }

    private int panelX() { return (width - panelWidth()) / 2; }
    private int panelY() { return (height - panelHeight()) / 2; }
    private int rowStart() { return panelY() + (compact() ? 43 : 78); }
    private int rowHeight() { return compact() ? 24 : 32; }
    private int rowStep() { return compact() ? 28 : 38; }

    @Override
    public void render(DrawContext d, int mouseX, int mouseY, float delta) {
        renderBackground(d, mouseX, mouseY, delta);
        boolean mobile = compact();
        int x = panelX(), y = panelY(), w = panelWidth(), h = panelHeight();
        int accent = 0xFF38CFFF;

        rounded(d, x, y + 3, w, h, 12, 0x60000000);
        rounded(d, x, y, w, h, 12, 0xF00B111B);
        rounded(d, x + 1, y + 1, w - 2, mobile ? 37 : 57, 11, 0xFF142333);
        d.fill(x + 12, y + (mobile ? 36 : 56), x + w - 12, y + (mobile ? 37 : 57), 0x5538CFFF);

        d.drawText(textRenderer, "ZYVEN  /  ALERT STUDIO", x + 13, y + (mobile ? 8 : 13),
                accent, true);
        d.drawText(textRenderer, mobile ? "Payment popup settings" : "Customize incoming payment alerts",
                x + 13, y + (mobile ? 23 : 31), 0xFFA5B4C5, false);
        if (!mobile) {
            d.drawText(textRenderer, "Y  •  CLOSE", x + w - 83, y + 15, 0xFF8797A9, false);
        }

        int rx = x + 12, rw = w - 24;
        int ry = rowStart(), rh = rowHeight(), step = rowStep();
        row(d, rx, ry, rw, rh, "PAYMENT ALERTS",
                ZyvenMod.payAlertsEnabled ? "ON" : "OFF",
                ZyvenMod.payAlertsEnabled ? 0xFF5BE4A0 : 0xFFFF7481);
        row(d, rx, ry + step, rw, rh, "PARTICLES",
                ZyvenMod.payAlertParticles ? "ON" : "OFF",
                ZyvenMod.payAlertParticles ? 0xFF5BE4A0 : 0xFFFF7481);
        row(d, rx, ry + step * 2, rw, rh, "DURATION",
                ZyvenMod.payAlertDurationSeconds + " SEC", 0xFFB9D9EA);
        // This row is deliberately prominent and uses a large tap target on phones.
        row(d, rx, ry + step * 3, rw, rh, "MIN PAY",
                ZyvenMod.formatCompactAmount(ZyvenMod.payAlertMinimum) + "  ›", accent);

        int labelY = mobile ? y + 159 : y + 238;
        d.drawText(textRenderer, "ALERT COLOR", rx + 2, labelY, 0xFFE5EDF5, true);
        if (!mobile) {
            d.drawText(textRenderer, "Payer name and particles", rx + 2, labelY + 14, 0xFF8495A9, false);
        }

        int swatchSize = mobile ? 19 : 24;
        int gap = Math.max(3, (rw - 7 * swatchSize) / 6);
        int total = 7 * swatchSize + 6 * gap;
        int swatchX = x + (w - total) / 2;
        int swatchY = mobile ? y + 178 : y + 264;
        for (int i = 0; i < COLORS.length; i++) {
            int sx = swatchX + i * (swatchSize + gap);
            boolean selected = ZyvenMod.payAlertColor == COLORS[i];
            rounded(d, sx - (selected ? 2 : 0), swatchY - (selected ? 2 : 0),
                    swatchSize + (selected ? 4 : 0), swatchSize + (selected ? 4 : 0), 7,
                    selected ? 0xFFFFFFFF : 0xFF303B4A);
            rounded(d, sx + 2, swatchY + 2, swatchSize - 4, swatchSize - 4,
                    5, COLORS[i]);
        }

        if (!mobile) {
            int previewY = y + h - 39;
            rounded(d, x + 14, previewY, w - 28, 25, 7, 0xFF101A26);
            d.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("PREVIEW   Alex paid you   +1B"),
                    width / 2, previewY + 8, ZyvenMod.payAlertColor);
        } else {
            d.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Tap MIN PAY to cycle the threshold"),
                    width / 2, y + h - 16, 0xFF8194A9);
        }
        super.render(d, mouseX, mouseY, delta);
    }

    private void row(DrawContext d, int x, int y, int w, int h,
                     String label, String value, int valueColor) {
        rounded(d, x, y, w, h, 7, 0xFF172230);
        int textY = y + (h - 9) / 2;
        d.drawText(textRenderer, label, x + 9, textY, 0xFFEAF1F8, true);
        d.drawText(textRenderer, value,
                x + w - textRenderer.getWidth(value) - 9, textY, valueColor, true);
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
        int x = panelX(), y = panelY(), w = panelWidth(), h = panelHeight();
        int rx = x + 12, rw = w - 24, ry = rowStart(), rh = rowHeight(), step = rowStep();
        double mx = click.x(), my = click.y();

        for (int i = 0; i < 4; i++) {
            int rowY = ry + i * step;
            if (mx >= rx && mx <= rx + rw && my >= rowY && my <= rowY + rh) {
                switch (i) {
                    case 0 -> ZyvenMod.payAlertsEnabled = !ZyvenMod.payAlertsEnabled;
                    case 1 -> ZyvenMod.payAlertParticles = !ZyvenMod.payAlertParticles;
                    case 2 -> ZyvenMod.payAlertDurationSeconds = switch (ZyvenMod.payAlertDurationSeconds) {
                        case 3 -> 4;
                        case 4 -> 6;
                        default -> 3;
                    };
                    case 3 -> cycleMinimum();
                }
                return true;
            }
        }

        boolean mobile = compact();
        int swatchSize = mobile ? 19 : 24;
        int gap = Math.max(3, (rw - 7 * swatchSize) / 6);
        int total = 7 * swatchSize + 6 * gap;
        int swatchX = x + (w - total) / 2;
        int swatchY = mobile ? y + 178 : y + 264;
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

    private static void cycleMinimum() {
        double[] minimums = {0, 1_000, 10_000, 100_000, 1_000_000,
                10_000_000, 100_000_000, 1_000_000_000};
        int current = 0;
        for (int i = 0; i < minimums.length; i++) {
            if (minimums[i] == ZyvenMod.payAlertMinimum) {
                current = i;
                break;
            }
        }
        ZyvenMod.payAlertMinimum = minimums[(current + 1) % minimums.length];
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE || input.key() == GLFW.GLFW_KEY_Y) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() { return false; }
}
