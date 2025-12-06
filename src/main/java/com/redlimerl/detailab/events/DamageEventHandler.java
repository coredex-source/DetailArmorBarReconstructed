package com.redlimerl.detailab.events;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles damage events to trigger thorns animation.
 * In NeoForge, the tick handling is done in DetailArmorBarClient.
 */
public class DamageEventHandler {
    
    /**
     * Check if player has any armor with thorns enchantment
     */
    public static boolean hasThorns(Player player) {
        List<ItemStack> armor = getArmorItems(player);
        for (ItemStack item : armor) {
            if (!item.isEmpty()) {
                // Use EnchantmentHelper to check for thorns enchantment
                var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(item);
                if (enchantments != null && !enchantments.isEmpty()) {
                    var entries = enchantments.entrySet();
                    for (var entry : entries) {
                        if (entry.getKey().unwrapKey().orElse(null) == Enchantments.THORNS) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Get all armor items worn by the player
     */
    private static List<ItemStack> getArmorItems(Player player) {
        List<ItemStack> list = new ArrayList<>();
        list.add(player.getItemBySlot(EquipmentSlot.HEAD));
        list.add(player.getItemBySlot(EquipmentSlot.CHEST));
        list.add(player.getItemBySlot(EquipmentSlot.LEGS));
        list.add(player.getItemBySlot(EquipmentSlot.FEET));
        return list;
    }
}
