package com.redlimerl.detailab.render;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;


public final class ArmorBarUtils {
    
    private ArmorBarUtils() {
    }
    
    public static int getDefense(ItemStack itemStack, EquipmentSlot slot) {
        var modifier = getArmorModifier(itemStack, slot);
        return modifier != null ? (int) modifier.amount() : 0;
    }

    public static AttributeModifier getArmorModifier(ItemStack itemStack, EquipmentSlot slot) {
        // NeoForge's getAttributeModifiers falls back to the item's defaults and applies ItemAttributeModifierEvent
        var modifier = itemStack.getAttributeModifiers();
        for (var entry : modifier.modifiers()) {
            if (entry.slot().test(slot) && entry.attribute().is(Attributes.ARMOR)) {
                return entry.modifier();
            }
        }
        return null;
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