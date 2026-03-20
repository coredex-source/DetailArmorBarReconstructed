package com.redlimerl.detailab.render;

import com.redlimerl.detailab.DetailArmorBar;
import com.redlimerl.detailab.api.DetailArmorBarAPI;
import com.redlimerl.detailab.api.render.CustomArmorBar;
import com.redlimerl.detailab.config.ConfigEnumType.Animation;
import net.minecraft.resources.Identifier;
import com.redlimerl.detailab.config.ConfigEnumType.ProtectionEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Gui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Tuple;
import net.minecraft.util.Mth;

import java.awt.*;
import java.util.List;
import java.util.*;

import static com.redlimerl.detailab.DetailArmorBar.GUI_ARMOR_BAR;
import static com.redlimerl.detailab.DetailArmorBar.getConfig;

public class ArmorBarRenderer {
    static class LevelData {
        int level;
        int count;
        LevelData(int level, int count) {
            this.level = level;
            this.count = count;
        }
    }

    public static final ArmorBarRenderer INSTANCE = new ArmorBarRenderer();
    public static long LAST_THORNS = 0L;
    public static long LAST_MENDING = 0L;

    // Per-tick cache for animation calculations
    private static long cachedTick = -1;
    private static int cachedAnimationSpeed = 30;
    private static Color cachedLowDurabilityColor = null;
    
    private static void updateAnimationCache() {
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
            int speed = cachedAnimationSpeed;
            long time = currentTick;
            int alpha;
            if (time % (speed*4L) >= (speed*2L)) alpha = 0;
            else if (time % (speed*2L) < speed)
                alpha = Math.round(Mth.lerp((time % speed) / (speed - 1f), 0f, 0.65f) * 255);
            else alpha = Math.round(Mth.lerp((time % speed) / (speed - 1f), 0.65f, 0f) * 255);
            cachedLowDurabilityColor = new Color(255, 25, 25, alpha);
        }
    }

    private static int getAnimationSpeed() {
        updateAnimationCache();
        return cachedAnimationSpeed;
    }

    private static Color getProtectColor(int g, int p, int e, int f, int a) {
        int speed = getAnimationSpeed();
        int alpha;
        if (getConfig().getOptions().effectType == ProtectionEffect.AURA) alpha = 80;
        else if (getConfig().getOptions().effectType == ProtectionEffect.OUTLINE) {
            long time = DetailArmorBar.getTicks();
            if (time % (speed*4L) < (speed*2L)) alpha = 0;
            else if (time % (speed*2L) < speed)
                alpha = Math.round(Mth.lerp((time % speed) / (speed - 1f), 0f, 0.65f) * 255);
            else alpha = Math.round(Mth.lerp((time % speed) / (speed - 1f), 0.65f, 0f) * 255);
        } else if (getConfig().getOptions().effectType == ProtectionEffect.STATIC) {
            alpha = Math.round(0.65f * 255); // Static outline at constant 65% opacity
        } else alpha = 0;

        if (g > 0) return new Color(153, 255, 255, alpha);
        if (p > 0) return new Color(112, 51, 173, alpha);
        if (e > 0) return new Color(255, 255, 0, alpha);
        if (f > 0) return new Color(210, 56, 0, alpha);
        if (a > 0) return new Color(255, 255, 255, alpha);
        return Color.WHITE;
    }

    // Make sure this method is properly defined
    private static Color getProtectColor(int[] s) {
        if (s == null || s.length < 5) {
            return Color.WHITE;
        }
        return getProtectColor(s[0], s[1], s[2], s[3], s[4]);
    }
    
    // Add this new method to apply animations to uniform color
    private static Color getAnimatedUniformColor(Color baseColor) {
        int speed = getAnimationSpeed();
        int alpha;
        
        if (getConfig().getOptions().effectType == ProtectionEffect.AURA) {
            alpha = 80; // Same as in getProtectColor
        } else if (getConfig().getOptions().effectType == ProtectionEffect.OUTLINE) {
            long time = DetailArmorBar.getTicks();
            if (time % (speed*4L) < (speed*2L)) {
                alpha = 0;
            } else if (time % (speed*2L) < speed) {
                alpha = Math.round(Mth.lerp((time % speed) / (speed - 1f), 0f, 0.65f) * 255);
            } else {
                alpha = Math.round(Mth.lerp((time % speed) / (speed - 1f), 0.65f, 0f) * 255);
            }
        } else if (getConfig().getOptions().effectType == ProtectionEffect.STATIC) {
            alpha = Math.round(0.65f * 255); // Static outline at constant 65% opacity
        } else {
            alpha = 0;
        }
        
        // Apply the calculated alpha to the base color while preserving its RGB values
        return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);
    }
    
    private static Color getLowDurabilityColor() {
        updateAnimationCache();
        return cachedLowDurabilityColor;
    }

    private static Color getDurabilityNotificationColor() {
        if (!getConfig().getOptions().toggleDurabilityVisualEffect || 
            !getConfig().getOptions().toggleDurabilityNotifications) {
            return null;
        }
        
        var handler = com.redlimerl.detailab.events.DurabilityNotificationHandler.class;
        var currentLevel = com.redlimerl.detailab.events.DurabilityNotificationHandler.CURRENT_WARNING_LEVEL;
        
        if (currentLevel == null) {
            return null;
        }
        
        long currentTick = DetailArmorBar.getTicks();
        long lastWarning = switch (currentLevel) {
            case HALF -> com.redlimerl.detailab.events.DurabilityNotificationHandler.LAST_WARNING_50;
            case QUARTER -> com.redlimerl.detailab.events.DurabilityNotificationHandler.LAST_WARNING_25;
            case LOW -> com.redlimerl.detailab.events.DurabilityNotificationHandler.LAST_WARNING_10;
            case CRITICAL -> com.redlimerl.detailab.events.DurabilityNotificationHandler.LAST_WARNING_5;
        };
        
        long timeSinceWarning = currentTick - lastWarning;
        
        int effectDuration = switch (currentLevel) {
            case CRITICAL -> 50;
            case LOW -> 40;
            case QUARTER -> 30;
            case HALF -> 20;
        };
        
        if (timeSinceWarning > effectDuration) {
            com.redlimerl.detailab.events.DurabilityNotificationHandler.CURRENT_WARNING_LEVEL = null;
            return null;
        }
        
        int speed = getAnimationSpeed() / 2;
        if (currentLevel == com.redlimerl.detailab.config.ConfigEnumType.DurabilityThreshold.CRITICAL) {
            speed = speed / 2;
        }
        
        long time = currentTick;
        int alpha;
        if (time % (speed * 2L) < speed) {
            alpha = Math.round(Mth.lerp((time % speed) / (speed - 1f), 0f, 0.85f) * 255);
        } else {
            alpha = Math.round(Mth.lerp((time % speed) / (speed - 1f), 0.85f, 0f) * 255);
        }
        
        return currentLevel.getColorWithAlpha(alpha);
    }

    private static Color getThornColor() {
        if (getConfig().getOptions().effectThorn == Animation.STATIC) return Color.WHITE;
        
        long currentTime = DetailArmorBar.getTicks();
        long timeSinceLastHit = currentTime - LAST_THORNS;
        int totalDuration;

        switch (getConfig().getOptions().effectSpeed) {
            case VERY_SLOW -> totalDuration = 20;
            case SLOW -> totalDuration = 16;
            case FAST -> totalDuration = 8;
            case VERY_FAST -> totalDuration = 4;
            default -> totalDuration = 12;
        }
        
        // Calculate phase durations proportionally
        int phase1Duration = totalDuration / 2;  // First half: bright red
        int phase2Duration = phase1Duration + (totalDuration / 4);  // Next quarter: slightly dimmer
        
        // If no recent hit or animation completed, return static color
        if (LAST_THORNS == 0 || timeSinceLastHit > totalDuration) {
            return Color.WHITE;
        }
        
        // Create a quick flash effect with duration based on speed setting
        
        // First phase: bright red
        if (timeSinceLastHit < phase1Duration) {
            return new Color(255, 0, 0);
        } 
        // Second phase: slightly dimmer
        else if (timeSinceLastHit < phase2Duration) {
            return new Color(255, 60, 60);
        }
        // Last phase: fading out
        else {
            return new Color(255, 150, 150);
        }
    }

    private static Map<ResourceKey<Enchantment>, LevelData> getEnchantments(Iterable<ItemStack> equipment) {
        HashMap<ResourceKey<Enchantment>, LevelData> result = new HashMap<>();

        for (ItemStack itemStack : equipment) {
            if (!itemStack.isEmpty()) {
                EnchantmentHelper.getEnchantmentsForCrafting(itemStack).entrySet().forEach(enchantment -> {
                    ResourceKey<Enchantment> enchantType = enchantment.getKey().unwrapKey().orElse(null);
                    LevelData enchantData = result.getOrDefault(enchantType, new LevelData(0, 0));
                    enchantData.count++;
                    enchantData.level += enchantment.getIntValue();
                    if (enchantType == Enchantments.THORNS) enchantData.level += enchantment.getIntValue() - 1;
                    result.put(enchantType, enchantData);
                });
            }
        }

        return result;
    }

    private static LevelData getEnchantLevel(Iterable<ItemStack> equipment, ResourceKey<Enchantment> type) {
        return getEnchantments(equipment).getOrDefault(type, new LevelData(0, 0));
    }

    /**
     * Efficiently gets all protection-related enchantment levels for a single item in one pass.
     */
    private static int[] getProtectionLevels(ItemStack itemStack) {
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

    private int getLowDurabilityItem(Iterable<Tuple<EquipmentSlot, ItemStack>> equipment) {
        var count = 0;
        for (Tuple<EquipmentSlot, ItemStack> pair : equipment) {
            ItemStack itemStack = pair.getB();
            EquipmentSlot slot = pair.getA();
            if (!itemStack.isEmpty()) {
                if (itemStack.getMaxDamage() != 0 && ((itemStack.getDamageValue() * 100f) / (itemStack.getMaxDamage() * 100f)) >= 0.92f) {
                    if(itemStack.has(DataComponents.ATTRIBUTE_MODIFIERS)) {
                        ItemAttributeModifiers component = itemStack.get(DataComponents.ATTRIBUTE_MODIFIERS);
                        assert component != null;
                        for (var attr : component.modifiers()) {
                            if (attr.attribute().equals(Attributes.ARMOR) && attr.slot().slots().contains(slot)) {
                                count += (int) attr.modifier().amount();
                                break;
                            }
                        }
                    }
                }
            }
        }
        return count;
    }

    private static List<Tuple<ItemStack, CustomArmorBar>> getArmorPoints(Player player) {
        ArrayList<Tuple<ItemStack, CustomArmorBar>> armorPoints = new ArrayList<>();
        ArrayList<Tuple<ItemStack, CustomArmorBar>> itemPoints = new ArrayList<>();
        int sumArmor = 0;

        // Stats from equipment
        for (var slot : EquipmentSlot.VALUES) {
            var itemStack = player.getItemBySlot(slot);
            if(itemStack.isEmpty()){
                continue;
            }

            var component = itemStack.get(DataComponents.ATTRIBUTE_MODIFIERS);
            if (component != null) {
                // Handle regular armor items (assign a type based on their material)
                CustomArmorBar barData = getConfig().getOptions().toggleArmorTypes
                    ? DetailArmorBarAPI.getArmorBarList().getOrDefault(itemStack.getItem(), CustomArmorBar.DEFAULT)
                    : CustomArmorBar.DEFAULT;

                var defense = ArmorBarUtils.getDefense(itemStack, slot);
                sumArmor += defense;
                for (int i = 0; i < defense; i++) {
                    armorPoints.add(new Tuple<>(itemStack, barData));
                }
            }

            // Special items (equippable with effects not described by the attribute system).
            if (getConfig().getOptions().toggleItemBar && DetailArmorBarAPI.getItemBarList().containsKey(itemStack.getItem())) {
                // Only show items on the bar if they are unequippable or equipped to the correct slot.
                var equippableComponent = itemStack.get(DataComponents.EQUIPPABLE);
                if(!(equippableComponent == null || equippableComponent.slot() == slot)){ continue; }

                var barData = DetailArmorBarAPI.getItemBarList().get(itemStack.getItem());
                var pair = new Tuple<>(itemStack, barData);
                if(getConfig().getOptions().toggleSortSpecialItem){ // add later
                    itemPoints.add(pair); // left half
                    itemPoints.add(pair); // right half
                }else{ // add now
                    if (armorPoints.size() % 2 == 1) armorPoints.add(new Tuple<>(ItemStack.EMPTY, CustomArmorBar.EMPTY));
                    armorPoints.add(pair); // left half
                    armorPoints.add(pair); // right half
                }
            }
        }

        // Base stats
        var baseArmor = player.getAttributeBaseValue(Attributes.ARMOR);
        sumArmor += baseArmor;
        for (int i = 0; i < baseArmor; i++) {
            armorPoints.add(new Tuple<>(ItemStack.EMPTY, CustomArmorBar.DEFAULT));
        }

        // Add items on second if that was the set option
        if(getConfig().getOptions().toggleSortSpecialItem){
            if (armorPoints.size() % 2 == 1) armorPoints.add(new Tuple<>(ItemStack.EMPTY, CustomArmorBar.EMPTY));
            armorPoints.addAll(itemPoints);
        }

        return armorPoints;
    }

    private final Minecraft client = Minecraft.getInstance();
    private final Gui hud = client.gui;

    private boolean hasSameProtectionEnchantments(Iterable<ItemStack> equipment) {
        if (!getConfig().getOptions().toggleUniformColor) {
            return false;
        }

        int[] firstLevels = null;
        for (ItemStack item : equipment) {
            if (!item.isEmpty()) {
                int[] levels = getProtectionLevels(item);
                if (firstLevels == null) {
                    firstLevels = levels;
                } else {
                    if (levels[0] != firstLevels[0] || levels[1] != firstLevels[1] ||
                            levels[2] != firstLevels[2] || levels[3] != firstLevels[3]) {
                        return false;
                    }
                }
            }
        }

        return firstLevels != null;
    }

    public void render(GuiGraphicsExtractor context, Player player, int y_base) {
        var options = getConfig().getOptions();
        
        var armorItems = ArmorBarUtils.getArmorItems(player);
        var generic = getEnchantLevel(armorItems, Enchantments.PROTECTION);
        var projectile = getEnchantLevel(armorItems, Enchantments.PROJECTILE_PROTECTION);
        var explosive = getEnchantLevel(armorItems, Enchantments.BLAST_PROTECTION);
        var fire = getEnchantLevel(armorItems, Enchantments.FIRE_PROTECTION);
        var protectArr = new int[] { generic.level + generic.count, projectile.level, explosive.level, fire.level, 0 };
        var armorPoints = getArmorPoints(player);
        var thorns = getEnchantLevel(armorItems, Enchantments.THORNS);

        var totalArmorPoint = armorPoints.size();
        var totalEnchants = Arrays.stream(protectArr).sum();

        // Hide armor bar completely when no armor is worn to maintain vanilla parity
        if (totalArmorPoint == 0 && getConfig().getOptions().toggleHideBarWithoutArmor) {
            return;
        }

        var screenWidth = client.getWindow().getGuiScaledWidth() / 2 - 91 + getConfig().getOptions().armorBarOffsetX;
        var yPos = y_base + getConfig().getOptions().armorBarOffsetY;

        int stackCount = (totalArmorPoint - 1) / 20;
        int stackRow = stackCount * 20;

        // Render empty armor bar if no armor is worn but toggleEmptyBar is true
        if (totalArmorPoint == 0 && getConfig().getOptions().toggleEmptyBar) {
            for (int count = 0; count < 10; count++) {
                int xPos;
                if (getConfig().getOptions().toggleInverseSlot) {
                    xPos = screenWidth + (9 - count) * 8;
                } else {
                    xPos = screenWidth + count * 8;
                }
                CustomArmorBar.EMPTY.draw(ItemStack.EMPTY, context, xPos, yPos, false, false);
            }
        }
        
        //Default
        if (totalArmorPoint > 0) {
            int maxSlots = 10;

            for (int count = 0; count < maxSlots; count++) {
                // Calculate xPos based on inverse slot setting
                int xPos;
                if (getConfig().getOptions().toggleInverseSlot) {
                    // For inverse order (right to left), start from right and move left
                    xPos = screenWidth + (9 - count) * 8;
                } else {
                    // Normal order (left to right)
                    xPos = screenWidth + count * 8;
                }

                if (count * 2 + 1 + stackRow < totalArmorPoint) {
                    Tuple<ItemStack, CustomArmorBar> am1 = armorPoints.get(count * 2 + stackRow);
                    Tuple<ItemStack, CustomArmorBar> am2 = armorPoints.get(count * 2 + 1 + stackRow);
                    if (am1.getB() == am2.getB()) {
                        am1.getB().draw(am1.getA(), context, xPos, yPos, false, false);
                    } else {
                        am2.getB().draw(am2.getA(), context, xPos, yPos, true, true);
                        am1.getB().draw(am1.getA(), context, xPos, yPos, true, false);
                    }
                    // Draw sparkle overlay for items with mending
                    if (getConfig().getOptions().toggleMending && (hasMendingEnchant(am1.getA()) || hasMendingEnchant(am2.getA()))) {
                        drawSparkleOverlay(context, xPos, yPos);
                    }
                }
                if (count * 2 + 1 + stackRow == totalArmorPoint) {
                    CustomArmorBar.EMPTY.draw(ItemStack.EMPTY, context, xPos, yPos, false, false);
                    Tuple<ItemStack, CustomArmorBar> am = armorPoints.get(count * 2 + stackRow);
                    am.getB().draw(am.getA(), context, xPos, yPos, true, false);
                    // Draw sparkle overlay for item with mending
                    if (getConfig().getOptions().toggleMending && hasMendingEnchant(am.getA())) {
                        drawSparkleOverlay(context, xPos, yPos);
                    }
                }
                if (count * 2 + 1 + stackRow > totalArmorPoint) {
                    CustomArmorBar.EMPTY.draw(ItemStack.EMPTY, context, xPos, yPos, false, false);
                }
            }

            if (armorPoints.size() > 20) {
                for (int i = 0; i < stackCount; i++) {
                    CustomArmorBar.DEFAULT.draw(ItemStack.EMPTY, context, screenWidth - 7 - ((stackCount - i)*3), yPos, false, false);
                }
            }
        }

        // Armor Trim Overlay
        if (getConfig().getOptions().toggleArmorTrims && totalArmorPoint > 0) {
            int maxSlots = 10;
            
            for (int count = 0; count < maxSlots; count++) {
                int xPos;
                if (getConfig().getOptions().toggleInverseSlot) {
                    xPos = screenWidth + (9 - count) * 8;
                } else {
                    xPos = screenWidth + count * 8;
                }
                
                int armorIndex = count * 2 + stackRow;
                int nextArmorIndex = armorIndex + 1;
                boolean hasFirstPoint = armorIndex < totalArmorPoint;
                boolean hasSecondPoint = nextArmorIndex < totalArmorPoint;
                
                if (hasFirstPoint || hasSecondPoint) {
                    ArmorTrimHandler.TrimMaterial firstTrimMaterial = null;
                    ArmorTrimHandler.TrimMaterial secondTrimMaterial = null;
                    
                    // Check if first armor point has a trim
                    if (hasFirstPoint) {
                        ItemStack armorItem = armorPoints.get(armorIndex).getA();
                        firstTrimMaterial = ArmorTrimHandler.getTrimMaterial(armorItem);
                    }
                    
                    // Check if second armor point has a trim
                    if (hasSecondPoint) {
                        ItemStack nextArmorItem = armorPoints.get(nextArmorIndex).getA();
                        secondTrimMaterial = ArmorTrimHandler.getTrimMaterial(nextArmorItem);
                    }
                    
                    // Draw trim overlay based on which armor points have trims
                    if (firstTrimMaterial != null && secondTrimMaterial != null) {
                        // Both points have trims
                        if (firstTrimMaterial == secondTrimMaterial) {
                            // Same trim material - draw full overlay
                            drawTrimOverlay(context, xPos, yPos, firstTrimMaterial, false, false);
                        } else {
                            // Different trim materials - draw half overlays
                            drawTrimOverlay(context, xPos, yPos, firstTrimMaterial, true, false);
                            drawTrimOverlay(context, xPos, yPos, secondTrimMaterial, true, true);
                        }
                    } else if (firstTrimMaterial != null) {
                        // Only first point has trim - draw left half
                        drawTrimOverlay(context, xPos, yPos, firstTrimMaterial, true, false);
                    } else if (secondTrimMaterial != null) {
                        // Only second point has trim - draw right half (mirrored)
                        drawTrimOverlay(context, xPos, yPos, secondTrimMaterial, true, true);
                    }
                }
            }
        }

        // Durability HUD - shows armor icons with durability percentages in bottom left corner
        if (getConfig().getOptions().toggleDurabilityOverlay) {
            renderDurabilityHUD(context, player);
        }

        //Durability Color
        if (getConfig().getOptions().toggleDurability) {
            List<Tuple<EquipmentSlot, ItemStack>> equipment = new ArrayList<>();

            for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
                ItemStack itemStack = player.getItemBySlot(equipmentSlot);
                Equippable equippableComponent = itemStack.get(DataComponents.EQUIPPABLE);
                if (equippableComponent != null && equippableComponent.slot() == equipmentSlot) {
                    equipment.add(new Tuple<>(equipmentSlot, itemStack));
                }
            }

            int lowDur = getLowDurabilityItem(equipment);

            if (totalArmorPoint != 0 && lowDur != 0) {
                Color lowDurColor = getLowDurabilityColor();
                if (lowDurColor.getAlpha() != 0) {
                    int armorPreset = ((totalArmorPoint - 1) % 20) + 1;
                    int halfArmors = (int) Math.ceil(armorPreset / 2.0) - 1;
                    for (int count = 0; count <= halfArmors; count++) {
                        if (lowDur <= 0) break;

                        // Calculate xPos based on inverse slot setting
                        int xPos;
                        if (getConfig().getOptions().toggleInverseSlot) {
                            xPos = screenWidth + (9 - (halfArmors - count)) * 8;
                        } else {
                            xPos = screenWidth + (halfArmors - count) * 8;
                        }
                        
                        Tuple<ItemStack, CustomArmorBar> am = armorPoints.get((halfArmors - count) * 2 + stackRow);
                        if (armorPreset == (halfArmors - count) * 2 + 1) {
                            if (count == 0) {
                                am.getB().drawOutLine(am.getA(), context, xPos, yPos, true, false, lowDurColor);
                                lowDur--;
                            }
                        } else {
                            if (lowDur == 1) {
                                am.getB().drawOutLine(am.getA(), context, xPos, yPos, true, true, lowDurColor);
                                lowDur = 0;
                            } else {
                                am.getB().drawOutLine(am.getA(), context, xPos, yPos, false, false, lowDurColor);
                                lowDur -= 2;
                            }
                        }
                    }
                }
            }
        }

        //Mending Color
        if (getConfig().getOptions().toggleMending && totalArmorPoint != 0) {
            var mendingTime = DetailArmorBar.getTicks() - LAST_MENDING;
            var mendingSpeed = 3;

            if (mendingTime < (mendingSpeed * 4)) {
                int maxSlots = 10;
                    
                for (int count = 0; count < maxSlots; count++) {
                    if (mendingTime % (mendingSpeed * 2) < mendingSpeed) {
                        // Calculate xPos based on inverse slot setting
                        int xPos;
                        if (getConfig().getOptions().toggleInverseSlot) {
                            xPos = screenWidth + (9 - count) * 8;
                        } else {
                            xPos = screenWidth + count * 8;
                        }

                        if (armorPoints.size() <= count * 2 + stackRow) {
                            if (getConfig().getOptions().toggleEmptyBar)
                                CustomArmorBar.DEFAULT.drawOutLine(ItemStack.EMPTY, context, xPos, yPos, false, false, Color.WHITE);
                        } else {
                            Tuple<ItemStack, CustomArmorBar> am = armorPoints.get(count * 2 + stackRow);
                            am.getB().drawOutLine(am.getA(), context, xPos, yPos, false, false, Color.WHITE);
                        }
                    }
                }
            }
        }
        
        if (getConfig().getOptions().toggleDurabilityNotifications && 
            getConfig().getOptions().toggleDurabilityVisualEffect && totalArmorPoint != 0) {
            Color notificationColor = getDurabilityNotificationColor();
            
            if (notificationColor != null && notificationColor.getAlpha() > 0) {
                int maxSlots = Math.min(10, (int) Math.ceil(totalArmorPoint / 2.0));
                
                for (int count = 0; count < maxSlots; count++) {
                    int xPos;
                    if (getConfig().getOptions().toggleInverseSlot) {
                        xPos = screenWidth + (9 - count) * 8;
                    } else {
                        xPos = screenWidth + count * 8;
                    }
                    
                    if (count * 2 + stackRow < armorPoints.size()) {
                        Tuple<ItemStack, CustomArmorBar> am = armorPoints.get(count * 2 + stackRow);
                        am.getB().drawOutLine(am.getA(), context, xPos, yPos, false, false, notificationColor);
                    }
                }
            }
        }

        //Armor Enchantments
        if (getConfig().getOptions().toggleEnchants && totalEnchants > 0 && totalArmorPoint > 0) {
            if (getConfig().getOptions().toggleAlignEnchantments) {
                // New behavior - align with armor points
                int displayedArmorIcons = Math.min(10, (int)Math.ceil(totalArmorPoint / 2.0));
                
                // Check if uniform color is enabled
                boolean useUniformColor = getConfig().getOptions().toggleUniformColor;
                Color baseUniformColor = useUniformColor ? getConfig().getOptions().uniformColorType.getColor() : null;
                // Apply animation to the uniform color
                Color animatedUniformColor = baseUniformColor != null ? getAnimatedUniformColor(baseUniformColor) : null;
                
                for (int count = 0; count < displayedArmorIcons; count++) {
                    // Calculate xPos based on inverse slot setting
                    int xPos;
                    if (getConfig().getOptions().toggleInverseSlot) {
                        xPos = screenWidth + (9 - count) * 8;
                    } else {
                        xPos = screenWidth + count * 8;
                    }
                    
                    int armorIndex = count * 2 + stackRow;
                    int nextArmorIndex = armorIndex + 1;
                    boolean hasFirstPoint = armorIndex < totalArmorPoint;
                    boolean hasSecondPoint = nextArmorIndex < totalArmorPoint;
                    
                    if (hasFirstPoint || hasSecondPoint) {
                        if (useUniformColor) {
                            // Use animated uniform color for all armor points
                            if (hasFirstPoint && hasSecondPoint) {
                                drawEnchantTexture(context, xPos, yPos, animatedUniformColor, 0);
                            } else if (hasFirstPoint) {
                                drawEnchantTexture(context, xPos, yPos, animatedUniformColor, 2);
                            } else if (hasSecondPoint) {
                                drawEnchantTexture(context, xPos, yPos, animatedUniformColor, 1);
                            }
                        } else {
                            // Original per-piece coloring logic
                            if (hasFirstPoint) {
                                ItemStack armorItem = armorPoints.get(armorIndex).getA();
                                if (!armorItem.isEmpty()) {
                                    var armorGeneric = getEnchantLevel(Collections.singleton(armorItem), Enchantments.PROTECTION);
                                    var armorProjectile = getEnchantLevel(Collections.singleton(armorItem), Enchantments.PROJECTILE_PROTECTION);
                                    var armorExplosive = getEnchantLevel(Collections.singleton(armorItem), Enchantments.BLAST_PROTECTION);
                                    var armorFire = getEnchantLevel(Collections.singleton(armorItem), Enchantments.FIRE_PROTECTION);
                                    var armorProtectArr = new int[] { armorGeneric.level, armorProjectile.level, armorExplosive.level, armorFire.level, 0 };
                                    if (Arrays.stream(armorProtectArr).sum() > 0) {
                                        if (hasSecondPoint) {
                                            ItemStack nextArmorItem = armorPoints.get(nextArmorIndex).getA();
                                            if (!nextArmorItem.isEmpty()) {
                                                var nextGeneric = getEnchantLevel(Collections.singleton(nextArmorItem), Enchantments.PROTECTION);
                                                var nextProjectile = getEnchantLevel(Collections.singleton(nextArmorItem), Enchantments.PROJECTILE_PROTECTION);
                                                var nextExplosive = getEnchantLevel(Collections.singleton(nextArmorItem), Enchantments.BLAST_PROTECTION);
                                                var nextFire = getEnchantLevel(Collections.singleton(nextArmorItem), Enchantments.FIRE_PROTECTION);
                                                if (armorGeneric.level == nextGeneric.level && 
                                                    armorProjectile.level == nextProjectile.level &&
                                                    armorExplosive.level == nextExplosive.level &&
                                                    armorFire.level == nextFire.level) {
                                                    drawEnchantTexture(context, xPos, yPos, getProtectColor(armorProtectArr), 0);
                                                } else {
                                                    drawEnchantTexture(context, xPos, yPos, getProtectColor(armorProtectArr), 2);
                                                }
                                            } else {
                                                drawEnchantTexture(context, xPos, yPos, getProtectColor(armorProtectArr), 2);
                                            }
                                        } else {
                                            drawEnchantTexture(context, xPos, yPos, getProtectColor(armorProtectArr), 2);
                                        }
                                    }
                                }
                            }
                            
                            if (hasSecondPoint) {
                                ItemStack nextArmorItem = armorPoints.get(nextArmorIndex).getA();
                                if (!nextArmorItem.isEmpty()) {
                                    var nextGeneric = getEnchantLevel(Collections.singleton(nextArmorItem), Enchantments.PROTECTION);
                                    var nextProjectile = getEnchantLevel(Collections.singleton(nextArmorItem), Enchantments.PROJECTILE_PROTECTION);
                                    var nextExplosive = getEnchantLevel(Collections.singleton(nextArmorItem), Enchantments.BLAST_PROTECTION);
                                    var nextFire = getEnchantLevel(Collections.singleton(nextArmorItem), Enchantments.FIRE_PROTECTION);
                                    var nextProtectArr = new int[] { nextGeneric.level, nextProjectile.level, nextExplosive.level, nextFire.level, 0 };
                                    
                                    if (Arrays.stream(nextProtectArr).sum() > 0) {
                                        if (hasFirstPoint) {
                                            ItemStack armorItem = armorPoints.get(armorIndex).getA();
                                            if (!armorItem.isEmpty()) {
                                                var armorGeneric = getEnchantLevel(Collections.singleton(armorItem), Enchantments.PROTECTION);
                                                var armorProjectile = getEnchantLevel(Collections.singleton(armorItem), Enchantments.PROJECTILE_PROTECTION);
                                                var armorExplosive = getEnchantLevel(Collections.singleton(armorItem), Enchantments.BLAST_PROTECTION);
                                                var armorFire = getEnchantLevel(Collections.singleton(armorItem), Enchantments.FIRE_PROTECTION);
                                                
                                                if (armorGeneric.level != nextGeneric.level || 
                                                    armorProjectile.level != nextProjectile.level ||
                                                    armorExplosive.level != nextExplosive.level ||
                                                    armorFire.level != nextFire.level) {
                                                    drawEnchantTexture(context, xPos, yPos, getProtectColor(nextProtectArr), 1);
                                                }
                                            }
                                        } else {
                                            drawEnchantTexture(context, xPos, yPos, getProtectColor(nextProtectArr), 1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Original behavior - based on enchantment levels
                int maxSlots = 10;
                    
                for (int count = 0; count * 2 + 1 <= totalEnchants; count++) {
                    if (count >= maxSlots) break;

                    // Calculate xPos based on inverse slot setting
                    int xPos;
                    if (getConfig().getOptions().toggleInverseSlot) {
                        xPos = screenWidth + (9 - count) * 8;
                    } else {
                        xPos = screenWidth + count * 8;
                    }
                    
                    if (count * 2 + 1 < totalEnchants) {
                        var min = -1;
                        var max = -1;
                        for (int pw = 0; pw < 5; pw++) {
                            if (min == -1 && protectArr[pw] > 1) {
                                min = pw;
                                break;
                            } else if (min == -1 && protectArr[pw] == 1) {
                                min = pw;
                            } else if (min != -1 && max == -1 && protectArr[pw] >= 1) max = pw;
                        }
                        if (min != -1 && max != -1) {
                            drawEnchantTexture(context, xPos, yPos, getProtectColor(protectArr), 2);
                            protectArr[min] = 0;
                            drawEnchantTexture(context, xPos, yPos, getProtectColor(protectArr), 1);
                            protectArr[max] -= 1;
                        } else {
                            drawEnchantTexture(context, xPos, yPos, getProtectColor(protectArr), 0);
                            protectArr[min] -= 2;
                        }
                    }
                    if (count * 2 + 1 == totalEnchants) {
                        drawEnchantTexture(context, xPos, yPos, getProtectColor(protectArr), 2);
                    }
                }
            }
        }

        //Thorns Check
        if (getConfig().getOptions().toggleThorns && thorns.level > 0 && totalArmorPoint > 0) {
            Color thornsColor = getThornColor();
            
            if (getConfig().getOptions().toggleAlignEnchantments) {
                // New behavior - align thorns with armor points (similar to protection overlay)
                int displayedArmorIcons = Math.min(10, (int)Math.ceil(totalArmorPoint / 2.0));
                
                for (int count = 0; count < displayedArmorIcons; count++) {
                    // Calculate xPos based on inverse slot setting
                    int xPos;
                    if (getConfig().getOptions().toggleInverseSlot) {
                        xPos = screenWidth + (9 - count) * 8;
                    } else {
                        xPos = screenWidth + count * 8;
                    }
                    
                    int armorIndex = count * 2 + stackRow;
                    int nextArmorIndex = armorIndex + 1;
                    boolean hasFirstPoint = armorIndex < totalArmorPoint;
                    boolean hasSecondPoint = nextArmorIndex < totalArmorPoint;
                    
                    if (hasFirstPoint || hasSecondPoint) {
                        boolean firstHasThorns = false;
                        boolean secondHasThorns = false;
                        
                        // Check if first armor point has thorns
                        if (hasFirstPoint) {
                            ItemStack armorItem = armorPoints.get(armorIndex).getA();
                            if (!armorItem.isEmpty()) {
                                var thornLevel = getEnchantLevel(Collections.singleton(armorItem), Enchantments.THORNS);
                                firstHasThorns = thornLevel.level > 0;
                            }
                        }
                        
                        // Check if second armor point has thorns
                        if (hasSecondPoint) {
                            ItemStack nextArmorItem = armorPoints.get(nextArmorIndex).getA();
                            if (!nextArmorItem.isEmpty()) {
                                var thornLevel = getEnchantLevel(Collections.singleton(nextArmorItem), Enchantments.THORNS);
                                secondHasThorns = thornLevel.level > 0;
                            }
                        }
                        
                        // Draw thorns overlay based on which armor points have thorns
                        if (firstHasThorns && secondHasThorns) {
                            // Both points have thorns - draw full icon
                            InGameDrawer.drawTexture(GUI_ARMOR_BAR, context, xPos, yPos, 36, 18, thornsColor, false);
                        } else if (firstHasThorns) {
                            // Only first point has thorns - draw left half
                            InGameDrawer.drawTexture(GUI_ARMOR_BAR, context, xPos, yPos, 27, 18, thornsColor, false);
                        } else if (secondHasThorns) {
                            // Only second point has thorns - draw right half
                            InGameDrawer.drawTexture(GUI_ARMOR_BAR, context, xPos, yPos, 27, 18, thornsColor, true);
                        }
                    }
                }
            } else {
                // Original behavior - based on total thorns level
                int maxSlots = 10;
                    
                for (int count = 0; count < maxSlots; count++) {
                    if (count * 2 + 1 > thorns.level) break;

                    // Calculate xPos based on inverse slot setting
                    int xPos;
                    if (getConfig().getOptions().toggleInverseSlot) {
                        xPos = screenWidth + (9 - count) * 8;
                    } else {
                        xPos = screenWidth + count * 8;
                    }
                    
                    if (count * 2 + 1 < thorns.level) {
                        InGameDrawer.drawTexture(GUI_ARMOR_BAR, context, xPos, yPos, 36, 18, thornsColor, false);
                    }
                    if (count * 2 + 1 == thorns.level) {
                        InGameDrawer.drawTexture(GUI_ARMOR_BAR, context, xPos, yPos, 27, 18, thornsColor, false);
                    }
                }
            }
        }
    }

    private void drawEnchantTexture(GuiGraphicsExtractor context, int x, int y, Color color, int half) {
        // Apply the animation speed calculation for aura mode as well
        int u = 0;
        int v = 0;
        
        // Calculate animation speed factor based on the config
        float speedFactor = 1.0f;
        switch (getConfig().getOptions().effectSpeed) {
            case VERY_SLOW -> speedFactor = 0.5f;
            case SLOW -> speedFactor = 0.75f;
            case FAST -> speedFactor = 1.25f;
            case VERY_FAST -> speedFactor = 1.5f;
        }
        
        // Apply speed factor to the animation timing
        var tickDivisor = Math.max(1, Math.round(3.0f / speedFactor));
        var t = (hud.getGuiTicks() / tickDivisor) % 36;

        if (getConfig().getOptions().effectType == ProtectionEffect.AURA) {
            if (t < 12) {
                u = (t % 12) * 9;
                v = 27 + (half * 9);
            }
        } else if (getConfig().getOptions().effectType == ProtectionEffect.OUTLINE) {
            u = 9 + (half * 9);
        } else if (getConfig().getOptions().effectType == ProtectionEffect.STATIC) {
            u = 9 + (half * 9); // Same texture position as outline
        } else return;

        InGameDrawer.drawTexture(GUI_ARMOR_BAR, context, x, y, u, v, color, false);
    }

    // Returns true if the item has the mending enchantment
    private boolean hasMendingEnchant(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return getEnchantLevel(Collections.singleton(stack), Enchantments.MENDING).level > 0;
    }

    // Draws a sparkle overlay at the given position
    private void drawSparkleOverlay(GuiGraphicsExtractor context, int x, int y) {
        long currentTicks = DetailArmorBar.getTicks();
        double fastAnimationSpeed = getAnimationSpeed() / 2.5; // 2.5x faster than normal effects
        
        // Star 1: Top-left area, slower animation
        int star1Cycle = (int) ((currentTicks / fastAnimationSpeed) % 12);
        if (star1Cycle < 6) {
            drawStar(context, x + 1, y + 1, star1Cycle % 3);
        }
        
        // Star 2: Center-right area, medium speed (offset by 4 ticks)
        int star2Cycle = (int) (((currentTicks + 4) / (fastAnimationSpeed * 0.8)) % 12);
        if (star2Cycle < 6) {
            drawStar(context, x + 6, y + 3, star2Cycle % 3);
        }
        
        // Star 3: Bottom-left area, faster animation (offset by 8 ticks)
        int star3Cycle = (int) (((currentTicks + 8) / (fastAnimationSpeed * 0.6)) % 12);
        if (star3Cycle < 6) {
            drawStar(context, x + 2, y + 6, star3Cycle % 3);
        }
    }
    
    private void drawStar(GuiGraphicsExtractor context, int x, int y, int frame) {
        int alpha = 255;
        if (frame == 0) alpha = 100;
        else if (frame == 1) alpha = 200;
        else if (frame == 2) alpha = 255;
        
        int color = (alpha << 24) | 0xFFFFFF; // White with varying alpha
        
        // Draw a 5-pointed star pattern
        // Center pixel
        context.fill(x + 1, y + 1, x + 2, y + 2, color);
        
        // Cross pattern
        context.fill(x + 1, y, x + 2, y + 1, color);     // Top
        context.fill(x + 1, y + 2, x + 2, y + 3, color); // Bottom
        context.fill(x, y + 1, x + 1, y + 2, color);     // Left
        context.fill(x + 2, y + 1, x + 3, y + 2, color); // Right
    }
    
    /**
     * A description for my or anyone else's future reference as I tend to forget details over time(sooner rather than later).
     * Draws the armor trim overlay on an armor bar icon.
     * The overlay texture (armor_trim.png) is 18x18 with 4 parts:
     * - (0,0): Full armor overlay (9x9)
     * - (9,0): Half armor overlay (9x9)
     * - (0,9): Elytra/special overlay (9x9)
     * - (9,9): Reserved/unused (9x9)
     *
     * @param context The draw context
     * @param x X position
     * @param y Y position
     * @param material The trim material (used to get colored texture)
     * @param isHalf Whether to draw half overlay
     * @param isMirror Whether to mirror the overlay (for right half)
     */
    private void drawTrimOverlay(GuiGraphicsExtractor context, int x, int y, ArmorTrimHandler.TrimMaterial material, boolean isHalf, boolean isMirror) {
        int u = isHalf ? 9 : 0;
        int v = 0;
        Identifier textureId = ArmorTrimHandler.getColoredTexture(material);
        InGameDrawer.drawTexture(textureId, context, x, y, u, v, 18, 18, Color.WHITE, isMirror);
    }

    /**
     * Renders a durability HUD in the bottom left corner of the screen.
     * Shows each equipped armor piece with its icon and durability percentage.
     * Format:
     * [helmet_icon] 100%
     * [chestplate_icon] 100%
     * [leggings_icon] 100%
     * [boots_icon] 100%
     *
     * @param context The draw context
     * @param player The player whose armor to display
     */
    private void renderDurabilityHUD(GuiGraphicsExtractor context, Player player) {
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        float scale = getConfig().getOptions().durabilityHudScale;
        var position = getConfig().getOptions().durabilityHudPosition;
        
        int baseX = 0;
        int baseY = 0;
        boolean renderUpward = false; // Whether to render items going upward or downward
        
        int padding = 5; // Padding from screen edges
        int hudWidth = (int)(50 * scale); // Approximate width of icon + text
        int hudHeight = (int)(18 * 4 * scale); // Max height for 4 armor pieces
        
        switch (position) {
            case TOP_LEFT:
                baseX = padding;
                baseY = padding;
                renderUpward = false;
                break;
            case TOP_RIGHT:
                baseX = screenWidth - hudWidth - padding;
                baseY = padding;
                renderUpward = false;
                break;
            case BOTTOM_LEFT:
                baseX = padding;
                baseY = screenHeight - padding;
                renderUpward = true;
                break;
            case BOTTOM_RIGHT:
                baseX = screenWidth - hudWidth - padding;
                baseY = screenHeight - padding;
                renderUpward = true;
                break;
        }
        
        // Apply user offsets
        baseX += getConfig().getOptions().durabilityHudOffsetX;
        baseY += getConfig().getOptions().durabilityHudOffsetY;
        
        // Equipment slots in order from bottom to top: boots, leggings, chestplate, helmet
        EquipmentSlot[] armorSlots = { EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD };
        
        // Fixed spacing - icon is 16x16, add gap based on scale
        int iconSize = 16;
        int gap = (int)(4 * scale); // Gap between items scales with size
        int spacing = iconSize + gap;
        int textXOffset = iconSize + 4; // Text offset from icon
        
        int yOffset = 0;
        
        for (EquipmentSlot slot : armorSlots) {
            ItemStack itemStack = player.getItemBySlot(slot);
            if (!itemStack.isEmpty() && itemStack.getMaxDamage() > 0) {
                float durabilityPercent = 1.0f - ((float)itemStack.getDamageValue() / itemStack.getMaxDamage());
                int percentage = Math.round(durabilityPercent * 100);
                
                // Move for this item based on render direction
                if (renderUpward) {
                    yOffset -= spacing;
                }
                
                // Calculate actual positions
                int xPos = baseX;
                int yPos = baseY + yOffset;
                
                // Render the item icon
                context.item(itemStack, xPos, yPos);
                
                // Render durability percentage text
                int textColor = getDurabilityColor(durabilityPercent);
                int textYOffset = (iconSize - 8) / 2; // Center text vertically with icon (8 is font height)
                context.text(client.font, percentage + "%", xPos + textXOffset, yPos + textYOffset, textColor, true);
                
                // Move down if rendering downward
                if (!renderUpward) {
                    yOffset += spacing;
                }
            }
        }
    }
    
    /**
     * Gets the color for durability text based on percentage.
     * White (100%) -> Yellow (50%) -> Red (0%)
     */
    private int getDurabilityColor(float durabilityPercent) {
        int red, green, blue;
        
        if (durabilityPercent >= 0.5f) {
            // White to Yellow transition (100% to 50%)
            // White (255,255,255) -> Yellow (255,255,0)
            float t = (durabilityPercent - 0.5f) * 2; // 1.0 at 100%, 0.0 at 50%
            red = 255;
            green = 255;
            blue = (int)(255 * t);
        } else {
            // Yellow to Red transition (50% to 0%)
            // Yellow (255,255,0) -> Red (255,0,0)
            float t = durabilityPercent * 2; // 1.0 at 50%, 0.0 at 0%
            red = 255;
            green = (int)(255 * t);
            blue = 0;
        }
        
        return (255 << 24) | (red << 16) | (green << 8) | blue;
    }
}