package com.redlimerl.detailab.events;

import com.redlimerl.detailab.DetailArmorBar;
import com.redlimerl.detailab.render.ArmorBarRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

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
            Player player = client.player;
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
    private static boolean hasThorns(Player player) {
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