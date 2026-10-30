package com.redlimerl.detailab.render;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.awt.Color;

import static com.redlimerl.detailab.DetailArmorBar.getConfig;

final class ArmorEffectUtils {

    private static long cachedTick = -1L;
    private static int cachedAnimationSpeed = 30;

    private ArmorEffectUtils() {
    }

    static int getAnimationSpeed() {
        long currentTick = com.redlimerl.detailab.DetailArmorBar.getTicks();
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

    static float getAnimationSpeedMultiplier() {
        return switch (getConfig().getOptions().effectSpeed) {
            case VERY_SLOW -> 0.5f;
            case SLOW -> 0.75f;
            case FAST -> 1.25f;
            case VERY_FAST -> 1.5f;
            default -> 1.0f;
        };
    }

    static int getEffectAlpha(int auraAlpha, float peakOutlineAlpha) {
        return switch (getConfig().getOptions().effectType) {
            case AURA -> auraAlpha;
            case OUTLINE -> getPulsingAlpha(com.redlimerl.detailab.DetailArmorBar.getTicks(), getAnimationSpeed(), peakOutlineAlpha, false);
            case STATIC -> Math.round(peakOutlineAlpha * 255);
            case NONE -> 0;
        };
    }

    static int getPulsingAlpha(long time, int speed, float peakAlpha, boolean visibleFirstHalf) {
        long phase = time % (speed * 4L);
        boolean hiddenWindow = visibleFirstHalf ? phase >= (speed * 2L) : phase < (speed * 2L);
        if (hiddenWindow) {
            return 0;
        }
        float progress = (time % speed) / (speed - 1f);
        if (time % (speed * 2L) < speed) {
            return Math.round(Mth.lerp(progress, 0f, peakAlpha) * 255);
        }
        return Math.round(Mth.lerp(progress, peakAlpha, 0f) * 255);
    }

    static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    static Color applyEffectAlpha(Color baseColor, int auraAlpha, float peakOutlineAlpha) {
        return withAlpha(baseColor, getEffectAlpha(auraAlpha, peakOutlineAlpha));
    }

    static int[] getProtectionLevels(ItemStack itemStack) {
        int[] levels = new int[5];
        if (itemStack.isEmpty()) {
            return levels;
        }

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
}