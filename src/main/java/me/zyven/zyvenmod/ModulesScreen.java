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
    private boolean scrollbarDragging;
    private double scrollbarGrabOffset;
    private boolean pressedSettings;
    private int pressedModuleId = -1;
    private double pressX;
    private double pressY;

    private record Module(String name, String description, int category, int id) {}

    private record Layout(
            int left, int top, int panelW, int panelH, boolean compact,
            int navY, int navH, int searchY, int searchH,
            int searchX, int searchW, int listY, int bottom, int listH,
            int contentW, int columns, int cardW, int cardH, int rowStep,
            int contentHeight, int trackX, int thumbHeight, int thumbY,
            double maxScroll
    ) {}

    private static final String[] CATEGORIES = {"ALL", "HUD", "VISUALS", "CLIENT"};
    private static final Module[] MODULES = {
            new Module("Profit HUD", "Track money received and sent", 1, 0),
            new Module("FPS Counter", "Live frames per second overlay", 1, 7),
            new Module("Payment Alerts", "Animated player payment notifications", 1, 8),
            new Module("HUD Layout", "Drag your HUD elements anywhere", 1, 10),
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

    private Layout layout() {
        boolean compact = width < 620 || height < 460;
        int panelW = Math.max(1, Math.min(width - 24, compact ? 330 : 1000));
        int panelH = Math.max(120, Math.min(height - 30, compact ? 430 : 660));
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;

        int navY = top + (compact ? 42 : 61);
        int navH = compact ? 22 : 27;
        int searchY = navY + (compact ? 28 : 35);
        int searchH = compact ? 25 : 29;
        int searchX = left + (compact ? 12 : 14);
        int searchW = panelW - (compact ? 24 : 28);
        int listY = searchY + (compact ? 34 : 40);
        int bottom = top + panelH - (compact ? 10 : 13);
        int listH = Math.max(20, bottom - listY);

        int gutter = compact ? 16 : 18;
        int contentW = Math.max(1, searchW - gutter);
        int columns = compact ? 1 : 2;
        int gap = compact ? 7 : 8;
        int cardW = Math.max(1, (contentW - gap * (columns - 1)) / columns);
        int cardH = compact ? 52 : 72;
        int rowStep = cardH + (compact ? 6 : 7);
        int rows = (visibleModules().size() + columns - 1) / columns;
        int contentHeight = rows == 0 ? 0 : (rows - 1) * rowStep + cardH;
        double maxScroll = Math.max(0, contentHeight - listH);

        int trackX = searchX + contentW + Math.max(2, (gutter - 2) / 2);
        int thumbHeight = maxScroll > 0
                ? Math.min(listH, Math.max(18, (int) Math.round((double) listH * listH / contentHeight)))
                : listH;
        int thumbTravel = Math.max(0, listH - thumbHeight);
        int thumbY = listY + (maxScroll > 0
                ? (int) Math.round(thumbTravel * (scroll / maxScroll)) : 0);

        return new Layout(left, top, panelW, panelH, compact, navY, navH,
                searchY, searchH, searchX, searchW, listY, bottom, listH,
                contentW, columns, cardW, cardH, rowStep, contentHeight,
                trackX, thumbHeight, thumbY, maxScroll);
    }

    @Override
    public void render(DrawContext d, int mouseX, int mouseY, float delta) {
        renderBackground(d, mouseX, mouseY, delta);
        Layout l = layout();
        scroll = Math.max(0, Math.min(scroll, l.maxScroll()));
        int accent = 0xFF38CFFF;
        int left = l.left(), top = l.top(), panelW = l.panelW(), panelH = l.panelH();

        rounded(d, left, top + 3, panelW, panelH, 12, 0x65000000);
        rounded(d, left, top, panelW, panelH, 12, 0xF00A0F18);
        rounded(d, left + 1, top + 1, panelW - 2, l.compact() ? 35 : 51, 11, 0xFF111B28);
        d.fill(left + 12, top + (l.compact() ? 34 : 50),
                left + panelW - 12, top + (l.compact() ? 35 : 51), 0x3338CFFF);

        d.drawText(textRenderer, "ZYVEN", left + 14, top + (l.compact() ? 8 : 13), accent, true);
        d.drawText(textRenderer, "CLIENT", left + 68, top + (l.compact() ? 8 : 13), 0xFFF4F7FC, true);
        if (!l.compact()) {
            d.drawText(textRenderer, "MODULAR CONTROL CENTER",
                    left + 14, top + 31, 0xFF8294AA, false);
        }
        String closeHint = l.compact() ? "ESC" : "RIGHT SHIFT / ESC";
        d.drawText(textRenderer, closeHint,
                left + panelW - textRenderer.getWidth(closeHint) - 13,
                top + (l.compact() ? 12 : 20), 0xFF8697AA, false);

        int navGap = l.compact() ? 3 : 4;
        int navW = (panelW - (l.compact() ? 24 : 28) - navGap * 3) / 4;
        for (int i = 0; i < CATEGORIES.length; i++) {
            int nx = left + (l.compact() ? 12 : 14) + i * (navW + navGap);
            boolean selected = category == i;
            rounded(d, nx, l.navY(), navW, l.navH(), 6,
                    selected ? 0xFF15384B : 0xFF151D29);
            if (selected) rounded(d, nx + 2, l.navY() + l.navH() - 3, navW - 4, 2, 1, accent);
            d.drawCenteredTextWithShadow(textRenderer, CATEGORIES[i],
                    nx + navW / 2, l.navY() + (l.compact() ? 7 : 9),
                    selected ? 0xFFEAFBFF : 0xFF9EADBE);
        }

        rounded(d, l.searchX(), l.searchY(), l.searchW(), l.searchH(), 7, 0xFF111A26);
        d.drawText(textRenderer, "⌕", l.searchX() + 8, l.searchY() + (l.compact() ? 8 : 9),
                accent, true);
        String searchText = search.isEmpty() ? "Search modules" : search;
        d.drawText(textRenderer, searchText + (typingSearch ? "_" : ""),
                l.searchX() + 24, l.searchY() + (l.compact() ? 8 : 9),
                search.isEmpty() ? 0xFF718197 : 0xFFE5F2FA, false);

        d.drawText(textRenderer, visibleModules().size() + " MODULES",
                l.searchX() + 1, l.listY() - 11, 0xFF7D8DA1, true);

        d.enableScissor(l.searchX(), l.listY(), l.searchX() + l.contentW(), l.bottom());
        List<Module> shown = visibleModules();
        for (int i = 0; i < shown.size(); i++) {
            Module module = shown.get(i);
            int col = i % l.columns(), row = i / l.columns();
            int x = l.searchX() + col * (l.cardW() + (l.compact() ? 7 : 8));
            int y = l.listY() + row * l.rowStep() - (int) scroll;
            boolean on = enabled(module.id());
            boolean hovered = mouseX >= x && mouseX <= x + l.cardW()
                    && mouseY >= y && mouseY <= y + l.cardH();

            rounded(d, x, y + 2, l.cardW(), l.cardH(), 8, 0x48000000);
            rounded(d, x, y, l.cardW(), l.cardH(), 8,
                    on ? 0xFF172B37 : hovered ? 0xFF202B39 : 0xFF151E2B);
            if (on) rounded(d, x + 1, y + 7, 3, l.cardH() - 14, 2, accent);

            int iconSize = l.compact() ? 27 : 34;
            int iconY = y + (l.cardH() - iconSize) / 2;
            rounded(d, x + 7, iconY, iconSize, iconSize, 7,
                    on ? 0xFF16465A : 0xFF222E3D);
            String icon = module.name().substring(0, Math.min(2, module.name().length()))
                    .toUpperCase(Locale.ROOT);
            d.drawCenteredTextWithShadow(textRenderer, icon,
                    x + 7 + iconSize / 2, iconY + (iconSize - 8) / 2,
                    on ? 0xFFB7F3FF : 0xFF9BAFC1);

            int textX = x + (l.compact() ? 42 : 53);
            int titleY = y + (l.compact() ? 8 : 13);
            d.drawText(textRenderer, textRenderer.trimToWidth(module.name(),
                    Math.max(20, l.cardW() - (textX - x) - 78)), textX, titleY, 0xFFF2F7FC, true);
            String desc = textRenderer.trimToWidth(module.description(),
                    Math.max(20, l.cardW() - (textX - x) - 78));
            if (!l.compact()) {
                d.drawText(textRenderer, desc, textX, y + 31, 0xFF91A1B4, false);
            }

            int settingsX = x + l.cardW() - 67;
            rounded(d, settingsX, y + (l.cardH() - 22) / 2, 22, 22, 6, 0xFF273445);
            d.drawCenteredTextWithShadow(textRenderer, "···", settingsX + 11,
                    y + (l.cardH() - 8) / 2, 0xFFB8C9D9);

            if (module.id() == 10) {
                rounded(d, x + l.cardW() - 48, y + (l.cardH() - 21) / 2, 40, 21, 6, 0xFF15536C);
                d.drawCenteredTextWithShadow(textRenderer, "OPEN", x + l.cardW() - 28,
                        y + (l.cardH() - 8) / 2, 0xFFE8FBFF);
            } else {
                int switchX = x + l.cardW() - 38;
                rounded(d, switchX, y + (l.cardH() - 15) / 2, 28, 15, 8,
                        on ? 0xFF167E9D : 0xFF3B4655);
                rounded(d, on ? switchX + 14 : switchX + 2,
                        y + (l.cardH() - 11) / 2, 11, 11, 6,
                        on ? 0xFFE8FBFF : 0xFFBEC8D4);
            }
        }
        d.disableScissor();

        if (l.maxScroll() > 0) {
            d.fill(l.trackX(), l.listY(), l.trackX() + 2, l.bottom(), 0xFF263342);
            rounded(d, l.trackX() - 3, l.thumbY(), 8, l.thumbHeight(), 4, accent);
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

    private void setScrollFromThumb(double mouseY) {
        Layout l = layout();
        if (l.maxScroll() <= 0) {
            scroll = 0;
            return;
        }
        int travel = Math.max(1, l.listH() - l.thumbHeight());
        double thumbTop = Math.max(l.listY(),
                Math.min(l.bottom() - l.thumbHeight(), mouseY - scrollbarGrabOffset));
        scroll = ((thumbTop - l.listY()) / travel) * l.maxScroll();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        Layout l = layout();
        int left = l.left(), top = l.top(), panelW = l.panelW();

        int navGap = l.compact() ? 3 : 4;
        int navLeft = left + (l.compact() ? 12 : 14);
        int navW = (panelW - (l.compact() ? 24 : 28) - navGap * 3) / 4;
        if (my >= l.navY() && my <= l.navY() + l.navH()
                && mx >= navLeft && mx <= navLeft + panelW - (l.compact() ? 24 : 28)) {
            int index = (int) ((mx - navLeft) / (navW + navGap));
            if (index >= 0 && index < CATEGORIES.length) {
                category = index;
                scroll = 0;
            }
            return true;
        }

        if (mx >= l.searchX() && mx <= l.searchX() + l.searchW()
                && my >= l.searchY() && my <= l.searchY() + l.searchH()) {
            typingSearch = true;
            return true;
        }
        typingSearch = false;

        if (my < l.listY() || my > l.bottom()
                || mx < l.searchX() || mx > l.searchX() + l.searchW()) {
            return super.mouseClicked(click, doubled);
        }

        if (l.maxScroll() > 0 && mx >= l.trackX() - 7 && mx <= l.trackX() + 7) {
            pointerDown = true;
            draggingList = false;
            scrollbarDragging = true;
            pressedModuleId = -1;
            if (my >= l.thumbY() && my <= l.thumbY() + l.thumbHeight()) {
                scrollbarGrabOffset = my - l.thumbY();
            } else {
                scrollbarGrabOffset = l.thumbHeight() / 2.0;
                setScrollFromThumb(my);
            }
            return true;
        }

        pointerDown = true;
        draggingList = false;
        scrollbarDragging = false;
        pressedModuleId = -1;
        pressedSettings = false;
        pressX = mx;
        pressY = my;

        if (mx >= l.searchX() + l.contentW()) {
            return true;
        }

        List<Module> shown = visibleModules();
        int gap = l.compact() ? 7 : 8;
        for (int i = 0; i < shown.size(); i++) {
            Module module = shown.get(i);
            int col = i % l.columns(), row = i / l.columns();
            int x = l.searchX() + col * (l.cardW() + gap);
            int y = l.listY() + row * l.rowStep() - (int) scroll;
            if (mx >= x && mx <= x + l.cardW() && my >= y && my <= y + l.cardH()) {
                pressedModuleId = module.id();
                pressedSettings = mx >= x + l.cardW() - 72 && mx <= x + l.cardW() - 43;
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (scrollbarDragging) {
            setScrollFromThumb(click.y());
            return true;
        }
        if (pointerDown) {
            double movedX = click.x() - pressX;
            double movedY = click.y() - pressY;
            if (Math.abs(movedX) + Math.abs(movedY) > 4) {
                draggingList = true;
                pressedModuleId = -1;
            }
            if (draggingList) {
                Layout l = layout();
                scroll = Math.max(0, Math.min(l.maxScroll(), scroll - deltaY));
            }
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (pointerDown || scrollbarDragging) {
            if (!scrollbarDragging && !draggingList && pressedModuleId >= 0 && client != null) {
                Module selected = null;
                for (Module module : MODULES) {
                    if (module.id() == pressedModuleId) {
                        selected = module;
                        break;
                    }
                }
                if (selected != null) {
                    if (selected.id() == 10) {
                        client.setScreen(new HudLayoutScreen());
                    } else if (pressedSettings) {
                        if (selected.id() == 8) client.setScreen(new PayAlertSettingsScreen());
                        else client.setScreen(new ModuleSettingsScreen(selected.id(), selected.name()));
                    } else {
                        toggle(selected.id());
                    }
                }
            }
            pointerDown = false;
            draggingList = false;
            scrollbarDragging = false;
            pressedModuleId = -1;
            pressedSettings = false;
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        Layout l = layout();
        if (mouseY >= l.listY() && mouseY <= l.bottom()
                && mouseX >= l.searchX() && mouseX <= l.searchX() + l.searchW()) {
            scroll = Math.max(0, Math.min(l.maxScroll(), scroll - verticalAmount * (l.compact() ? 26 : 32)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        if (typingSearch && key == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            scroll = 0;
            return true;
        }
        if (typingSearch && key == GLFW.GLFW_KEY_ENTER) {
            typingSearch = false;
            return true;
        }
        if (typingSearch && key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) {
            search += (char) ('a' + key - GLFW.GLFW_KEY_A);
            scroll = 0;
            return true;
        }
        if (typingSearch && key == GLFW.GLFW_KEY_SPACE) {
            search += " ";
            scroll = 0;
            return true;
        }
        return super.keyPressed(input);
    }

    @Override public boolean shouldPause() { return false; }
}
