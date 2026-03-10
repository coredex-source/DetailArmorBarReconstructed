package com.redlimerl.detailab.render;

import com.redlimerl.detailab.DetailArmorBar;
import com.redlimerl.detailab.config.ConfigEnumType.ProtectionEffect;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
    
    private static long cachedTick = -1;
    private static int cachedAnimationSpeed = 30;

    private static int getAnimationSpeed() {
        long currentTick = DetailArmorBar.getTicks();
        if (currentTick != cachedTick) {
            cachedTick = currentTick;
            cachedAnimationSpeed = switch (getConfig().getOptions().effectSpeed) {
                case VERY_SLOW -> 45;
                case SLOW -> 37;
                case FAST -> 23;
                case VERY_FAST -> 15;
                default -> 30;
            };
        }
        return cachedAnimationSpeed;
    }
    
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
        } else if (getConfig().getOptions().effectType == ProtectionEffect.STATIC) {
            alpha = Math.round(0.75f * 255); // Static outline at constant 75% opacity
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
    
    private Color getThornsColor(ItemStack itemStack, int[] levels) {
        if (itemStack.isEmpty()) return null;
        
        int thorns = levels[4];
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
        } else if (getConfig().getOptions().effectType == ProtectionEffect.STATIC) {
            alpha = Math.round(0.75f * 255); // Static outline at constant 75% opacity
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
    
        int[] levels = getProtectionLevels(itemStack);
        
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