package me.zyven.zyvenmod.mixin;

import me.zyven.zyvenmod.ZyvenMod;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.SkyRendering;
import net.minecraft.client.render.state.SkyRenderState;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies the selected sky tint after vanilla has calculated biome/weather lighting. */
@Mixin(SkyRendering.class)
public abstract class SkyRenderingMixin {
    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void zyven$applyCustomSkyColor(ClientWorld world, float tickProgress,
                                           Camera camera, SkyRenderState state, CallbackInfo ci) {
        if (ZyvenMod.customSkyEnabled) {
            state.skyColor = ZyvenMod.customSkyColor & 0x00FFFFFF;
        }
    }
}
