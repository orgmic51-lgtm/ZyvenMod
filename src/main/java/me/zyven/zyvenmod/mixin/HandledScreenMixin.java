package me.zyven.zyvenmod.mixin;

import me.zyven.zyvenmod.ZyvenMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Shadow protected int x;
    @Shadow protected int y;

    @Inject(method = "drawSlot", at = @At("TAIL"))
    private void zyven$drawFakeHostItem(DrawContext context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if (!ZyvenMod.fakeItemsEnabled) return;
        ItemStack original = slot.getStack();
        if (original.isEmpty() || !original.isOf(Items.END_STONE) || !original.hasCustomName()) return;

        String hostName = original.getName().getString();
        Item fake = FakeItemMap.itemFor(hostName);
        if (fake == null) return;

        ItemStack visual = new ItemStack(fake, original.getCount());
        visual.setCustomName(Text.literal(hostName));
        context.fill(x + slot.x, y + slot.y, x + slot.x + 16, y + slot.y + 16, 0xFF30343B);
        context.drawItem(visual, x + slot.x, y + slot.y);
        context.drawStackOverlay(net.minecraft.client.MinecraftClient.getInstance().textRenderer,
                visual, x + slot.x, y + slot.y);
    }
}
