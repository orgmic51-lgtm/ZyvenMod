package me.zyven.zyvenmod.mixin;

import me.zyven.zyvenmod.ZyvenMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerCapeMixin {
    private static final AssetInfo.TextureAsset ZYVENCLIENT_CAPE =
            new AssetInfo.TextureAssetInfo(
                    Identifier.of("zyvenmod", "zyvenclient"),
                    Identifier.of("zyvenmod", "textures/cape/zyvenclient.png")
            );

    @Inject(method = "updateCape", at = @At("TAIL"))
    private void zyven$applyClientCape(PlayerLikeEntity player, PlayerEntityRenderState state,
                                       float tickProgress, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!ZyvenMod.zyvenCapeEnabled || client.player == null
                || !player.getUuid().equals(client.player.getUuid()) || state.skinTextures == null) {
            return;
        }

        SkinTextures original = state.skinTextures;
        state.skinTextures = new SkinTextures(
                original.body(), ZYVENCLIENT_CAPE, original.elytra(), original.model(), original.secure()
        );
        state.capeVisible = true;
    }
}
