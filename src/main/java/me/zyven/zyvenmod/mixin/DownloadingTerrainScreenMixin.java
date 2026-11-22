package me.zyven.zyvenmod.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.gl.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLoadingScreen.class)
public abstract class DownloadingTerrainScreenMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void zyven$drawServerBranding(DrawContext draw, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int cx = draw.getScaledWindowWidth() / 2;
        int y = draw.getScaledWindowHeight() / 2 - 54;
        draw.fill(cx - 116, y - 82, cx + 116, y + 58, 0xC900101A);
        draw.fill(cx - 116, y - 82, cx + 116, y - 79, 0xFF20C8F2);
        draw.drawTexture(RenderPipelines.GUI_TEXTURED,
                Identifier.of("zyvenmod", "textures/gui/zyvensmp_icon.jpg"),
                cx - 38, y - 74, 0, 0, 76, 76, 128, 128);
        draw.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,
                Text.literal("ZYVENSMP"), cx, y + 8, 0xFF36D9FF);
        draw.drawCenteredTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer,
                Text.literal("Connecting to world..."), cx, y + 31, 0xFFD2DCE8);
    }
}
