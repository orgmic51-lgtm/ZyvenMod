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
    private int category;
    private double scroll;
    private String search = "";
    private boolean typingSearch;
    private boolean pointerDown;
    private boolean draggingList;
    private boolean pressedSettings;
    private int pressedModuleId = -1;
    private double pressX;
    private double pressY;

    private record Module(String name, String description, int category, int id) {}

    private static final String[] CATEGORIES = {"ALL", "HUD", "VISUALS", "CLIENT"};
    private static final Module[] MODULES = {
            new Module("Profit HUD", "Track money received and sent", 1, 0),
            new Module("FPS Counter", "Live frames per second overlay", 1, 7),
            new Module("Payment Alerts", "Animated player payment notifications", 1, 8),
            new Module("Fake Items", "Client-side inventory icon disguise", 3, 1),
            new Module("Viewmodel", "Adjust the held item appearance", 2, 2),
            new Module("Custom Sky", "Personalize the sky appearance", 2, 3),
            new Module("Hats", "Christmas hat cosmetic", 2, 4),
            new Module("ZyvenClient Cape", "Christmas cape using your server icon", 2, 9),
            new Module("Crosshair Style", "Customize your crosshair", 2, 5),
            new Module("Clean HUD", "Hide selected non-essential elements", 1, 6)
    };

    public ModulesScreen() { super(Text.literal("Zyven Client")); }

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
            case 8 -> ZyvenMod.payAlertsEnabled;
            case 9 -> ZyvenMod.zyvenCapeEnabled;
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
            case 8 -> ZyvenMod.payAlertsEnabled = !ZyvenMod.payAlertsEnabled;
            case 9 -> ZyvenMod.zyvenCapeEnabled = !ZyvenMod.zyvenCapeEnabled;
        }
    }

    private List<Module> visibleModules() {
        List<Module> result = new ArrayList<>();
        for (Module module : MODULES) {
            if (category != 0 && module.category() != category) continue;
            if (!search.isBlank() && !module.name().toLowerCase(Locale.ROOT)
                    .contains(search.toLowerCase(Locale.ROOT))) continue;
            result.add(module);
        }
        return result;
    }

    private int[] bounds() {
        int panelW = Math.max(180, Math.min(width - 24, width < 600 ? 410 : 1000));
        int panelH = Math.max(170, Math.min(height - 20, width < 600 ? 520 : 660));
        return new int[]{(width - panelW) / 2, (height - panelH) / 2, panelW, panelH};
    }

    @Override
    public void render(DrawContext d, int mouseX, int mouseY, float delta) {
        renderBackground(d, mouseX, mouseY, delta);
        int[] b = bounds();
        int left = b[0], top = b[1], panelW = b[2], panelH = b[3];
        boolean compact = width < 660;
        int accent = 0xFF38CFFF;

        rounded(d, left, top + 3, panelW, panelH, 13, 0x65000000);
        rounded(d, left, top, panelW, panelH, 13, 0xF00A0F18);
        rounded(d, left + 1, top + 1, panelW - 2, 51, 12, 0xFF111B28);
        d.fill(left + 14, top + 50, left + panelW - 14, top + 51, 0x3338CFFF);

        d.drawText(textRenderer, "ZYVEN", left + 15, top + 13, accent, true);
        d.drawText(textRenderer, "CLIENT", left + 69, top + 13, 0xFFF4F7FC, true);
        d.drawText(textRenderer, "MODULAR CONTROL CENTER", left + 15, top + 31, 0xFF8294AA, false);
        String closeHint = compact ? "ESC" : "RIGHT SHIFT / ESC";
        d.drawText(textRenderer, closeHint, left + panelW - textRenderer.getWidth(closeHint) - 14,
                top + 20, 0xFF8697AA, false);

        int navY = top + 61;
        int navGap = 4;
        int navW = (panelW - 28 - navGap * 3) / 4;
        for (int i = 0; i < CATEGORIES.length; i++) {
            int nx = left + 14 + i * (navW + navGap);
            boolean selected = category == i;
            rounded(d, nx, navY, navW, 27, 7, selected ? 0xFF15384B : 0xFF151D29);
            if (selected) rounded(d, nx + 2, navY + 23, navW - 4, 2, 1, accent);
            d.drawCenteredTextWithShadow(textRenderer, CATEGORIES[i], nx + navW / 2, navY + 9,
                    selected ? 0xFFEAFBFF : 0xFF9EADBE);
        }

        int searchY = navY + 35;
        int searchX = left + 14;
        int searchW = panelW - 28;
        rounded(d, searchX, searchY, searchW, 29, 8, 0xFF111A26);
        d.drawText(textRenderer, "⌕", searchX + 9, searchY + 9, accent, true);
        String searchText = search.isEmpty() ? "Search modules" : search;
        d.drawText(textRenderer, searchText + (typingSearch ? "_" : ""),
                searchX + 25, searchY + 9, search.isEmpty() ? 0xFF718197 : 0xFFE5F2FA, false);

        List<Module> shown = visibleModules();
        int listY = searchY + 40;
        int bottom = top + panelH - 13;
        int listH = Math.max(24, bottom - listY);
        d.drawText(textRenderer, shown.size() + " MODULES", searchX + 1, listY - 12, 0xFF7D8DA1, true);

        // Keep a dedicated 14px gutter between cards and scrollbar so touch swipes
        // on the scrollbar can never be interpreted as module taps.
        int gutter = Math.min(14, Math.max(8, searchW / 14));
        int contentW = Math.max(50, searchW - gutter);
        int columns = compact ? 1 : 2;
        int gap = 8;
        int cardW = (contentW - gap * (columns - 1)) / columns;
        int cardH = compact ? 58 : 72;
        int rowStep = cardH + 7;
        int rows = (shown.size() + columns - 1) / columns;
        double maxScroll = Math.max(0, rows * rowStep - listH);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        d.enableScissor(searchX, listY, searchX + contentW, bottom);
        for (int i = 0; i < shown.size(); i++) {
            Module module = shown.get(i);
            int col = i % columns, row = i / columns;
            int x = searchX + col * (cardW + gap);
            int y = listY + row * rowStep - (int) scroll;
            boolean on = enabled(module.id());
            boolean hovered = mouseX >= x && mouseX <= x + cardW && mouseY >= y && mouseY <= y + cardH;
            rounded(d, x, y + 2, cardW, cardH, 9, 0x48000000);
            rounded(d, x, y, cardW, cardH, 9,
                    on ? 0xFF172B37 : hovered ? 0xFF202B39 : 0xFF151E2B);
            if (on) rounded(d, x + 1, y + 7, 3, cardH - 14, 2, accent);

            int iconSize = compact ? 30 : 34;
            int iconY = y + (cardH - iconSize) / 2;
            rounded(d, x + 8, iconY, iconSize, iconSize, 8, on ? 0xFF16465A : 0xFF222E3D);
            String icon = module.name().substring(0, Math.min(2, module.name().length())).toUpperCase(Locale.ROOT);
            d.drawCenteredTextWithShadow(textRenderer, icon, x + 8 + iconSize / 2,
                    iconY + (iconSize - 8) / 2, on ? 0xFFB7F3FF : 0xFF9BAFC1);

            int textX = x + (compact ? 46 : 53);
            d.drawText(textRenderer, module.name(), textX, y + (compact ? 11 : 13), 0xFFF2F7FC, true);
            String desc = module.description();
            int maxTextWidth = Math.max(28, cardW - (textX - x) - 88);
            desc = textRenderer.trimToWidth(desc, maxTextWidth);
            d.drawText(textRenderer, desc, textX, y + (compact ? 28 : 31), 0xFF91A1B4, false);

            int settingsX = x + cardW - 69;
            rounded(d, settingsX, y + cardH / 2 - 12, 24, 24, 7, 0xFF273445);
            d.drawCenteredTextWithShadow(textRenderer, "···", settingsX + 12, y + cardH / 2 - 4, 0xFFB8C9D9);

            int switchX = x + cardW - 38;
            rounded(d, switchX, y + cardH / 2 - 8, 29, 16, 8,
                    on ? 0xFF167E9D : 0xFF3B4655);
            rounded(d, on ? switchX + 15 : switchX + 2, y + cardH / 2 - 6, 12, 12, 6,
                    on ? 0xFFE8FBFF : 0xFFBEC8D4);
        }
        d.disableScissor();

        // Scrollbar is drawn entirely in its own gutter.
        if (maxScroll > 0) {
            int trackX = searchX + contentW + 4;
            d.fill(trackX, listY, trackX + 2, bottom, 0xFF263342);
            int thumbH = Math.max(20, (int) (listH * listH / (rows * rowStep)));
            int thumbY = listY + (int) ((listH - thumbH) * (scroll / maxScroll));
            rounded(d, trackX - 2, thumbY, 6, thumbH, 3, accent);
        }
        super.render(d, mouseX, mouseY, delta);
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
        double mx = click.x(), my = click.y();
        int[] b = bounds();
        int left = b[0], top = b[1], panelW = b[2], panelH = b[3];
        boolean compact = width < 660;
        int navY = top + 61, navGap = 4;
        int navW = (panelW - 28 - navGap * 3) / 4;
        if (my >= navY && my <= navY + 27 && mx >= left + 14 && mx <= left + panelW - 14) {
            int index = (int) ((mx - left - 14) / (navW + navGap));
            if (index >= 0 && index < CATEGORIES.length) { category = index; scroll = 0; }
            return true;
        }

        int searchY = navY + 35;
        int searchX = left + 14, searchW = panelW - 28;
        if (mx >= searchX && mx <= searchX + searchW && my >= searchY && my <= searchY + 29) {
            typingSearch = true;
            return true;
        }
        typingSearch = false;

        int listY = searchY + 40, bottom = top + panelH - 13;
        if (my < listY || my > bottom || mx < searchX || mx > searchX + searchW) {
            return super.mouseClicked(click, doubled);
        }

        pointerDown = true;
        draggingList = false;
        pressedModuleId = -1;
        pressedSettings = false;
        pressX = mx;
        pressY = my;

        int gutter = Math.min(14, Math.max(8, searchW / 14));
        int contentW = Math.max(50, searchW - gutter);
        if (mx >= searchX + contentW) {
            return true; // Begin scroll from gutter without touching a module card.
        }

        List<Module> shown = visibleModules();
        int columns = compact ? 1 : 2, gap = 8;
        int cardW = (contentW - gap * (columns - 1)) / columns;
        int cardH = compact ? 58 : 72, rowStep = cardH + 7;
        for (int i = 0; i < shown.size(); i++) {
            Module module = shown.get(i);
            int col = i % columns, row = i / columns;
            int x = searchX + col * (cardW + gap);
            int y = listY + row * rowStep - (int) scroll;
            if (mx >= x && mx <= x + cardW && my >= y && my <= y + cardH) {
                pressedModuleId = module.id();
                pressedSettings = mx >= x + cardW - 72 && mx <= x + cardW - 43;
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (pointerDown) {
            double movedX = click.x() - pressX;
            double movedY = click.y() - pressY;
            if (Math.abs(movedX) + Math.abs(movedY) > 5) {
                draggingList = true;
                pressedModuleId = -1; // A swipe can never activate a module.
            }
            if (draggingList) {
                int panelW = bounds()[2];
                int searchW = panelW - 28;
                int listH = Math.max(24, bounds()[3] - (61 + 35 + 40) - 13);
                int gutter = Math.min(14, Math.max(8, searchW / 14));
                int contentW = Math.max(50, searchW - gutter);
                boolean compact = width < 660;
                int columns = compact ? 1 : 2;
                int cardH = compact ? 58 : 72;
                int rowStep = cardH + 7;
                int rows = (visibleModules().size() + columns - 1) / columns;
                double maxScroll = Math.max(0, rows * rowStep - listH);
                scroll = Math.max(0, Math.min(maxScroll, scroll - deltaY));
            }
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (pointerDown) {
            if (!draggingList && pressedModuleId >= 0 && client != null) {
                Module selected = null;
                for (Module module : MODULES) {
                    if (module.id() == pressedModuleId) { selected = module; break; }
                }
                if (selected != null) {
                    if (pressedSettings) {
                        if (selected.id() == 8) client.setScreen(new PayAlertSettingsScreen());
                        else client.setScreen(new ModuleSettingsScreen(selected.id(), selected.name()));
                    } else {
                        toggle(selected.id());
                    }
                }
            }
            pointerDown = false;
            draggingList = false;
            pressedModuleId = -1;
            pressedSettings = false;
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int panelH = bounds()[3];
        int listH = Math.max(24, panelH - (61 + 35 + 40) - 13);
        boolean compact = width < 660;
        int columns = compact ? 1 : 2;
        int rowStep = (compact ? 58 : 72) + 7;
        int rows = (visibleModules().size() + columns - 1) / columns;
        double maxScroll = Math.max(0, rows * rowStep - listH);
        scroll = Math.max(0, Math.min(maxScroll, scroll - verticalAmount * 32));
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_RIGHT_SHIFT) { close(); return true; }
        if (typingSearch && key == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1); scroll = 0; return true;
        }
        if (typingSearch && key == GLFW.GLFW_KEY_ENTER) { typingSearch = false; return true; }
        if (typingSearch && key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) {
            search += (char) ('a' + key - GLFW.GLFW_KEY_A); scroll = 0; return true;
        }
        if (typingSearch && key == GLFW.GLFW_KEY_SPACE) { search += " "; return true; }
        return super.keyPressed(input);
    }

    @Override public boolean shouldPause() { return false; }
}
