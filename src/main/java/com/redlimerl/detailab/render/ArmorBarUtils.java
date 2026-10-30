package com.redlimerl.detailab.render;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;


public final class ArmorBarUtils {
    
    private ArmorBarUtils() {
    }
    
    public static int getDefense(ItemStack itemStack, EquipmentSlot slot) {
        var modifier = getArmorModifier(itemStack, slot);
        return modifier != null ? (int) modifier.amount() : 0;
    }

    public static AttributeModifier getArmorModifier(ItemStack itemStack, EquipmentSlot slot) {
        var modifier = itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        // Like ItemStack.forEachModifier: armor items keep their default modifiers on the item, not in the component
        if (modifier.modifiers().isEmpty()) {
            modifier = itemStack.getItem().getDefaultAttributeModifiers();
        }
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