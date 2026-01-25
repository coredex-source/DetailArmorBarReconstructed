package com.redlimerl.detailab.render;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;


public final class ArmorBarUtils {
    
    private ArmorBarUtils() {
    }
    
    public static int getDefense(ItemStack itemStack, EquipmentSlot slot) {
        var modifier = itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (var entry : modifier.modifiers()) {
            if (entry.slot().test(slot) && entry.attribute().equals(Attributes.ARMOR)) {
                return (int) entry.modifier().amount();
            }
        }
        return 0;
    }
    
    public static List<ItemStack> getArmorItems(Player player) {
        return List.of(
            player.getItemBySlot(EquipmentSlot.HEAD),
            player.getItemBySlot(EquipmentSlot.CHEST),
            player.getItemBySlot(EquipmentSlot.LEGS),
            player.getItemBySlot(EquipmentSlot.FEET)
        );
    }
}