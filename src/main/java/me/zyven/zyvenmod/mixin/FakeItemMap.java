package me.zyven.zyvenmod.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

final class FakeItemMap {
    private FakeItemMap() {}

    static Item itemFor(String name) {
        String normalized = name.toLowerCase(java.util.Locale.ROOT).replace(" ", "");
        if (!normalized.startsWith("host")) return null;
        String id = normalized.substring(4);
        if (id.endsWith(".1")) id = id.substring(0, id.length() - 2);
        return switch (id) {
            case "1" -> Items.DIRT;
            case "2" -> Items.IRON_BLOCK;
            case "3" -> Items.WITHER_SKELETON_SKULL;
            case "4" -> Items.DIAMOND_BLOCK;
            case "5" -> Items.TRIDENT;
            case "6" -> Items.MACE;
            case "7" -> Items.NETHERITE_INGOT;
            case "8" -> Items.NETHERITE_BLOCK;
            case "9" -> Items.SPAWNER;
            default -> null;
        };
    }
}
