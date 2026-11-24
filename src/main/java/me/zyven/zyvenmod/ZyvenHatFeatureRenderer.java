package me.zyven.zyvenmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public final class ZyvenHatFeatureRenderer
        extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private static final Identifier RED_TEXTURE =
            Identifier.of("zyvenmod", "textures/entity/zyven_hat_red.png");
    private static final Identifier WHITE_TEXTURE =
            Identifier.of("zyvenmod", "textures/entity/zyven_hat_white.png");

    private final Model<PlayerEntityRenderState> redHat;
    private final Model<PlayerEntityRenderState> whiteTrim;

    public ZyvenHatFeatureRenderer(
            FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
        super(context);
        this.redHat = model(createRedPart());
        this.whiteTrim = model(createWhitePart());
    }

    private static Model<PlayerEntityRenderState> model(ModelPart root) {
        return new Model<PlayerEntityRenderState>(root, RenderLayer::getEntityCutoutNoCull) {
            @Override
            public void setAngles(PlayerEntityRenderState state) {
                // The feature copies the player head transform immediately before rendering.
            }
        };
    }

    private static ModelPart createRedPart() {
        ModelData data = new ModelData();
        data.getRoot().addChild("hat", ModelPartBuilder.create()
                .uv(0, 0).cuboid(-3.5f, -13.0f, -3.5f, 7.0f, 4.0f, 7.0f)
                .uv(28, 0).cuboid(-2.5f, -17.0f, -2.5f, 5.0f, 4.0f, 5.0f)
                .uv(48, 0).cuboid(-1.5f, -20.0f, -1.5f, 3.0f, 3.0f, 3.0f),
                ModelTransform.NONE);
        return TexturedModelData.of(data, 64, 32).createModel();
    }

    private static ModelPart createWhitePart() {
        ModelData data = new ModelData();
        data.getRoot().addChild("trim", ModelPartBuilder.create()
                .uv(0, 0).cuboid(-4.5f, -9.0f, -4.5f, 9.0f, 1.5f, 9.0f)
                .uv(40, 0).cuboid(-1.7f, -22.0f, -1.7f, 3.4f, 3.4f, 3.4f),
                ModelTransform.NONE);
        return TexturedModelData.of(data, 64, 32).createModel();
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light,
                       PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!ZyvenMod.hatsEnabled || client.player == null || state.invisible
                || state.id != client.player.getId()) {
            return;
        }

        redHat.resetTransforms();
        whiteTrim.resetTransforms();
        redHat.getRootPart().copyTransform(getContextModel().head);
        whiteTrim.getRootPart().copyTransform(getContextModel().head);

        render(redHat, RED_TEXTURE, matrices, queue, light, state,
                0xFFFFFFFF, OverlayTexture.DEFAULT_UV);
        render(whiteTrim, WHITE_TEXTURE, matrices, queue, light, state,
                0xFFFFFFFF, OverlayTexture.DEFAULT_UV);
    }
}
