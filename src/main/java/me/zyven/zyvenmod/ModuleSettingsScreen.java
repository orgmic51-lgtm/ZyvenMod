package me.zyven.zyvenmod;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class ModuleSettingsScreen extends Screen {
    private final int moduleId;
    private final String moduleName;

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
    }

    @Override
    public void render(DrawContext d, int mouseX, int mouseY, float delta) {
        renderBackground(d, mouseX, mouseY, delta);
        int panelW = Math.min(340, width - 24);
        int panelH = 190;
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
        boolean hasCapeSettings = moduleId == 9;
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
            d.drawText(textRenderer, ZyvenMod.hudOpacity + "%  ◀ ▶", x + panelW - 91, rowY + 11, 0xFF38CFFF, false);
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
        int panelH = 190;
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;
        int rowX = x + 16;
        int rowY = y + 48;
        if (click.x() >= rowX && click.x() <= x + panelW - 16
                && click.y() >= rowY && click.y() <= rowY + 30) {
            setEnabled(!enabled());
            return true;
        }
        if ((moduleId == 0 || moduleId == 7)
                && click.x() >= rowX && click.x() <= x + panelW - 16
                && click.y() >= rowY + 38 && click.y() <= rowY + 68) {
            ZyvenMod.hudBackground = !ZyvenMod.hudBackground;
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
