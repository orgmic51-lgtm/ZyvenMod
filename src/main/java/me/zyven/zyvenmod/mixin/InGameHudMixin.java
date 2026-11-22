package me.zyven.zyvenmod.mixin;

import me.zyven.zyvenmod.ZyvenMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void zyven$replaceVanillaHud(DrawContext draw, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || client.options.hudHidden) return;

        ci.cancel();
        int w = draw.getScaledWindowWidth();
        int h = draw.getScaledWindowHeight();
        int center = w / 2;

        // Compact Zyven HUD panel and status bars
        draw.fill(12, 12, 174, 67, 0xB9101723);
        draw.fill(12, 12, 174, 14, 0xFF20BFEA);
        draw.drawText(client.textRenderer, Text.literal("ZYVEN SMP"), 20, 20, 0xFF62D9FF, false);
        draw.drawText(client.textRenderer,
                Text.literal("HP " + Math.round(player.getHealth()) + "   ARMOR " + player.getArmor()),
                20, 35, 0xFFF0F5FA, false);
        draw.fill(20, 49, 158, 55, 0xFF303B4B);
        draw.fill(20, 49, 20 + Math.max(0, Math.min(138, Math.round(138f * player.getHealth() / player.getMaxHealth()))), 55, 0xFF35D889);
        draw.drawText(client.textRenderer,
                Text.literal("FOOD " + player.getHungerManager().getFoodLevel()),
                20, 57, 0xFFFFD36B, false);

        String coords = String.format(java.util.Locale.US, "XYZ  %.1f  %.1f  %.1f",
                player.getX(), player.getY(), player.getZ());
        draw.drawText(client.textRenderer, coords, 12, 78, 0xFFD6E8F4, true);

        if (ZyvenMod.showProfit) {
            String profit = "PROFIT: " + ZyvenMod.PROFIT.formatted();
            int color = ZyvenMod.PROFIT.value() > 0 ? 0xFF55E68A :
                    ZyvenMod.PROFIT.value() < 0 ? 0xFFFF626B : 0xFFB8BEC9;
            draw.drawCenteredTextWithShadow(client.textRenderer, Text.literal(profit), center, 12, color);
        }

        // Custom 9-slot hotbar
        int slotW = 22;
        int totalW = slotW * 9 + 8 * 3;
        int startX = center - totalW / 2;
        int y = h - 34;
        draw.fill(startX - 5, y - 5, startX + totalW + 5, y + 25, 0xC9101723);
        for (int i = 0; i < 9; i++) {
            int x = startX + i * (slotW + 3);
            boolean selected = player.getInventory().getSelectedSlot() == i;
            draw.fill(x, y, x + slotW, y + slotW, selected ? 0xFF245B76 : 0xFF263344);
            draw.fill(x, y, x + slotW, y + 2, selected ? 0xFF45D8FF : 0xFF536174);
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) draw.drawItem(stack, x + 3, y + 3);
        }
    }
}
