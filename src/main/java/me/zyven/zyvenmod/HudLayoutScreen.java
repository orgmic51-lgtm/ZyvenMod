package me.zyven.zyvenmod;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Drag the preview cards to place the corresponding in-game HUD elements. */
public final class HudLayoutScreen extends Screen {
    private final double[] xs = {
            ZyvenMod.fpsHudX, ZyvenMod.profitHudX, ZyvenMod.paymentHudX
    };
    private final double[] ys = {
            ZyvenMod.fpsHudY, ZyvenMod.profitHudY, ZyvenMod.paymentHudY
    };
    private int dragging = -1;
    private double grabX;
    private double grabY;

    public HudLayoutScreen() {
        super(Text.literal("Zyven HUD Layout"));
    }

    private int itemWidth(int id) {
        return switch (id) {
            case 0 -> Math.min(108, Math.max(82, width - 24));
            case 1 -> Math.min(158, Math.max(120, width - 24));
            default -> Math.min(320, Math.max(190, width - 20));
        };
    }

    private int itemHeight(int id) {
        return id == 2 ? 96 : 30;
    }

    private int itemX(int id) {
        return (int) Math.round(xs[id] * Math.max(0, width - itemWidth(id)));
    }

    private int itemY(int id) {
        return (int) Math.round(ys[id] * Math.max(0, height - itemHeight(id)));
    }

    private void setPositionFromMouse(int id, double mouseX, double mouseY) {
        int maxX = Math.max(0, width - itemWidth(id));
        int maxY = Math.max(0, height - itemHeight(id));
        int x = (int) Math.max(0, Math.min(maxX, mouseX - grabX));
        int y = (int) Math.max(0, Math.min(maxY, mouseY - grabY));
        xs[id] = maxX == 0 ? 0 : (double) x / maxX;
        ys[id] = maxY == 0 ? 0 : (double) y / maxY;
    }

    private void save() {
        for (int i = 0; i < 3; i++) {
            ZyvenMod.setHudPosition(i, xs[i], ys[i]);
        }
    }

    @Override
    public void render(DrawContext d, int mouseX, int mouseY, float delta) {
        renderBackground(d, mouseX, mouseY, delta);
        d.fill(0, 0, width, 34, 0xD90B1420);
        d.drawText(textRenderer, "ZYVEN  /  HUD LAYOUT", 12, 10, 0xFF38CFFF, true);
        d.drawText(textRenderer, "DRAG THE PANELS  •  ESC TO SAVE",
                Math.max(12, width - 205), 11, 0xFFD4E7F4, false);

        for (int i = 0; i < 3; i++) {
            int x = itemX(i), y = itemY(i), w = itemWidth(i), h = itemHeight(i);
            int border = dragging == i ? 0xFFFFFFFF : 0xFF38CFFF;
            int bg = dragging == i ? 0xF0213E53 : 0xE80B1420;
            d.fill(x - 2, y - 2, x + w + 2, y + h + 2, border);
            d.fill(x, y, x + w, y + h, bg);

            if (i == 0) {
                d.drawText(textRenderer, "FPS  144", x + 10, y + 11, 0xFFFFFFFF, true);
                d.drawText(textRenderer, "DRAG", x + w - 35, y + 11, 0xFF8FCDE5, false);
            } else if (i == 1) {
                d.drawText(textRenderer, "PROFIT", x + 10, y + 6, 0xFFB7C0CC, true);
                d.drawText(textRenderer, "+900M", x + 10, y + 17, 0xFF62E89A, true);
                d.drawText(textRenderer, "DRAG", x + w - 35, y + 11, 0xFF8FCDE5, false);
            } else {
                d.drawText(textRenderer, "PAYMENT RECEIVED", x + 14, y + 10, 0xFF38CFFF, true);
                d.fill(x + 14, y + 29, x + 66, y + 81, 0xFF263C50);
                d.fill(x + 20, y + 35, x + 60, y + 75, 0xFF47718C);
                d.drawText(textRenderer, "PLAYER NAME", x + 78, y + 43, 0xFFFFFFFF, true);
                d.drawText(textRenderer, "+1.0M", x + 78, y + 61, 0xFFFFD45C, true);
                d.drawText(textRenderer, "DRAG TO MOVE", x + w - 92, y + h - 13, 0xFF8FCDE5, false);
            }
        }

        d.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Your HUD positions are saved automatically when you release a panel."),
                width / 2, height - 18, 0xFFB5C5D4);
        super.render(d, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();
        for (int i = 2; i >= 0; i--) {
            int x = itemX(i), y = itemY(i), w = itemWidth(i), h = itemHeight(i);
            if (mx >= x && mx <= x + w && my >= y && my <= y + h) {
                dragging = i;
                grabX = mx - x;
                grabY = my - y;
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (dragging >= 0) {
            setPositionFromMouse(dragging, click.x(), click.y());
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging >= 0) {
            setPositionFromMouse(dragging, click.x(), click.y());
            save();
            dragging = -1;
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE || input.key() == GLFW.GLFW_KEY_L) {
            save();
            close();
            return true;
        }
        if (input.key() == GLFW.GLFW_KEY_R) {
            xs[0] = 0.02; ys[0] = 0.78;
            xs[1] = 0.02; ys[1] = 0.84;
            xs[2] = 0.34; ys[2] = 0.39;
            save();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
