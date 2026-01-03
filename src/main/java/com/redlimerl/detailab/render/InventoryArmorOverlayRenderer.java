package com.redlimerl.detailab.render;

import com.redlimerl.detailab.DetailArmorBar;
import com.redlimerl.detailab.config.ConfigEnumType.ProtectionEffect;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.awt.*;
import java.util.Collections;

import static com.redlimerl.detailab.DetailArmorBar.GUI_ARMOR_BAR;
import static com.redlimerl.detailab.DetailArmorBar.getConfig;

/**
 * Renders protection type overlays on armor items in the inventory.
 * Shows the same enchantment display overlays used on the armor bar.
 */
public class InventoryArmorOverlayRenderer {
    
    public static final InventoryArmorOverlayRenderer INSTANCE = new InventoryArmorOverlayRenderer();
    
    private static int getAnimationSpeed() {
        return switch (getConfig().getOptions().effectSpeed) {
            case VERY_SLOW -> 45;
            case SLOW -> 37;
            case FAST -> 23;
            case VERY_FAST -> 15;
            default -> 30;
        };
    }
    
    /**
     * Gets the protection color for the item based on its enchantments.
     * Returns null if the item has no protection enchantments.
     */
    private Color getProtectionColor(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        
        var generic = getEnchantLevel(itemStack, Enchantments.PROTECTION);
        var projectile = getEnchantLevel(itemStack, Enchantments.PROJECTILE_PROTECTION);
        var explosive = getEnchantLevel(itemStack, Enchantments.BLAST_PROTECTION);
        var fire = getEnchantLevel(itemStack, Enchantments.FIRE_PROTECTION);
        
        // Check if item has any protection enchantment
        if (generic == 0 && projectile == 0 && explosive == 0 && fire == 0) {
            return null;
        }
        
        int speed = getAnimationSpeed();
        int alpha;
        
        if (getConfig().getOptions().effectType == ProtectionEffect.AURA) {
            alpha = 120; // Slightly more visible in inventory
        } else if (getConfig().getOptions().effectType == ProtectionEffect.OUTLINE) {
            long time = DetailArmorBar.getTicks();
            if (time % (speed * 4L) < (speed * 2L)) alpha = 0;
            else if (time % (speed * 2L) < speed)
                alpha = Math.round(Mth.lerp((time % speed) / (speed - 1f), 0f, 0.75f) * 255);
            else alpha = Math.round(Mth.lerp((time % speed) / (speed - 1f), 0.75f, 0f) * 255);
        } else {
            return null; // No effect when set to NONE
        }
        
        // Return color based on priority: Generic > Projectile > Blast > Fire
        if (getConfig().getOptions().toggleUniformColor) {
            var baseColor = getConfig().getOptions().uniformColorType.getColor();
            return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);
        }
        
        if (generic > 0) return new Color(153, 255, 255, alpha);  // Aqua - Protection
        if (projectile > 0) return new Color(112, 51, 173, alpha); // Purple - Projectile Protection
        if (explosive > 0) return new Color(255, 255, 0, alpha);   // Yellow - Blast Protection
        if (fire > 0) return new Color(210, 56, 0, alpha);         // Orange - Fire Protection
        
        return null;
    }
    
    /**
     * Gets the thorns color if the item has thorns enchantment.
     */
    private Color getThornsColor(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        
        var thorns = getEnchantLevel(itemStack, Enchantments.THORNS);
        if (thorns == 0) return null;
        
        int speed = getAnimationSpeed();
        int alpha;
        
        if (getConfig().getOptions().effectType == ProtectionEffect.AURA) {
            alpha = 120;
        } else if (getConfig().getOptions().effectType == ProtectionEffect.OUTLINE) {
            long time = DetailArmorBar.getTicks();
            if (time % (speed * 4L) < (speed * 2L)) alpha = 0;
            else if (time % (speed * 2L) < speed)
                alpha = Math.round(Mth.lerp((time % speed) / (speed - 1f), 0f, 0.75f) * 255);
            else alpha = Math.round(Mth.lerp((time % speed) / (speed - 1f), 0.75f, 0f) * 255);
        } else {
            return null;
        }
        
        return new Color(255, 255, 255, alpha); // White for thorns
    }
    
    private int getEnchantLevel(ItemStack itemStack, ResourceKey<Enchantment> type) {
        final int[] level = {0};
        EnchantmentHelper.getEnchantmentsForCrafting(itemStack).entrySet().forEach(enchantment -> {
            ResourceKey<Enchantment> enchantType = enchantment.getKey().unwrapKey().orElse(null);
            if (enchantType == type) {
                level[0] = enchantment.getIntValue();
            }
        });
        return level[0];
    }
    
    /**
     * Checks if the item is an armor piece that should have overlays rendered.
     */
    public boolean shouldRenderOverlay(ItemStack itemStack) {
        if (!getConfig().getOptions().toggleInventoryOverlay) return false;
        if (itemStack.isEmpty()) return false;
        
        // Check if it's in the armor bar list or has protection/thorns enchants
        return getProtectionColor(itemStack) != null || getThornsColor(itemStack) != null;
    }
    
    /**
     * Renders the protection type overlay on an armor item in inventory.
     * Called after the item is rendered.
     * 
     * @param context The GUI graphics context
     * @param itemStack The item being rendered
     * @param x X position of the item slot
     * @param y Y position of the item slot
     */
    public void renderOverlay(GuiGraphics context, ItemStack itemStack, int x, int y) {
        if (!getConfig().getOptions().toggleInventoryOverlay) return;
        if (itemStack.isEmpty()) return;
        
        // Render protection overlay
        if (getConfig().getOptions().toggleEnchants) {
            Color protectionColor = getProtectionColor(itemStack);
            if (protectionColor != null && protectionColor.getAlpha() > 0) {
                renderProtectionOverlay(context, x, y, protectionColor);
            }
        }
        
        // Render thorns overlay
        if (getConfig().getOptions().toggleThorns) {
            Color thornsColor = getThornsColor(itemStack);
            if (thornsColor != null && thornsColor.getAlpha() > 0) {
                renderThornsOverlay(context, x, y, thornsColor);
            }
        }
    }
    
    /**
     * Renders a colored border/overlay for protection enchantments.
     */
    private void renderProtectionOverlay(GuiGraphics context, int x, int y, Color color) {
        int alpha = color.getAlpha();
        int argb = (alpha << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
        
        // Draw a subtle border around the item slot (16x16 slot)
        // Top border
        context.fill(x, y, x + 16, y + 1, argb);
        // Bottom border
        context.fill(x, y + 15, x + 16, y + 16, argb);
        // Left border
        context.fill(x, y, x + 1, y + 16, argb);
        // Right border
        context.fill(x + 15, y, x + 16, y + 16, argb);
        
        // Additional inner highlight for aura effect
        if (getConfig().getOptions().effectType == ProtectionEffect.AURA) {
            int innerAlpha = alpha / 2;
            int innerArgb = (innerAlpha << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
            
            // Inner top
            context.fill(x + 1, y + 1, x + 15, y + 2, innerArgb);
            // Inner bottom
            context.fill(x + 1, y + 14, x + 15, y + 15, innerArgb);
            // Inner left
            context.fill(x + 1, y + 1, x + 2, y + 15, innerArgb);
            // Inner right
            context.fill(x + 14, y + 1, x + 15, y + 15, innerArgb);
        }
    }
    
    /**
     * Renders a thorns indicator in the corner of the item slot.
     */
    private void renderThornsOverlay(GuiGraphics context, int x, int y, Color color) {
        int alpha = color.getAlpha();
        int argb = (alpha << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
        
        // Draw a small thorns indicator in the top-right corner
        // Creates a small triangle/spike pattern
        context.fill(x + 13, y + 1, x + 15, y + 2, argb);
        context.fill(x + 14, y + 2, x + 15, y + 3, argb);
    }
}
