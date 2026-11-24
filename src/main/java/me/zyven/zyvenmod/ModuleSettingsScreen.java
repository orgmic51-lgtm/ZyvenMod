package me.zyven.zyvenmod;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class ModuleSettingsScreen extends Screen {
    private final int moduleId;
    private final String moduleName;
    private static final String[] HAT_STYLES = {"Santa", "Beanie", "Top Hat", "Crown", "Horns"};
    private static final int[] SKY_COLORS = {0xFF399CFF, 0xFF28D7C2, 0xFF9B6DFF, 0xFFFF79B8, 0xFFFFB84D, 0xFFEBF5FF};

    public ModuleSettingsScreen(int moduleId, String moduleName) {
        super(Text.literal(moduleName + " Settings"));
        this.moduleId = moduleId;
        this.moduleName = moduleName;
    }

    private boolean enabled() {
        return switch (moduleId) {
            case 0 -> ZyvenMod.showProfit;
            case 1 -> ZyvenMod.fakeItemsEnabled;
            case 2 -> ZyvenMod.viewmodelEnabled;
            case 3 -> ZyvenMod.customSkyEnabled;
            case 4 -> ZyvenMod.hatsEnabled;
            case 5 -> ZyvenMod.crosshairStyleEnabled;
            case 6 -> ZyvenMod.cleanHudEnabled;
            case 7 -> ZyvenMod.showFps;
            case 9 -> ZyvenMod.zyvenCapeEnabled;
            default -> false;
        };
    }

    private void setEnabled(boolean value) {
        switch (moduleId) {
            case 0 -> ZyvenMod.showProfit = value;
            case 1 -> ZyvenMod.fakeItemsEnabled = value;
            case 2 -> ZyvenMod.viewmodelEnabled = value;
            case 3 -> ZyvenMod.customSkyEnabled = value;
            case 4 -> ZyvenMod.hatsEnabled = value;
            case 5 -> ZyvenMod.crosshairStyleEnabled = value;
            case 6 -> ZyvenMod.cleanHudEnabled = value;
            case 7 -> ZyvenMod.showFps = value;
            case 9 -> ZyvenMod.zyvenCapeEnabled = value;
        }
        ZyvenMod.saveUserSettings();
    }

    private void drawAdjustRow(DrawContext d, int x, int y, int w, String label, String value) {
        d.fill(x, y, x + w, y + 27, 0xFF202A38);
        d.drawText(textRenderer, label, x + 9, y + 9, 0xFFF1F5FA, true);
        String control = "−   " + value + "   +";
        d.drawText(textRenderer, control,
                x + w - textRenderer.getWidth(control) - 9, y + 9, 0xFF38CFFF, true);
    }

    @Override
    public void render(DrawContext d, int mouseX, int mouseY, float delta) {
        renderBackground(d, mouseX, mouseY, delta);
        int panelW = Math.min(340, width - 24);
        int panelH = moduleId == 2 ? 250 : 190;
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;
        d.fill(x, y, x + panelW, y + panelH, 0xF00D1019);
        d.fill(x, y, x + panelW, y + 3, 0xFF25C8F2);
        d.drawCenteredTextWithShadow(textRenderer, Text.literal(moduleName + " Settings"),
                width / 2, y + 15, 0xFFFFFFFF);

        int rowX = x + 16;
        int rowY = y + 48;
        d.fill(rowX, rowY, x + panelW - 16, rowY + 30, 0xFF202A38);
        d.drawText(textRenderer, "Enabled", rowX + 10, rowY + 11, 0xFFF1F5FA, false);
        String state = enabled() ? "ON" : "OFF";
        int color = enabled() ? 0xFF42D98B : 0xFFFF6572;
        d.drawText(textRenderer, state, x + panelW - 48, rowY + 11, color, false);

        boolean hasHudSettings = moduleId == 0 || moduleId == 7;
        boolean hasViewmodelSettings = moduleId == 2;
        boolean hasCapeSettings = moduleId == 9;
        boolean hasHatSettings = moduleId == 4;
        boolean hasSkySettings = moduleId == 3;
        if (hasHudSettings) {
            rowY += 38;
            d.fill(rowX, rowY, x + panelW - 16, rowY + 30, 0xFF202A38);
            d.drawText(textRenderer, "Panel background", rowX + 10, rowY + 11, 0xFFF1F5FA, false);
            d.drawText(textRenderer, ZyvenMod.hudBackground ? "ON" : "OFF",
                    x + panelW - 48, rowY + 11,
                    ZyvenMod.hudBackground ? 0xFF42D98B : 0xFFFF6572, false);
            rowY += 38;
            d.fill(rowX, rowY, x + panelW - 16, rowY + 30, 0xFF202A38);
            d.drawText(textRenderer, "HUD opacity", rowX + 10, rowY + 11, 0xFFF1F5FA, false);
            int opacity = moduleId == 0 ? ZyvenMod.profitHudOpacity : ZyvenMod.fpsHudOpacity;
            d.drawText(textRenderer, opacity + "%  ◀ ▶", x + panelW - 91, rowY + 11, 0xFF38CFFF, false);
        } else if (hasViewmodelSettings) {
            drawAdjustRow(d, rowX, y + 88, panelW - 32, "X OFFSET", String.format("%.2f", ZyvenMod.viewmodelX));
            drawAdjustRow(d, rowX, y + 121, panelW - 32, "Y OFFSET", String.format("%.2f", ZyvenMod.viewmodelY));
            drawAdjustRow(d, rowX, y + 154, panelW - 32, "Z OFFSET", String.format("%.2f", ZyvenMod.viewmodelZ));
            drawAdjustRow(d, rowX, y + 187, panelW - 32, "SCALE", String.format("%.2f", ZyvenMod.viewmodelScale));
        } else if (hasSkySettings) {
            d.fill(rowX, y + 88, x + panelW - 16, y + 132, 0xFF202A38);
            d.drawText(textRenderer, "SKY TINT", rowX + 10, y + 96, 0xFFF1F5FA, true);
            d.drawText(textRenderer, "Tap a color", rowX + 10, y + 111, 0xFF9BA8BA, false);
            int swatch = 18;
            int gap = 7;
            int total = SKY_COLORS.length * swatch + (SKY_COLORS.length - 1) * gap;
            int sx = x + (panelW - total) / 2;
            for (int i = 0; i < SKY_COLORS.length; i++) {
                int xx = sx + i * (swatch + gap);
                d.fill(xx - 2, y + 119, xx + swatch + 2, y + 139, 
                        ZyvenMod.customSkyColor == SKY_COLORS[i] ? 0xFFFFFFFF : 0xFF566273);
                d.fill(xx, y + 121, xx + swatch, y + 137, SKY_COLORS[i]);
            }
        } else if (hasHatSettings) {
            d.fill(rowX, y + 88, x + panelW - 16, y + 132, 0xFF202A38);
            d.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("HAT STYLE  •  " + HAT_STYLES[Math.floorMod(ZyvenMod.hatStyle, HAT_STYLES.length)]),
                    width / 2, y + 98, 0xFF38CFFF);
            d.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Tap here to cycle your cosmetic"),
                    width / 2, y + 115, 0xFFB6C3D1);
        } else if (hasCapeSettings) {
            d.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Texture: zyvenclient"),
                    width / 2, y + 104, 0xFF38CFFF);
            d.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Local cosmetic cape preview"),
                    width / 2, y + 119, 0xFF9BA8BA);
        } else {
            d.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("No extra settings are available for this module yet."),
                    width / 2, y + 104, 0xFF9BA8BA);
        }
        d.drawCenteredTextWithShadow(textRenderer, Text.literal("Tap a setting to change it  •  ESC to return"),
                width / 2, y + panelH - 24, 0xFF9BA8BA);
        super.render(d, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int panelW = Math.min(340, width - 24);
        int panelH = moduleId == 2 ? 250 : 190;
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;
        int rowX = x + 16;
        int rowY = y + 48;
        if (click.x() >= rowX && click.x() <= x + panelW - 16
                && click.y() >= rowY && click.y() <= rowY + 30) {
            setEnabled(!enabled());
            return true;
        }
        if (moduleId == 2 && click.x() >= rowX && click.x() <= x + panelW - 16) {
            int row = (int) ((click.y() - (y + 88)) / 33.0);
            if (row >= 0 && row < 4 && click.y() <= y + 214) {
                float step = row == 3 ? 0.05f : 0.02f;
                float direction = click.x() >= width / 2.0 ? step : -step;
                float value = switch (row) {
                    case 0 -> ZyvenMod.viewmodelX + direction;
                    case 1 -> ZyvenMod.viewmodelY + direction;
                    case 2 -> ZyvenMod.viewmodelZ + direction;
                    default -> ZyvenMod.viewmodelScale + direction;
                };
                ZyvenMod.setViewmodelValue(row, value);
                return true;
            }
        }
        if (moduleId == 3 && click.y() >= y + 116 && click.y() <= y + 142) {
            int swatch = 18, gap = 7;
            int total = SKY_COLORS.length * swatch + (SKY_COLORS.length - 1) * gap;
            int sx = x + (panelW - total) / 2;
            for (int i = 0; i < SKY_COLORS.length; i++) {
                int xx = sx + i * (swatch + gap);
                if (click.x() >= xx - 2 && click.x() <= xx + swatch + 2) {
                    ZyvenMod.setCustomSkyColor(SKY_COLORS[i]);
                    return true;
                }
            }
        }
        if (moduleId == 4 && click.x() >= rowX && click.x() <= x + panelW - 16
                && click.y() >= y + 88 && click.y() <= y + 132) {
            ZyvenMod.setHatStyle(ZyvenMod.hatStyle + 1);
            return true;
        }
        if ((moduleId == 0 || moduleId == 7)
                && click.x() >= rowX && click.x() <= x + panelW - 16
                && click.y() >= rowY + 38 && click.y() <= rowY + 68) {
            ZyvenMod.hudBackground = !ZyvenMod.hudBackground;
            ZyvenMod.saveUserSettings();
            return true;
        }
        if ((moduleId == 0 || moduleId == 7)
                && click.x() >= rowX && click.x() <= x + panelW - 16
                && click.y() >= rowY + 76 && click.y() <= rowY + 106) {
            boolean increase = click.x() >= x + panelW / 2.0;
            int current = moduleId == 0 ? ZyvenMod.profitHudOpacity : ZyvenMod.fpsHudOpacity;
            int next = Math.max(0, Math.min(100, current + (increase ? 10 : -10)));
            if (moduleId == 0) ZyvenMod.setProfitHudOpacity(next);
            else ZyvenMod.setFpsHudOpacity(next);
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            if (client != null) client.setScreen(new ModulesScreen());
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() { return false; }
}
