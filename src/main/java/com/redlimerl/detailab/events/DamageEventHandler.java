package com.redlimerl.detailab.events;

import com.redlimerl.detailab.DetailArmorBar;
import com.redlimerl.detailab.render.ArmorBarRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles damage events to trigger thorns animation
 */
public class DamageEventHandler {
    // Last health value to detect changes
    private static float lastHealth = -1;
    
    /**
     * Register the event handlers
     */
    public static void register() {
        // Monitor player health changes each tick to detect damage
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            PlayerEntity player = client.player;
            if (player != null) {
                float currentHealth = player.getHealth();
                
                // If health decreased, player took damage - update thorns timestamp
                if (lastHealth > currentHealth && lastHealth != -1 && hasThorns(player)) {
                    ArmorBarRenderer.LAST_THORNS = DetailArmorBar.getTicks();
                }
                
                lastHealth = currentHealth;
            } else {
                lastHealth = -1; // Reset when player is null
            }
        });
    }
    
    /**
     * Check if player has any armor with thorns enchantment
     */
    private static boolean hasThorns(PlayerEntity player) {
        List<ItemStack> armor = getArmorItems(player);
        for (ItemStack item : armor) {
            if (!item.isEmpty()) {
                // Use EnchantmentHelper to check for thorns enchantment
                var enchantments = EnchantmentHelper.getEnchantments(item);
                if (enchantments != null && !enchantments.isEmpty()) {
                    var entries = enchantments.getEnchantmentEntries();
                    for (var entry : entries) {
                        if (entry.getKey().getKey().orElse(null) == Enchantments.THORNS) {
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
    private static List<ItemStack> getArmorItems(PlayerEntity player) {
        List<ItemStack> list = new ArrayList<>();
        list.add(player.getEquippedStack(EquipmentSlot.HEAD));
        list.add(player.getEquippedStack(EquipmentSlot.CHEST));
        list.add(player.getEquippedStack(EquipmentSlot.LEGS));
        list.add(player.getEquippedStack(EquipmentSlot.FEET));
        return list;
    }
}