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
    private boolean draggingList;

    private record Module(String name, String description, int category, int id, String tag) {}

    private static final String[] CATEGORIES = {"ALL", "HUD", "VISUALS", "CLIENT"};
    private static final Module[] MODULES = {
            new Module("Profit HUD", "Track the money you receive and send", 1, 0, "HUD"),
            new Module("FPS Counter", "Live frames per second overlay", 1, 7, "PERFORMANCE"),
            new Module("Payment Alerts", "Animated player payment notifications", 1, 8, "ALERTS"),
            new Module("Fake Items", "Client-side inventory icon disguise", 3, 1, "CLIENT"),
            new Module("Viewmodel", "Adjust the held item appearance", 2, 2, "VISUAL"),
            new Module("Custom Sky", "Personalize the sky appearance", 2, 3, "VISUAL"),
            new Module("Hats", "Cosmetic headwear options", 2, 4, "COSMETIC"),
            new Module("Crosshair Style", "Customize your crosshair", 2, 5, "VISUAL"),
            new Module("Clean HUD", "Hide selected non-essential elements", 1, 6, "HUD")
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
        int panelW = Math.min(width - 18, width < 600 ? 520 : 1000);
        int panelH = Math.min(height - 18, 660);
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

        d.drawText(textRenderer, "ZYVEN", left + 19, top + 15, accent, true);
        d.drawText(textRenderer, "CLIENT", left + 73, top + 15, 0xFFF4F7FC, true);
        d.drawText(textRenderer, "MODULAR CONTROL CENTER", left + 19, top + 32, 0xFF8294AA, false);
        String closeHint = compact ? "ESC TO CLOSE" : "RIGHT SHIFT  /  ESC";
        d.drawText(textRenderer, closeHint, left + panelW - textRenderer.getWidth(closeHint) - 18,
                top + 20, 0xFF8697AA, false);

        int navY = top + 63;
        int navGap = 7;
        int navW = (panelW - 36 - navGap * 3) / 4;
        for (int i = 0; i < CATEGORIES.length; i++) {
            int nx = left + 18 + i * (navW + navGap);
            boolean selected = category == i;
            rounded(d, nx, navY, navW, 29, 7, selected ? 0xFF15384B : 0xFF151D29);
            if (selected) rounded(d, nx + 2, navY + 25, navW - 4, 2, 1, accent);
            d.drawCenteredTextWithShadow(textRenderer, CATEGORIES[i], nx + navW / 2, navY + 10,
                    selected ? 0xFFEAFBFF : 0xFF9EADBE);
        }

        int searchY = navY + 39;
        int searchX = left + 18;
        int searchW = panelW - 36;
        rounded(d, searchX, searchY, searchW, 31, 8, 0xFF111A26);
        d.drawText(textRenderer, "⌕", searchX + 11, searchY + 10, accent, true);
        String searchText = search.isEmpty() ? "Search modules" : search;
        d.drawText(textRenderer, searchText + (typingSearch ? "_" : ""),
                searchX + 29, searchY + 10, search.isEmpty() ? 0xFF718197 : 0xFFE5F2FA, false);

        List<Module> shown = visibleModules();
        int listY = searchY + 43;
        int bottom = top + panelH - 17;
        int listH = Math.max(24, bottom - listY);
        String countLabel = shown.size() + " MODULES";
        d.drawText(textRenderer, countLabel, searchX + 2, listY - 13, 0xFF7D8DA1, true);

        int columns = compact ? 1 : 2;
        int gap = 9;
        int cardW = (searchW - gap * (columns - 1)) / columns;
        int cardH = compact ? 67 : 72;
        int rowStep = cardH + 8;
        int rows = (shown.size() + columns - 1) / columns;
        double maxScroll = Math.max(0, rows * rowStep - listH);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        d.enableScissor(searchX, listY, searchX + searchW, bottom);
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
            if (on) rounded(d, x + 1, y + 8, 3, cardH - 16, 2, accent);

            rounded(d, x + 10, y + 12, 34, 34, 8, on ? 0xFF16465A : 0xFF222E3D);
            String icon = module.name().substring(0, Math.min(2, module.name().length())).toUpperCase(Locale.ROOT);
            d.drawCenteredTextWithShadow(textRenderer, icon, x + 27, y + 25,
                    on ? 0xFFB7F3FF : 0xFF9BAFC1);

            int textX = x + 54;
            d.drawText(textRenderer, module.name(), textX, y + 13, 0xFFF2F7FC, true);
            String desc = module.description();
            int maxChars = Math.max(12, (cardW - 126) / 5);
            if (desc.length() > maxChars) desc = desc.substring(0, maxChars - 1) + "…";
            d.drawText(textRenderer, desc, textX, y + 30, 0xFF91A1B4, false);

            int gearX = x + cardW - 74;
            rounded(d, gearX, y + 21, 26, 26, 7, 0xFF273445);
            d.drawCenteredTextWithShadow(textRenderer, "···", gearX + 13, y + 29, 0xFFB8C9D9);

            int switchX = x + cardW - 40;
            rounded(d, switchX, y + 26, 30, 16, 8, on ? 0xFF167E9D : 0xFF3B4655);
            rounded(d, on ? switchX + 16 : switchX + 2, y + 28, 12, 12, 6,
                    on ? 0xFFE8FBFF : 0xFFBEC8D4);
        }
        d.disableScissor();

        if (maxScroll > 0) {
            int trackX = searchX + searchW - 2;
            d.fill(trackX, listY, trackX + 2, bottom, 0xFF263342);
            int thumbH = Math.max(20, (int) (listH * listH / (rows * rowStep)));
            int thumbY = listY + (int) ((listH - thumbH) * (scroll / maxScroll));
            rounded(d, trackX - 1, thumbY, 4, thumbH, 2, accent);
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
        int left = b[0], top = b[1], panelW = b[2];
        int navY = top + 63, navGap = 7;
        int navW = (panelW - 36 - navGap * 3) / 4;
        if (my >= navY && my <= navY + 29 && mx >= left + 18 && mx <= left + panelW - 18) {
            int index = (int) ((mx - left - 18) / (navW + navGap));
            if (index >= 0 && index < CATEGORIES.length) { category = index; scroll = 0; }
            return true;
        }

        int searchY = navY + 39;
        int searchX = left + 18, searchW = panelW - 36;
        if (mx >= searchX && mx <= searchX + searchW && my >= searchY && my <= searchY + 31) {
            typingSearch = true;
            return true;
        }
        typingSearch = false;

        int listY = searchY + 43, bottom = top + b[3] - 17;
        if (mx < searchX || mx > searchX + searchW || my < listY || my > bottom) return super.mouseClicked(click, doubled);

        draggingList = true;
        boolean compact = width < 660;
        List<Module> shown = visibleModules();
        int columns = compact ? 1 : 2, gap = 9;
        int cardW = (searchW - gap * (columns - 1)) / columns;
        int cardH = compact ? 67 : 72, rowStep = cardH + 8;
        for (int i = 0; i < shown.size(); i++) {
            Module module = shown.get(i);
            int col = i % columns, row = i / columns;
            int x = searchX + col * (cardW + gap);
            int y = listY + row * rowStep - (int) scroll;
            if (mx >= x && mx <= x + cardW && my >= y && my <= y + cardH) {
                if (mx >= x + cardW - 76 && mx <= x + cardW - 45) {
                    if (client != null) {
                        if (module.id() == 8) client.setScreen(new PayAlertSettingsScreen());
                        else client.setScreen(new ModuleSettingsScreen(module.id(), module.name()));
                    }
                } else if (mx >= x + cardW - 43) {
                    toggle(module.id());
                } else {
                    toggle(module.id());
                }
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (draggingList) { scroll = Math.max(0, scroll - deltaY); return true; }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        draggingList = false;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll = Math.max(0, scroll - verticalAmount * 34);
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
