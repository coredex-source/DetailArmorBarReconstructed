package com.redlimerl.detailab.render;

import com.redlimerl.detailab.DetailArmorBar;
import com.redlimerl.detailab.config.ConfigEnumType.ProtectionEffect;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import static com.redlimerl.detailab.DetailArmorBar.getConfig;

/**
 * Renders protection type overlays on armor items in the inventory.
 * Shows the same enchantment display overlays used on the armor bar.
 */
public class InventoryArmorOverlayRenderer {
    
    public static final InventoryArmorOverlayRenderer INSTANCE = new InventoryArmorOverlayRenderer();
    
    /**
     * Gets the protection color for the item based on its enchantments.
     * Returns null if the item has no protection enchantments.
     */
    private Color getProtectionColor(ItemStack itemStack, int[] levels) {
        if (itemStack.isEmpty()) return null;
        
        int generic = levels[0];
        int projectile = levels[1];
        int explosive = levels[2];
        int fire = levels[3];
        
        if (generic == 0 && projectile == 0 && explosive == 0 && fire == 0) {
            return null;
        }

        int alpha = ArmorEffectUtils.getEffectAlpha(120, 0.75f);
        if (getConfig().getOptions().effectType == ProtectionEffect.NONE) {
            return null;
        }
        
        if (getConfig().getOptions().toggleUniformColor) {
            var baseColor = getConfig().getOptions().getUniformColor();
            return ArmorEffectUtils.withAlpha(baseColor, alpha);
        }

        var options = getConfig().getOptions();
        Color baseColor = null;
        if (generic > 0) baseColor = options.getProtectionColorGeneric();
        else if (projectile > 0) baseColor = options.getProtectionColorProjectile();
        else if (explosive > 0) baseColor = options.getProtectionColorBlast();
        else if (fire > 0) baseColor = options.getProtectionColorFire();

        if (baseColor != null) {
            return ArmorEffectUtils.withAlpha(baseColor, alpha);
        }
        return null;
    }
    
    private Color getThornsColor(ItemStack itemStack, int[] levels) {
        if (itemStack.isEmpty()) return null;
        
        int thorns = levels[4];
        if (thorns == 0) return null;

        if (getConfig().getOptions().effectType == ProtectionEffect.NONE) {
            return null;
        }

        int alpha = ArmorEffectUtils.getEffectAlpha(120, 0.75f);
        return new Color(255, 255, 255, alpha);
    }

    /**
     * Checks if the item is an armor piece that should have overlays rendered.
     */
    public boolean shouldRenderOverlay(ItemStack itemStack) {
        if (!getConfig().getOptions().toggleInventoryOverlay) return false;
        if (itemStack.isEmpty()) return false;
        
        int[] levels = ArmorEffectUtils.getProtectionLevels(itemStack);
        return getProtectionColor(itemStack, levels) != null || getThornsColor(itemStack, levels) != null;
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
    public void renderOverlay(GuiGraphicsExtractor context, ItemStack itemStack, int x, int y) {
        if (!getConfig().getOptions().toggleInventoryOverlay) return;
        if (itemStack.isEmpty()) return;
    
        int[] levels = ArmorEffectUtils.getProtectionLevels(itemStack);
        
        if (getConfig().getOptions().toggleEnchants) {
            Color protectionColor = getProtectionColor(itemStack, levels);
            if (protectionColor != null && protectionColor.getAlpha() > 0) {
                renderProtectionOverlay(context, x, y, protectionColor);
            }
        }
        
        if (getConfig().getOptions().toggleThorns) {
            Color thornsColor = getThornsColor(itemStack, levels);
            if (thornsColor != null && thornsColor.getAlpha() > 0) {
                renderThornsOverlay(context, x, y, thornsColor);
            }
        }
    }
    
    /**
     * Renders a colored border/overlay for protection enchantments.
     */
    private void renderProtectionOverlay(GuiGraphicsExtractor context, int x, int y, Color color) {
        int alpha = color.getAlpha();
        int argb = (alpha << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
        
        context.fill(x, y, x + 16, y + 1, argb);
        context.fill(x, y + 15, x + 16, y + 16, argb);
        context.fill(x, y, x + 1, y + 16, argb);
        context.fill(x + 15, y, x + 16, y + 16, argb);
        
        if (getConfig().getOptions().effectType == ProtectionEffect.AURA) {
            int innerAlpha = alpha / 2;
            int innerArgb = (innerAlpha << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
            
            context.fill(x + 1, y + 1, x + 15, y + 2, innerArgb);
            context.fill(x + 1, y + 14, x + 15, y + 15, innerArgb);
            context.fill(x + 1, y + 1, x + 2, y + 15, innerArgb);
            context.fill(x + 14, y + 1, x + 15, y + 15, innerArgb);
        }
    }

    private void renderThornsOverlay(GuiGraphicsExtractor context, int x, int y, Color color) {
        int alpha = color.getAlpha();
        int argb = (alpha << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
        context.fill(x + 13, y + 1, x + 15, y + 2, argb);
        context.fill(x + 14, y + 2, x + 15, y + 3, argb);
    }
}