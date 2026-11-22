package me.zyven.zyvenmod.mixin;

import me.zyven.zyvenmod.ZyvenMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void zyven$drawOverlays(DrawContext draw, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || client.options.hudHidden) return;

        int center = draw.getScaledWindowWidth() / 2;
        int top = 8;
        int fpsWidth = client.textRenderer.getWidth(client.getCurrentFps() + " FPS") + 20;
        int profitWidth = ZyvenMod.showProfit
                ? client.textRenderer.getWidth("PROFIT: " + ZyvenMod.PROFIT.formatted()) + 20 : 0;
        int gap = ZyvenMod.showFps && ZyvenMod.showProfit ? 6 : 0;
        int totalWidth = (ZyvenMod.showFps ? fpsWidth : 0) + (ZyvenMod.showProfit ? profitWidth : 0) + gap;
        int x = center - totalWidth / 2;

        if (ZyvenMod.showFps) {
            draw.fill(x, top, x + fpsWidth, top + 25, 0xB9000000);
            draw.drawCenteredTextWithShadow(client.textRenderer,
                    Text.literal(client.getCurrentFps() + " FPS"),
                    x + fpsWidth / 2, top + 8, 0xFFFFFFFF);
            x += fpsWidth + gap;
        }

        if (ZyvenMod.showProfit) {
            int color = ZyvenMod.PROFIT.value() > 0 ? 0xFF55E68A :
                    ZyvenMod.PROFIT.value() < 0 ? 0xFFFF626B : 0xFFFFFFFF;
            draw.fill(x, top, x + profitWidth, top + 25, 0xB9000000);
            draw.drawCenteredTextWithShadow(client.textRenderer,
                    Text.literal("PROFIT: " + ZyvenMod.PROFIT.formatted()),
                    x + profitWidth / 2, top + 8, color);
        }
    }
}
