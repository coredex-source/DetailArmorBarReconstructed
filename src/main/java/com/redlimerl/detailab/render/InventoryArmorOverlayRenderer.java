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

import java.awt.Color;

import static com.redlimerl.detailab.DetailArmorBar.getConfig;

/**
 * Renders protection type overlays on armor items in the inventory.
 * Shows the same enchantment effects as the armor bar (protection, projectile, blast, fire, thorns).
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
     * Gets the protection color for an item based on its enchantments.
     * Returns null if no protection enchantment effect should be shown.
     */
    private Color getProtectionColor(ItemStack itemStack, int[] levels) {
        int generic = levels[0];
        int projectile = levels[1];
        int explosive = levels[2];
        int fire = levels[3];
        
        int speed = getAnimationSpeed();
        int alpha;
        
        if (getConfig().getOptions().effectType == ProtectionEffect.AURA) {
            alpha = 80;
        } else if (getConfig().getOptions().effectType == ProtectionEffect.OUTLINE) {
            long time = DetailArmorBar.getTicks();
            if (time % (speed * 4L) < (speed * 2L)) alpha = 0;
            else if (time % (speed * 2L) < speed)
                alpha = Math.round(Mth.lerp((time % speed) / (speed - 1f), 0f, 0.75f) * 255);
            else alpha = Math.round(Mth.lerp((time % speed) / (speed - 1f), 0.75f, 0f) * 255);
        } else {
            return null;
        }
        
        if (getConfig().getOptions().toggleUniformColor) {
            var baseColor = getConfig().getOptions().uniformColorType.getColor();
            return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);
        }
        
        if (generic > 0) return new Color(153, 255, 255, alpha);
        if (projectile > 0) return new Color(112, 51, 173, alpha);
        if (explosive > 0) return new Color(255, 255, 0, alpha);
        if (fire > 0) return new Color(210, 56, 0, alpha);
        return null;
    }
    
    /**
     * Gets the thorns color for an item if it has the thorns enchantment.
     * Returns null if no thorns effect should be shown.
     */
    private Color getThornsColor(int thornsLevel) {
        if (thornsLevel <= 0) return null;
        
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
        
        return new Color(255, 255, 255, alpha);
    }

    private int[] getProtectionLevels(ItemStack itemStack) {
        int[] levels = new int[5];
        if (itemStack.isEmpty()) return levels;
        
        EnchantmentHelper.getEnchantmentsForCrafting(itemStack).entrySet().forEach(enchantment -> {
            ResourceKey<Enchantment> type = enchantment.getKey().unwrapKey().orElse(null);
            if (type == Enchantments.PROTECTION) levels[0] = enchantment.getIntValue();
            else if (type == Enchantments.PROJECTILE_PROTECTION) levels[1] = enchantment.getIntValue();
            else if (type == Enchantments.BLAST_PROTECTION) levels[2] = enchantment.getIntValue();
            else if (type == Enchantments.FIRE_PROTECTION) levels[3] = enchantment.getIntValue();
            else if (type == Enchantments.THORNS) levels[4] = enchantment.getIntValue();
        });
        return levels;
    }
    
    /**
     * Checks if the item is an armor piece that should have overlays rendered.
     */
    public boolean shouldRenderOverlay(ItemStack itemStack) {
        if (!getConfig().getOptions().toggleInventoryOverlay) return false;
        if (itemStack.isEmpty()) return false;
        
        int[] levels = getProtectionLevels(itemStack);
        return getProtectionColor(itemStack, levels) != null || getThornsColor(levels[4]) != null;
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
    
        int[] levels = getProtectionLevels(itemStack);
        
        if (getConfig().getOptions().toggleEnchants) {
            Color protectionColor = getProtectionColor(itemStack, levels);
            if (protectionColor != null) {
                renderBorderOverlay(context, x, y, protectionColor);
            }
        }
        
        if (getConfig().getOptions().toggleThorns) {
            Color thornsColor = getThornsColor(levels[4]);
            if (thornsColor != null) {
                renderThornsIndicator(context, x, y, thornsColor);
            }
        }
    }
    
    /**
     * Renders a colored border around the item slot.
     */
    private void renderBorderOverlay(GuiGraphics context, int x, int y, Color color) {
        int alpha = color.getAlpha();
        int argb = (alpha << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
        
        context.fill(x, y, x + 16, y + 1, argb);
        context.fill(x, y + 15, x + 16, y + 16, argb);
        context.fill(x, y, x + 1, y + 16, argb);
        context.fill(x + 15, y, x + 16, y + 16, argb);
        
        // Inner border for more visibility
        if (alpha > 50) {
            int innerAlpha = alpha / 2;
            int innerArgb = (innerAlpha << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
            
            context.fill(x + 1, y + 1, x + 15, y + 2, innerArgb);
            context.fill(x + 1, y + 14, x + 15, y + 15, innerArgb);
            context.fill(x + 1, y + 1, x + 2, y + 15, innerArgb);
            context.fill(x + 14, y + 1, x + 15, y + 15, innerArgb);
        }
    }
    
    /**
     * Renders a small thorns indicator in the top-right corner of the slot.
     */
    private void renderThornsIndicator(GuiGraphics context, int x, int y, Color color) {
        int alpha = color.getAlpha();
        int argb = (alpha << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
        context.fill(x + 13, y + 1, x + 15, y + 2, argb);
        context.fill(x + 14, y + 2, x + 15, y + 3, argb);
    }
}
