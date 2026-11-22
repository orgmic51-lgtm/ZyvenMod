package me.zyven.zyvenmod;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class AuctionScreen extends Screen {
    private int tab = 0;
    public AuctionScreen() { super(Text.literal("Zyven Auction")); }

    @Override
    public void render(DrawContext d, int mouseX, int mouseY, float delta) {
        renderBackground(d, mouseX, mouseY, delta);
        int cx = width / 2, cy = height / 2, x = cx - 205, y = cy - 130;
        d.fill(x, y, x + 410, y + 260, 0xF0121721);
        d.fill(x, y, x + 410, y + 38, 0xFF1D2A3A);
        d.drawText(textRenderer, "ZYVEN  /  MARKET", x + 16, y + 14, 0xFF70D4FF, false);
        d.drawText(textRenderer, "O  •  CLOSE", x + 330, y + 14, 0xFF9BA9BA, false);
        String[] tabs = {"Browse", "My Listings", "Sell"};
        for (int i = 0; i < tabs.length; i++) {
            int tx = x + 16 + i * 91;
            d.fill(tx, y + 49, tx + 82, y + 73, tab == i ? 0xFF285675 : 0xFF202B39);
            d.drawCenteredTextWithShadow(textRenderer, tabs[i], tx + 41, y + 57,
                    tab == i ? 0xFFFFFFFF : 0xFFB5C0CE);
        }
        d.fill(x + 16, y + 85, x + 394, y + 113, 0xFF0D131C);
        d.drawText(textRenderer, "Search items...", x + 28, y + 95, 0xFF78869A, false);
        String heading = tab == 0 ? "AVAILABLE LISTINGS" : tab == 1 ? "YOUR ACTIVE LISTINGS" : "CREATE A LISTING";
        d.drawText(textRenderer, heading, x + 18, y + 128, 0xFF75CFFF, false);
        d.fill(x + 16, y + 148, x + 394, y + 218, 0xFF1A2330);
        d.drawText(textRenderer, tab == 2 ? "Select an item in your inventory to list it." :
                "No marketplace listings loaded.", x + 28, y + 174, 0xFFCBD4DF, false);
        d.drawText(textRenderer, "Transparent fixed-price listings • No random rewards",
                x + 18, y + 235, 0xFF7F8C9D, false);
        super.render(d, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();
        int x = width / 2 - 205, y = height / 2 - 130;
        for (int i = 0; i < 3; i++) {
            int tx = x + 16 + i * 91;
            if (mx >= tx && mx <= tx + 82 && my >= y + 49 && my <= y + 73) tab = i;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.key();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_O) {
            close(); return true;
        }
        return super.keyPressed(input);
    }
    @Override public boolean shouldPause() { return false; }
}
