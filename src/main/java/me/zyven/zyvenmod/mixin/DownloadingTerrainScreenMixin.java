package me.zyven.zyvenmod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLoadingScreen.class)
public abstract class DownloadingTerrainScreenMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void zyven$drawServerBranding(DrawContext draw, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int cx = draw.getScaledWindowWidth() / 2;
        int cy = draw.getScaledWindowHeight() / 2 - 42;
        MinecraftClient client = MinecraftClient.getInstance();

        // Draw with vanilla primitives so a missing texture can never show purple/black checkerboard.
        draw.fill(cx - 112, cy - 29, cx + 112, cy + 52, 0xD90B1220);
        draw.fill(cx - 112, cy - 29, cx + 112, cy - 26, 0xFF18C8F2);
        draw.drawCenteredTextWithShadow(client.textRenderer,
                Text.literal("❄ ZYVEN ❄"), cx, cy - 12, 0xFF28D8FF);
        draw.drawCenteredTextWithShadow(client.textRenderer,
                Text.literal("S M P"), cx, cy + 5, 0xFFFFFFFF);
        draw.drawCenteredTextWithShadow(client.textRenderer,
                Text.literal("Joining world..."), cx, cy + 34, 0xFFD2DCE8);
    }
}
