package me.zyven.zyvenmod.mixin;

import me.zyven.zyvenmod.ZyvenMod;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void zyven$addNametagBadge(Entity entity, CallbackInfoReturnable<Text> cir) {
        if (entity instanceof PlayerEntity && cir.getReturnValue() != null) {
            cir.setReturnValue(ZyvenMod.decoratePlayerName(cir.getReturnValue()));
        }
    }
}
