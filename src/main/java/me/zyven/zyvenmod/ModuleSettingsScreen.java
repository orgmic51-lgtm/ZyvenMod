package me.zyven.zyvenmod;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class ModuleSettingsScreen extends Screen {
    private final int moduleId;
    private final String moduleName;
    private boolean offhandTab;
    private static final String[] HAT_STYLES = {"Santa", "Beanie", "Top Hat", "Crown", "Horns", "Frost Beanie", "Ruby Top Hat", "Emerald Horns"};
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
            case 11 -> ZyvenMod.zyNameBadgeEnabled;
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
            case 11 -> ZyvenMod.zyNameBadgeEnabled = value;
        }
        ZyvenMod.saveUserSettings();
    }

    private void drawAdjustRow(DrawContext d, int x, int y, int w, String label, String value) {
        d.fill(x, y, x + w, y + 27, 0xFF1B2838);
        d.fill(x + 1, y + 1, x + w - 1, y + 2, 0x2238CFFF);
        d.drawText(textRenderer, label, x + 9, y + 9, 0xFFE5EFF8, true);
        int minusX = x + w - 62;
        int plusX = x + w - 29;
        d.fill(minusX, y + 4, minusX + 24, y + 23, 0xFF263B50);
        d.fill(plusX, y + 4, plusX + 24, y + 23, 0xFF15536C);
        d.drawCenteredTextWithShadow(textRenderer, "−", minusX + 12, y + 9, 0xFFEAF7FF);
        d.drawCenteredTextWithShadow(textRenderer, "+", plusX + 12, y + 9, 0xFFEAFBFF);
        d.drawCenteredTextWithShadow(textRenderer, value, x + w - 78, y + 9, 0xFF38CFFF);
    }

    private void drawOpacityRow(DrawContext d, int x, int y, int panelW, int opacity) {
        d.fill(x, y, x + panelW - 32, y + 30, 0xFF1B2838);
        d.drawText(textRenderer, "HUD opacity", x + 10, y + 11, 0xFFE5EFF8, false);
        int minusX = x + panelW - 100;
        int plusX = x + panelW - 44;
        d.fill(minusX, y + 5, minusX + 24, y + 25, 0xFF263B50);
        d.fill(plusX, y + 5, plusX + 24, y + 25, 0xFF15536C);
        d.drawCenteredTextWithShadow(textRenderer, "−", minusX + 12, y + 11, 0xFFEAF7FF);
        d.drawCenteredTextWithShadow(textRenderer, opacity + "%", minusX + 40, y + 11, 0xFF38CFFF);
        d.drawCenteredTextWithShadow(textRenderer, "+", plusX + 12, y + 11, 0xFFEAFBFF);
    }

    @Override
    public void render(DrawContext d, int mouseX, int mouseY, float delta) {
        renderBackground(d, mouseX, mouseY, delta);
        int panelW = Math.min(340, width - 24);
        int panelH = moduleId == 2 ? 270 : 190;
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
            int opacity = moduleId == 0 ? ZyvenMod.profitHudOpacity : ZyvenMod.fpsHudOpacity;
            drawOpacityRow(d, rowX, rowY, panelW, opacity);
        } else if (hasViewmodelSettings) {
            int tabY = y + 82;
            d.fill(rowX, tabY, x + panelW - 16, tabY + 25, 0xFF172333);
            d.fill(rowX + (offhandTab ? (panelW - 32) / 2 : 0), tabY,
                    rowX + (offhandTab ? panelW - 32 : (panelW - 32) / 2), tabY + 25, 0xFF15536C);
            d.drawCenteredTextWithShadow(textRenderer, "MAIN HAND",
                    rowX + (panelW - 32) / 4, tabY + 8, offhandTab ? 0xFFAAB9C8 : 0xFFFFFFFF);
            d.drawCenteredTextWithShadow(textRenderer, "OFFHAND",
                    rowX + (panelW - 32) * 3 / 4, tabY + 8, offhandTab ? 0xFFFFFFFF : 0xFFAAB9C8);
            float vx = offhandTab ? ZyvenMod.offhandViewmodelX : ZyvenMod.viewmodelX;
            float vy = offhandTab ? ZyvenMod.offhandViewmodelY : ZyvenMod.viewmodelY;
            float vz = offhandTab ? ZyvenMod.offhandViewmodelZ : ZyvenMod.viewmodelZ;
            float vs = offhandTab ? ZyvenMod.offhandViewmodelScale : ZyvenMod.viewmodelScale;
            drawAdjustRow(d, rowX, y + 114, panelW - 32, "X OFFSET", String.format("%.2f", vx));
            drawAdjustRow(d, rowX, y + 146, panelW - 32, "Y OFFSET", String.format("%.2f", vy));
            drawAdjustRow(d, rowX, y + 178, panelW - 32, "Z OFFSET", String.format("%.2f", vz));
            drawAdjustRow(d, rowX, y + 210, panelW - 32, "SCALE", String.format("%.2f", vs));
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
        } else if (moduleId == 11) {
            d.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Z  WHITE   •   Y  CYAN"), width / 2, y + 104, 0xFF38CFFF);
            d.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Shown before player names in TAB, chat and nametags"),
                    width / 2, y + 119, 0xFF9BA8BA);
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
        if (!hasViewmodelSettings) {
            d.drawCenteredTextWithShadow(textRenderer, Text.literal("Tap a setting to change it  •  ESC to return"),
                    width / 2, y + panelH - 24, 0xFF9BA8BA);
        }
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
            if (click.y() >= y + 82 && click.y() <= y + 107) {
                offhandTab = click.x() >= width / 2.0;
                return true;
            }
            int row = (int) ((click.y() - (y + 114)) / 32.0);
            if (row >= 0 && row < 4 && click.y() <= y + 237) {
                int minusX = rowX + panelW - 32 - 62;
                int plusX = rowX + panelW - 32 - 29;
                float direction;
                if (click.x() >= minusX && click.x() <= minusX + 24) direction = -1.0f;
                else if (click.x() >= plusX && click.x() <= plusX + 24) direction = 1.0f;
                else return true;
                float step = row == 3 ? 0.05f : 0.02f;
                float value;
                if (offhandTab) {
                    value = switch (row) {
                        case 0 -> ZyvenMod.offhandViewmodelX + direction * step;
                        case 1 -> ZyvenMod.offhandViewmodelY + direction * step;
                        case 2 -> ZyvenMod.offhandViewmodelZ + direction * step;
                        default -> ZyvenMod.offhandViewmodelScale + direction * step;
                    };
                    ZyvenMod.setOffhandViewmodelValue(row, value);
                } else {
                    value = switch (row) {
                        case 0 -> ZyvenMod.viewmodelX + direction * step;
                        case 1 -> ZyvenMod.viewmodelY + direction * step;
                        case 2 -> ZyvenMod.viewmodelZ + direction * step;
                        default -> ZyvenMod.viewmodelScale + direction * step;
                    };
                    ZyvenMod.setViewmodelValue(row, value);
                }
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
                && click.y() >= rowY + 76 && click.y() <= rowY + 106) {
            int current = moduleId == 0 ? ZyvenMod.profitHudOpacity : ZyvenMod.fpsHudOpacity;
            if (click.x() >= x + panelW - 100 && click.x() <= x + panelW - 76) {
                current -= 10;
            } else if (click.x() >= x + panelW - 44 && click.x() <= x + panelW - 20) {
                current += 10;
            } else return true;
            if (moduleId == 0) ZyvenMod.setProfitHudOpacity(current);
            else ZyvenMod.setFpsHudOpacity(current);
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
