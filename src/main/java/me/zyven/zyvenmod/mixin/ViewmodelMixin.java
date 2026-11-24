package me.zyven.zyvenmod.mixin;

import me.zyven.zyvenmod.ZyvenMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies local-only first-person item offsets after the hand matrix is pushed. */
@Mixin(HeldItemRenderer.class)
public abstract class ViewmodelMixin {
    @Inject(
            method = "renderFirstPersonItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;push()V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void zyven$applyViewmodel(AbstractClientPlayerEntity player, float tickProgress,
                                      float pitch, Hand hand, float swingProgress, ItemStack item,
                                      float equipProgress, MatrixStack matrices,
                                      OrderedRenderCommandQueue queue, int light, CallbackInfo ci) {
        if (!ZyvenMod.viewmodelEnabled || player != MinecraftClient.getInstance().player) return;
        float x = hand == Hand.OFF_HAND ? ZyvenMod.offhandViewmodelX : ZyvenMod.viewmodelX;
        float y = hand == Hand.OFF_HAND ? ZyvenMod.offhandViewmodelY : ZyvenMod.viewmodelY;
        float z = hand == Hand.OFF_HAND ? ZyvenMod.offhandViewmodelZ : ZyvenMod.viewmodelZ;
        float scale = hand == Hand.OFF_HAND ? ZyvenMod.offhandViewmodelScale : ZyvenMod.viewmodelScale;
        matrices.translate(x, y, z);
        matrices.scale(scale, scale, scale);
    }
}
