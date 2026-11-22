package me.zyven.zyvenmod.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.DownloadingTerrainScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DownloadingTerrainScreen.class)
public abstract class DownloadingTerrainScreenMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void zyven$drawServerBranding(DrawContext draw, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int cx = draw.getScaledWindowWidth() / 2;
        int y = draw.getScaledWindowHeight() / 2 - 54;
        draw.fill(cx - 116, y - 24, cx + 116, y + 40, 0xC900101A);
        draw.fill(cx - 116, y - 24, cx + 116, y - 21, 0xFF20C8F2);
        draw.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,
                Text.literal("❄ ZYVEN ❄"), cx, y - 8, 0xFF36D9FF);
        draw.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,
                Text.literal("S M P"), cx, y + 12, 0xFFF4F8FF);
        draw.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,
                Text.literal("Connecting to world..."), cx, y + 49, 0xFFD2DCE8);
    }
}
