package me.zyven.zyvenmod;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public final class PayAlertSettingsScreen extends Screen {
    private static final int[] COLORS = ZyvenMod.alertColorPresets();
    private TextFieldWidget minimumField;
    private String minimumStatus = "Type any amount: 67k, 1m, 2.5b";

    public PayAlertSettingsScreen() {
        super(Text.literal("Payment Alert Settings"));
    }

    private boolean compact() {
        return width < 600 || height < 450;
    }

    private int panelWidth() {
        return Math.max(240, Math.min(width - 28, compact() ? 340 : 440));
    }

    private int panelHeight() {
        return Math.max(170, Math.min(height - 20, compact() ? 204 : 350));
    }

    private int panelX() { return (width - panelWidth()) / 2; }
    private int panelY() { return (height - panelHeight()) / 2; }
    private int rowStart() { return panelY() + (compact() ? 39 : 68); }
    private int rowHeight() { return compact() ? 20 : 28; }
    private int rowStep() { return compact() ? 23 : 33; }
    private int minimumFieldY() { return panelY() + (compact() ? 126 : 190); }
    private int colorLabelY() { return panelY() + (compact() ? 162 : 231); }
    private int swatchY() { return panelY() + (compact() ? 178 : 251); }

    @Override
    protected void init() {
        super.init();
        int x = panelX();
        int w = panelWidth();
        boolean mobile = compact();
        int fieldX = x + 12;
        int fieldY = minimumFieldY();
        int fieldW = Math.max(100, w - (mobile ? 82 : 112));
        int fieldH = mobile ? 20 : 24;

        minimumField = new TextFieldWidget(textRenderer, fieldX, fieldY,
                fieldW, fieldH, Text.literal("Minimum payment"));
        minimumField.setMaxLength(24);
        minimumField.setTextPredicate(value -> value.matches("(?i)[0-9.,_kmbof]*"));
        minimumField.setText(ZyvenMod.payAlertMinimum == 0
                ? "off" : ZyvenMod.formatCompactAmount(ZyvenMod.payAlertMinimum).replace("+", ""));
        minimumField.setPlaceholder(Text.literal("e.g. 67k, 1m, 2.5b"));
        minimumField.setDrawsBackground(false);
        minimumField.setEditableColor(0xFFEAF7FF);
        addDrawableChild(minimumField);
    }

    @Override
    public void render(DrawContext d, int mouseX, int mouseY, float delta) {
        renderBackground(d, mouseX, mouseY, delta);
        boolean mobile = compact();
        int x = panelX(), y = panelY(), w = panelWidth(), h = panelHeight();
        int accent = 0xFF38CFFF;

        rounded(d, x, y + 3, w, h, 12, 0x60000000);
        rounded(d, x, y, w, h, 12, 0xFF38CFFF);
        rounded(d, x + 1, y + 1, w - 2, h - 2, 11, 0xF00B111B);
        rounded(d, x + 2, y + 2, w - 4, mobile ? 34 : 56, 10, 0xFF142333);
        d.fill(x + 12, y + (mobile ? 33 : 55), x + w - 12, y + (mobile ? 34 : 56), 0x5538CFFF);

        d.drawText(textRenderer, "ZYVEN  /  ALERT STUDIO", x + 13, y + (mobile ? 7 : 13),
                accent, true);
        d.drawText(textRenderer, mobile ? "Payment popup settings" : "Customize incoming payment alerts",
                x + 13, y + (mobile ? 21 : 31), 0xFFA5B4C5, false);

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

        d.drawText(textRenderer, "MINIMUM PAY", rx + 2,
                minimumFieldY() - (mobile ? 12 : 14), 0xFFE5EDF5, true);
        int buttonX = x + w - (mobile ? 60 : 88);
        int buttonY = minimumFieldY();
        int buttonW = mobile ? 48 : 76;
        rounded(d, x + 9, buttonY - 1, w - 18, mobile ? 22 : 26, 7, 0xFF101A26);
        rounded(d, buttonX, buttonY, buttonW, mobile ? 20 : 24, 6, 0xFF15536C);
        d.drawCenteredTextWithShadow(textRenderer, "SET",
                buttonX + buttonW / 2, buttonY + (mobile ? 6 : 8), 0xFFEAFBFF);

        d.drawText(textRenderer, minimumStatus, rx + 2,
                buttonY + (mobile ? 24 : 28), 0xFF7F93A9, false);

        d.drawText(textRenderer, "ALERT COLOR", rx + 2, colorLabelY(), 0xFFE5EDF5, true);
        int swatchSize = mobile ? 16 : 22;
        int gap = Math.max(3, (rw - 7 * swatchSize) / 6);
        int total = 7 * swatchSize + 6 * gap;
        int swatchX = x + (w - total) / 2;
        for (int i = 0; i < COLORS.length; i++) {
            int sx = swatchX + i * (swatchSize + gap);
            boolean selected = ZyvenMod.payAlertColor == COLORS[i];
            rounded(d, sx - (selected ? 2 : 0), swatchY() - (selected ? 2 : 0),
                    swatchSize + (selected ? 4 : 0), swatchSize + (selected ? 4 : 0), 6,
                    selected ? 0xFFFFFFFF : 0xFF303B4A);
            rounded(d, sx + 2, swatchY() + 2, swatchSize - 4, swatchSize - 4,
                    4, COLORS[i]);
        }

        int previewY = y + h - (mobile ? 28 : 31);
        int previewH = mobile ? 19 : 22;
        rounded(d, x + 14, previewY, w - 28, previewH, 7, 0xFF101A26);
        d.drawCenteredTextWithShadow(textRenderer,
                Text.literal("▶  PREVIEW ANIMATION  •  TAP TO SHOW"),
                width / 2, previewY + (mobile ? 5 : 6), ZyvenMod.payAlertColor);
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

    private void saveMinimum() {
        if (minimumField == null) return;
        String input = minimumField.getText().trim().toLowerCase(Locale.ROOT)
                .replace(",", "").replace("_", "").replace(" ", "");
        if (input.equals("off") || input.equals("0")) {
            ZyvenMod.setPayAlertMinimum(0);
            minimumStatus = "Minimum alert: OFF";
            return;
        }

        double multiplier = 1;
        if (input.endsWith("k")) {
            multiplier = 1_000;
            input = input.substring(0, input.length() - 1);
        } else if (input.endsWith("m")) {
            multiplier = 1_000_000;
            input = input.substring(0, input.length() - 1);
        } else if (input.endsWith("b")) {
            multiplier = 1_000_000_000;
            input = input.substring(0, input.length() - 1);
        }

        try {
            double amount = Double.parseDouble(input) * multiplier;
            if (!Double.isFinite(amount) || amount < 0) throw new NumberFormatException();
            ZyvenMod.setPayAlertMinimum(amount);
            minimumStatus = "Saved minimum: " + ZyvenMod.formatCompactAmount(amount);
        } catch (NumberFormatException ignored) {
            minimumStatus = "Invalid amount. Try 67k, 1m or 2.5b";
        }
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
        int x = panelX(), y = panelY(), w = panelWidth();
        int rx = x + 12, rw = w - 24;
        int ry = rowStart(), rh = rowHeight(), step = rowStep();
        double mx = click.x(), my = click.y();

        if (mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh) {
            ZyvenMod.payAlertsEnabled = !ZyvenMod.payAlertsEnabled;
            return true;
        }
        if (mx >= rx && mx <= rx + rw && my >= ry + step && my <= ry + step + rh) {
            ZyvenMod.payAlertParticles = !ZyvenMod.payAlertParticles;
            return true;
        }
        if (mx >= rx && mx <= rx + rw && my >= ry + step * 2 && my <= ry + step * 2 + rh) {
            ZyvenMod.payAlertDurationSeconds = switch (ZyvenMod.payAlertDurationSeconds) {
                case 3 -> 4;
                case 4 -> 6;
                default -> 3;
            };
            return true;
        }

        int buttonX = x + w - (compact() ? 60 : 88);
        int buttonY = minimumFieldY();
        int buttonW = compact() ? 48 : 76;
        int buttonH = compact() ? 20 : 24;
        if (mx >= buttonX && mx <= buttonX + buttonW
                && my >= buttonY && my <= buttonY + buttonH) {
            saveMinimum();
            return true;
        }

        int previewY = y + h - (compact() ? 28 : 31);
        int previewH = compact() ? 19 : 22;
        if (mx >= x + 14 && mx <= x + w - 14
                && my >= previewY && my <= previewY + previewH) {
            ZyvenMod.showPaymentAlert("Alex", "+1B");
            if (client != null) client.setScreen(null);
            return true;
        }

        int swatchSize = compact() ? 16 : 22;
        int gap = Math.max(3, (rw - 7 * swatchSize) / 6);
        int total = 7 * swatchSize + 6 * gap;
        int swatchX = x + (w - total) / 2;
        if (my >= swatchY() - 4 && my <= swatchY() + swatchSize + 4) {
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
        if (input.key() == GLFW.GLFW_KEY_ENTER || input.key() == GLFW.GLFW_KEY_KP_ENTER) {
            saveMinimum();
            return true;
        }
        if (input.key() == GLFW.GLFW_KEY_ESCAPE || input.key() == GLFW.GLFW_KEY_Y) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() { return false; }
}
