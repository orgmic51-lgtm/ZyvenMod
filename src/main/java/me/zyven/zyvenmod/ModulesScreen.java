package me.zyven.zyvenmod;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ModulesScreen extends Screen {
    private int category = 0;
    private double scroll = 0;
    private String search = "";
    private boolean typingSearch = false;
    private boolean draggingList = false;

    private record Module(String name, String description, int category, int id) {}

    private static final String[] CATEGORIES = {"All", "HUD", "Visuals", "Client"};
    private static final Module[] MODULES = {
            new Module("Profit HUD", "Shows your running net profit", 1, 0),
            new Module("FPS HUD", "Shows your current frames per second", 1, 7),
            new Module("Fake Items", "Client-side inventory icon disguise", 3, 1),
            new Module("Viewmodel", "Customize held-item position and scale", 2, 2),
            new Module("Custom Sky", "Use a personal sky appearance", 2, 3),
            new Module("Hats", "Cosmetic hat accessories for your character", 2, 4),
            new Module("Crosshair Style", "Customize the look of your crosshair", 2, 5),
            new Module("Clean HUD", "Hide selected non-essential HUD elements", 1, 6)
    };

    public ModulesScreen() { super(Text.literal("ZyvenMod Modules")); }

    private boolean enabled(int id) {
        return switch (id) {
            case 0 -> ZyvenMod.showProfit;
            case 1 -> ZyvenMod.fakeItemsEnabled;
            case 2 -> ZyvenMod.viewmodelEnabled;
            case 3 -> ZyvenMod.customSkyEnabled;
            case 4 -> ZyvenMod.hatsEnabled;
            case 5 -> ZyvenMod.crosshairStyleEnabled;
            case 6 -> ZyvenMod.cleanHudEnabled;
            case 7 -> ZyvenMod.showFps;
            default -> false;
        };
    }

    private void toggle(int id) {
        switch (id) {
            case 0 -> ZyvenMod.showProfit = !ZyvenMod.showProfit;
            case 1 -> ZyvenMod.fakeItemsEnabled = !ZyvenMod.fakeItemsEnabled;
            case 2 -> ZyvenMod.viewmodelEnabled = !ZyvenMod.viewmodelEnabled;
            case 3 -> ZyvenMod.customSkyEnabled = !ZyvenMod.customSkyEnabled;
            case 4 -> ZyvenMod.hatsEnabled = !ZyvenMod.hatsEnabled;
            case 5 -> ZyvenMod.crosshairStyleEnabled = !ZyvenMod.crosshairStyleEnabled;
            case 6 -> ZyvenMod.cleanHudEnabled = !ZyvenMod.cleanHudEnabled;
            case 7 -> ZyvenMod.showFps = !ZyvenMod.showFps;
        }
    }

    private List<Module> visibleModules() {
        List<Module> out = new ArrayList<>();
        for (Module m : MODULES) {
            if (category != 0 && m.category() != category) continue;
            if (!search.isBlank() && !m.name().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))) continue;
            out.add(m);
        }
        return out;
    }

    @Override
    public void render(DrawContext d, int mouseX, int mouseY, float delta) {
        renderBackground(d, mouseX, mouseY, delta);
        boolean compact = width < 620;
        int panelW = Math.min(width - 16, compact ? 520 : 980);
        int panelH = Math.min(height - 16, compact ? 620 : 560);
        int left = (width - panelW) / 2, top = (height - panelH) / 2;
        fill(d, left, top, left + panelW, top + panelH, 0xF00D1019);
        fill(d, left, top, left + panelW, top + 3, 0xFFB98AFF);
        fill(d, left, top + 42, left + panelW, top + 43, 0x553D4658);

        d.drawText(textRenderer, "ZYVEN", left + 18, top + 14, 0xFFB98AFF, false);
        d.drawText(textRenderer, "CLIENT", left + 65, top + 14, 0xFFF3F0FA, false);
        d.drawText(textRenderer, compact ? "TOUCH / ESC TO CLOSE" : "RIGHT SHIFT  •  CLOSE",
                left + panelW - (compact ? 145 : 170), top + 16, 0xFF8993A7, false);

        int contentTop = top + 54;
        int sideW = compact ? 0 : 150;
        if (compact) {
            int tabW = (panelW - 24) / CATEGORIES.length;
            for (int i = 0; i < CATEGORIES.length; i++) {
                int tx = left + 12 + i * tabW;
                if (category == i) fill(d, tx, contentTop, tx + tabW - 4, contentTop + 27, 0xFF49345F);
                d.drawCenteredTextWithShadow(textRenderer, CATEGORIES[i], tx + (tabW - 4) / 2,
                        contentTop + 9, category == i ? 0xFFFFFFFF : 0xFFB9B5C5);
            }
            contentTop += 38;
        } else {
            fill(d, left + 10, contentTop, left + sideW - 4, top + panelH - 14, 0xAA141923);
            for (int i = 0; i < CATEGORIES.length; i++) {
                int cy = contentTop + 12 + i * 37;
                if (category == i) fill(d, left + 17, cy - 5, left + sideW - 12, cy + 22, 0xFF49345F);
                d.drawText(textRenderer, CATEGORIES[i], left + 31, cy + 2,
                        category == i ? 0xFFD9B9FF : 0xFFB8C0CF, false);
            }
        }

        int mainX = left + (compact ? 10 : sideW + 8);
        int mainW = panelW - (compact ? 20 : sideW + 18);
        int searchY = contentTop;
        fill(d, mainX, searchY, mainX + mainW, searchY + 29, 0xFF171D29);
        d.drawText(textRenderer, "⌕  " + (search.isEmpty() ? "Search modules..." : search)
                + (typingSearch ? "_" : ""), mainX + 11, searchY + 10, 0xFF9CA8BA, false);

        int listY = searchY + 39;
        int bottom = top + panelH - 17;
        int listH = Math.max(20, bottom - listY);
        fill(d, mainX, listY, mainX + mainW, bottom, 0x55101520);

        List<Module> shown = visibleModules();
        int columns = compact ? 1 : 2;
        int gap = 8;
        int cardW = (mainW - gap * (columns - 1)) / columns;
        int cardH = 48;
        int rows = (shown.size() + columns - 1) / columns;
        double maxScroll = Math.max(0, rows * (cardH + 7) - listH);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        d.enableScissor(mainX, listY, mainX + mainW, bottom);
        for (int i = 0; i < shown.size(); i++) {
            Module m = shown.get(i);
            int col = i % columns, row = i / columns;
            int x = mainX + col * (cardW + gap);
            int y = listY + row * (cardH + 7) - (int) scroll;
            boolean on = enabled(m.id());
            boolean hovered = mouseX >= x && mouseX <= x + cardW && mouseY >= y && mouseY <= y + cardH;
            fill(d, x, y, x + cardW, y + cardH, on ? 0xFF292238 : hovered ? 0xFF252D3B : 0xFF1B222E);
            if (on) fill(d, x, y, x + 2, y + cardH, 0xFFB98AFF);
            d.drawText(textRenderer, m.name(), x + 10, y + 8, 0xFFF1EFF8, false);
            String desc = m.description();
            int maxChars = Math.max(12, (cardW - 62) / 5);
            if (desc.length() > maxChars) desc = desc.substring(0, maxChars - 1) + "…";
            d.drawText(textRenderer, desc, x + 10, y + 27, 0xFF929DB0, false);
            int sx = x + cardW - 39;
            fill(d, sx, y + 15, sx + 29, y + 32, on ? 0xFF7956A3 : 0xFF3A4352);
            fill(d, on ? sx + 15 : sx + 2, y + 17, on ? sx + 27 : sx + 14, y + 30, 0xFFECE7F5);
        }
        d.disableScissor();

        if (maxScroll > 0) {
            int trackX = mainX + mainW - 3;
            fill(d, trackX, listY, trackX + 2, bottom, 0xFF303746);
            int thumbH = Math.max(18, (int)(listH * listH / (rows * (cardH + 7))));
            int thumbY = listY + (int)((listH - thumbH) * (scroll / maxScroll));
            fill(d, trackX, thumbY, trackX + 2, thumbY + thumbH, 0xFFB98AFF);
        }
        super.render(d, mouseX, mouseY, delta);
    }

    private void fill(DrawContext d, int x1, int y1, int x2, int y2, int color) {
        d.fill(x1, y1, x2, y2, color);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        boolean compact = width < 620;
        int panelW = Math.min(width - 16, compact ? 520 : 980);
        int panelH = Math.min(height - 16, compact ? 620 : 560);
        int left = (width - panelW) / 2, top = (height - panelH) / 2;
        int contentTop = top + 54;
        int sideW = compact ? 0 : 150;
        if (compact) {
            int tabW = (panelW - 24) / CATEGORIES.length;
            if (my >= contentTop && my <= contentTop + 29) {
                int index = (int)((mx - left - 12) / tabW);
                if (index >= 0 && index < CATEGORIES.length) { category = index; scroll = 0; }
                return true;
            }
            contentTop += 38;
        } else {
            for (int i = 0; i < CATEGORIES.length; i++) {
                int cy = contentTop + 12 + i * 37;
                if (mx >= left + 17 && mx <= left + sideW - 12 && my >= cy - 5 && my <= cy + 22) {
                    category = i; scroll = 0; return true;
                }
            }
        }
        int mainX = left + (compact ? 10 : sideW + 8);
        int mainW = panelW - (compact ? 20 : sideW + 18);
        int searchY = contentTop;
        if (mx >= mainX && mx <= mainX + mainW && my >= searchY && my <= searchY + 29) {
            typingSearch = true;
            return true;
        }
        typingSearch = false;
        int listY = searchY + 39, bottom = top + panelH - 17;
        int listH = Math.max(20, bottom - listY);
        if (mx < mainX || mx > mainX + mainW || my < listY || my > bottom) return super.mouseClicked(click, doubled);
        draggingList = true;
        List<Module> shown = visibleModules();
        int columns = compact ? 1 : 2, gap = 8, cardW = (mainW - gap * (columns - 1)) / columns, cardH = 48;
        for (int i = 0; i < shown.size(); i++) {
            int col = i % columns, row = i / columns;
            int x = mainX + col * (cardW + gap), y = listY + row * (cardH + 7) - (int)scroll;
            if (mx >= x && mx <= x + cardW && my >= y && my <= y + cardH) {
                if (click.button() == 1) {
                    if (client != null) client.setScreen(new ModuleSettingsScreen(shown.get(i).id(), shown.get(i).name()));
                } else {
                    toggle(shown.get(i).id());
                }
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (draggingList) {
            scroll -= deltaY;
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        draggingList = false;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll -= verticalAmount * 32;
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close(); return true;
        }
        if (typingSearch && key == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1); scroll = 0; return true;
        }
        if (typingSearch && key == GLFW.GLFW_KEY_ENTER) { typingSearch = false; return true; }
        if (typingSearch && key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) {
            search += (char)('a' + key - GLFW.GLFW_KEY_A); scroll = 0; return true;
        }
        return super.keyPressed(input);
    }

    @Override public boolean shouldPause() { return false; }
}
