package me.zyven.zyvenmod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class ModulesScreen extends Screen {
    private int category = 0;

    public ModulesScreen() { super(Text.literal("ZyvenMod Modules")); }

    @Override
    public void render(DrawContext d, int mouseX, int mouseY, float delta) {
        renderBackground(d, mouseX, mouseY, delta);
        int cx = width / 2, cy = height / 2;
        int left = cx - 190, top = cy - 125;
        panel(d, left, top, left + 380, top + 250, 0xF0141822);
        d.drawText(textRenderer, "ZYVEN", left + 18, top + 15, 0xFF63C8FF, false);
        d.drawText(textRenderer, "MODULES", left + 72, top + 15, 0xFFFFFFFF, false);
        d.drawText(textRenderer, "RIGHT SHIFT  •  CLOSE", left + 225, top + 17, 0xFF8994A8, false);
        panel(d, left + 14, top + 42, left + 112, top + 228, 0xFF10151E);
        String[] cats = {"All", "HUD", "Visuals", "Utility"};
        for (int i = 0; i < cats.length; i++) {
            int y = top + 54 + i * 30;
            if (category == i) panel(d, left + 21, y - 4, left + 105, y + 18, 0xFF244D69);
            d.drawText(textRenderer, cats[i], left + 31, y, category == i ? 0xFF8DDCFF : 0xFFBEC6D4, false);
        }
        panel(d, left + 125, top + 42, left + 366, top + 70, 0xFF10151E);
        d.drawText(textRenderer, "Search modules...", left + 137, top + 52, 0xFF758195, false);
        card(d, left + 125, top + 82, "Profit HUD", "Shows your running net profit", ZyvenMod.showProfit);
        card(d, left + 125, top + 128, "Fake Items", "Disguises named End Stone visually", ZyvenMod.fakeItemsEnabled);
        d.drawText(textRenderer, "Fake Items only changes the client-side icon.",
                left + 126, top + 204, 0xFF7F8A9D, false);
        super.render(d, mouseX, mouseY, delta);
    }

    private void card(DrawContext d, int x, int y, String name, String desc, boolean enabled) {
        panel(d, x, y, x + 241, y + 38, 0xFF1B2330);
        d.drawText(textRenderer, name, x + 10, y + 7, 0xFFF1F5FA, false);
        d.drawText(textRenderer, desc, x + 10, y + 21, 0xFF8995A8, false);
        panel(d, x + 205, y + 10, x + 231, y + 27, enabled ? 0xFF287D68 : 0xFF394353);
        d.drawText(textRenderer, enabled ? "ON" : "OFF", x + 209, y + 14, 0xFFFFFFFF, false);
    }

    private void panel(DrawContext d, int x1, int y1, int x2, int y2, int color) {
        d.fill(x1, y1, x2, y2, color);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int cx = width / 2, cy = height / 2, left = cx - 190, top = cy - 125;
        for (int i = 0; i < 4; i++) {
            int y = top + 50 + i * 30;
            if (mx >= left + 20 && mx <= left + 110 && my >= y && my <= y + 24) category = i;
        }
        if (mx >= left + 125 && mx <= left + 366 && my >= top + 82 && my <= top + 120)
            ZyvenMod.showProfit = !ZyvenMod.showProfit;
        if (mx >= left + 125 && mx <= left + 366 && my >= top + 128 && my <= top + 166)
            ZyvenMod.fakeItemsEnabled = !ZyvenMod.fakeItemsEnabled;
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close(); return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public boolean shouldPause() { return false; }
}
