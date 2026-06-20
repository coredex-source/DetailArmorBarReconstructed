package com.redlimerl.detailab.render;

import com.redlimerl.detailab.DetailArmorBar;
import com.redlimerl.detailab.api.DetailArmorBarAPI;
import com.redlimerl.detailab.api.render.CustomArmorBar;
import com.redlimerl.detailab.config.ConfigEnumType.Animation;
import net.minecraft.resources.Identifier;
import com.redlimerl.detailab.config.ConfigEnumType.ProtectionEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if minecraft_26_2 {
import net.minecraft.client.gui.Hud;
//?} else {
/*import net.minecraft.client.gui.Gui;
*///?}
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
import net.minecraft.util.Mth;

import java.awt.*;
import java.util.List;
import java.util.*;

import static com.redlimerl.detailab.DetailArmorBar.GUI_ARMOR_BAR;
import static com.redlimerl.detailab.DetailArmorBar.getConfig;

public class ArmorBarRenderer {
    private static final class Tuple<A, B> {
        private final A a;
        private final B b;

        private Tuple(A a, B b) {
            this.a = a;
            this.b = b;
        }

        private A getA() {
            return a;
        }

        private B getB() {
            return b;
        }
    }

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

    // Per-tick cache for low durability calculations
    private static long cachedLowDurabilityTick = -1;
    private static Color cachedLowDurabilityColor = null;

    private static Color getProtectColor(int g, int p, int e, int f, int a) {
        int alpha = ArmorEffectUtils.getEffectAlpha(80, 0.65f);

        var options = getConfig().getOptions();

        if (g > 0) return ArmorEffectUtils.withAlpha(options.getProtectionColorGeneric(), alpha);
        if (p > 0) return ArmorEffectUtils.withAlpha(options.getProtectionColorProjectile(), alpha);
        if (e > 0) return ArmorEffectUtils.withAlpha(options.getProtectionColorBlast(), alpha);
        if (f > 0) return ArmorEffectUtils.withAlpha(options.getProtectionColorFire(), alpha);
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
        return ArmorEffectUtils.applyEffectAlpha(baseColor, 80, 0.65f);
    }
    
    private static Color getLowDurabilityColor() {
        long currentTick = DetailArmorBar.getTicks();
        if (currentTick != cachedLowDurabilityTick) {
            cachedLowDurabilityTick = currentTick;
            int alpha = ArmorEffectUtils.getPulsingAlpha(currentTick, ArmorEffectUtils.getAnimationSpeed(), 0.65f, true);
            cachedLowDurabilityColor = new Color(255, 25, 25, alpha);
        }
        return cachedLowDurabilityColor;
    }

    private static Color getDurabilityNotificationColor() {
        if (!getConfig().getOptions().toggleDurabilityVisualEffect || 
            !getConfig().getOptions().toggleDurabilityNotifications) {
            return null;
        }
        
        var currentLevel = com.redlimerl.detailab.events.DurabilityNotificationHandler.getCurrentWarningLevel();
        
        if (currentLevel == null) {
            return null;
        }
        
        long currentTick = DetailArmorBar.getTicks();
        long lastWarning = com.redlimerl.detailab.events.DurabilityNotificationHandler.getLastWarningTime(currentLevel);
        
        long timeSinceWarning = currentTick - lastWarning;
        
        int effectDuration = switch (currentLevel) {
            case CRITICAL -> 50;
            case LOW -> 40;
            case QUARTER -> 30;
            case HALF -> 20;
        };
        
        if (timeSinceWarning > effectDuration) {
            com.redlimerl.detailab.events.DurabilityNotificationHandler.clearCurrentWarningLevel();
            return null;
        }
        
        int speed = ArmorEffectUtils.getAnimationSpeed() / 2;
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
    //? if minecraft_26_2 {
    private final Hud hud = client.gui.hud;
    //?} else {
    /*private final Gui hud = client.gui;
    *///?}
    private static final int ARMOR_POINTS_PER_ROW = 20;
    private static final int ARMOR_SLOTS_PER_ROW = 10;
    private static final int ARMOR_ROW_HEIGHT = 10;

    private boolean hasSameProtectionEnchantments(Iterable<ItemStack> equipment) {
        if (!getConfig().getOptions().toggleUniformColor) {
            return false;
        }
        
        int[] firstLevels = null;
        
        for (ItemStack item : equipment) {
            if (!item.isEmpty()) {
                int[] levels = ArmorEffectUtils.getProtectionLevels(item);
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

    private int getRenderedArmorRows(int totalArmorPoint, boolean stackArmorBars) {
        if (totalArmorPoint <= 0) {
            return 0;
        }
        return stackArmorBars ? Mth.ceil(totalArmorPoint / (float) ARMOR_POINTS_PER_ROW) : 1;
    }

    private int getArmorRowStart(int totalArmorPoint, int rowIndex, boolean stackArmorBars) {
        int highestRowStart = Math.max(0, ((Math.max(totalArmorPoint, 1) - 1) / ARMOR_POINTS_PER_ROW) * ARMOR_POINTS_PER_ROW);
        if (!stackArmorBars || totalArmorPoint <= ARMOR_POINTS_PER_ROW) {
            return highestRowStart;
        }
        return Math.max(0, highestRowStart - (rowIndex * ARMOR_POINTS_PER_ROW));
    }

    private int getArmorRowY(int baseYPos, int rowIndex) {
        return baseYPos - (rowIndex * ARMOR_ROW_HEIGHT);
    }

    private int getArmorSlotX(int screenWidth, int slotIndex, boolean inverseSlotOrder) {
        if (inverseSlotOrder) {
            return screenWidth + (ARMOR_SLOTS_PER_ROW - 1 - slotIndex) * 8;
        }
        return screenWidth + slotIndex * 8;
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
        if (totalArmorPoint == 0 && options.toggleHideBarWithoutArmor) {
            return;
        }

        var screenWidth = client.getWindow().getGuiScaledWidth() / 2 - 91 + options.armorBarOffsetX;
        var baseYPos = y_base + options.armorBarOffsetY;
        boolean stackArmorBars = options.toggleStackArmorBars && totalArmorPoint > ARMOR_POINTS_PER_ROW;
        int totalArmorRows = totalArmorPoint > 0 ? Mth.ceil(totalArmorPoint / (float) ARMOR_POINTS_PER_ROW) : 0;
        int renderedArmorRows = getRenderedArmorRows(totalArmorPoint, stackArmorBars);
        int overflowRowCount = Math.max(0, totalArmorRows - 1);

        // Render empty armor bar if no armor is worn but toggleEmptyBar is true
        if (totalArmorPoint == 0 && options.toggleEmptyBar) {
            for (int count = 0; count < ARMOR_SLOTS_PER_ROW; count++) {
                int xPos = getArmorSlotX(screenWidth, count, options.toggleInverseSlot);
                CustomArmorBar.EMPTY.draw(ItemStack.EMPTY, context, xPos, baseYPos, false, false);
            }
        }
        
        //Default
        if (totalArmorPoint > 0) {
            for (int rowIndex = 0; rowIndex < renderedArmorRows; rowIndex++) {
                int rowStart = getArmorRowStart(totalArmorPoint, rowIndex, stackArmorBars);
                int rowEnd = Math.min(totalArmorPoint, rowStart + ARMOR_POINTS_PER_ROW);
                int rowYPos = getArmorRowY(baseYPos, rowIndex);

                for (int count = 0; count < ARMOR_SLOTS_PER_ROW; count++) {
                    int xPos = getArmorSlotX(screenWidth, count, options.toggleInverseSlot);
                    int armorIndex = rowStart + count * 2;
                    int nextArmorIndex = armorIndex + 1;

                    if (nextArmorIndex < rowEnd) {
                        Tuple<ItemStack, CustomArmorBar> am1 = armorPoints.get(armorIndex);
                        Tuple<ItemStack, CustomArmorBar> am2 = armorPoints.get(nextArmorIndex);
                        if (am1.getB() == am2.getB()) {
                            am1.getB().draw(am1.getA(), context, xPos, rowYPos, false, false);
                        } else {
                            am2.getB().draw(am2.getA(), context, xPos, rowYPos, true, true);
                            am1.getB().draw(am1.getA(), context, xPos, rowYPos, true, false);
                        }
                        if (options.toggleMending && (hasMendingEnchant(am1.getA()) || hasMendingEnchant(am2.getA()))) {
                            drawSparkleOverlay(context, xPos, rowYPos);
                        }
                    } else if (armorIndex < rowEnd) {
                        CustomArmorBar.EMPTY.draw(ItemStack.EMPTY, context, xPos, rowYPos, false, false);
                        Tuple<ItemStack, CustomArmorBar> am = armorPoints.get(armorIndex);
                        am.getB().draw(am.getA(), context, xPos, rowYPos, true, false);
                        if (options.toggleMending && hasMendingEnchant(am.getA())) {
                            drawSparkleOverlay(context, xPos, rowYPos);
                        }
                    } else {
                        CustomArmorBar.EMPTY.draw(ItemStack.EMPTY, context, xPos, rowYPos, false, false);
                    }
                }
            }

            if (!stackArmorBars && overflowRowCount > 0) {
                for (int i = 0; i < overflowRowCount; i++) {
                    CustomArmorBar.DEFAULT.draw(ItemStack.EMPTY, context, screenWidth - 7 - ((overflowRowCount - i) * 3), baseYPos, false, false);
                }
            }
        }

        // Armor Trim Overlay
        if (options.toggleArmorTrims && totalArmorPoint > 0) {
            for (int rowIndex = 0; rowIndex < renderedArmorRows; rowIndex++) {
                int rowStart = getArmorRowStart(totalArmorPoint, rowIndex, stackArmorBars);
                int rowEnd = Math.min(totalArmorPoint, rowStart + ARMOR_POINTS_PER_ROW);
                int rowYPos = getArmorRowY(baseYPos, rowIndex);

                for (int count = 0; count < ARMOR_SLOTS_PER_ROW; count++) {
                    int xPos = getArmorSlotX(screenWidth, count, options.toggleInverseSlot);
                    int armorIndex = rowStart + count * 2;
                    int nextArmorIndex = armorIndex + 1;
                    boolean hasFirstPoint = armorIndex < rowEnd;
                    boolean hasSecondPoint = nextArmorIndex < rowEnd;

                    if (hasFirstPoint || hasSecondPoint) {
                        ArmorTrimHandler.TrimMaterial firstTrimMaterial = null;
                        ArmorTrimHandler.TrimMaterial secondTrimMaterial = null;

                        if (hasFirstPoint) {
                            ItemStack armorItem = armorPoints.get(armorIndex).getA();
                            firstTrimMaterial = ArmorTrimHandler.getTrimMaterial(armorItem);
                        }

                        if (hasSecondPoint) {
                            ItemStack nextArmorItem = armorPoints.get(nextArmorIndex).getA();
                            secondTrimMaterial = ArmorTrimHandler.getTrimMaterial(nextArmorItem);
                        }

                        if (firstTrimMaterial != null && secondTrimMaterial != null) {
                            if (firstTrimMaterial == secondTrimMaterial) {
                                drawTrimOverlay(context, xPos, rowYPos, firstTrimMaterial, false, false);
                            } else {
                                drawTrimOverlay(context, xPos, rowYPos, firstTrimMaterial, true, false);
                                drawTrimOverlay(context, xPos, rowYPos, secondTrimMaterial, true, true);
                            }
                        } else if (firstTrimMaterial != null) {
                            drawTrimOverlay(context, xPos, rowYPos, firstTrimMaterial, true, false);
                        } else if (secondTrimMaterial != null) {
                            drawTrimOverlay(context, xPos, rowYPos, secondTrimMaterial, true, true);
                        }
                    }
                }
            }
        }

        // Durability HUD - shows armor icons with durability percentages in bottom left corner
        if (options.toggleDurabilityOverlay) {
            renderDurabilityHUD(context, player);
        }

        //Durability Color
        if (options.toggleDurability) {
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
                    int remainingLowDur = lowDur;
                    for (int rowIndex = 0; rowIndex < renderedArmorRows && remainingLowDur > 0; rowIndex++) {
                        int rowStart = getArmorRowStart(totalArmorPoint, rowIndex, stackArmorBars);
                        int rowEnd = Math.min(totalArmorPoint, rowStart + ARMOR_POINTS_PER_ROW);
                        int rowYPos = getArmorRowY(baseYPos, rowIndex);
                        int armorPreset = rowEnd - rowStart;
                        int halfArmors = (int) Math.ceil(armorPreset / 2.0) - 1;

                        for (int count = 0; count <= halfArmors && remainingLowDur > 0; count++) {
                            int slotIndex = halfArmors - count;
                            int xPos = getArmorSlotX(screenWidth, slotIndex, options.toggleInverseSlot);
                            Tuple<ItemStack, CustomArmorBar> am = armorPoints.get(rowStart + slotIndex * 2);
                            if (armorPreset == slotIndex * 2 + 1) {
                                if (count == 0) {
                                    am.getB().drawOutLine(am.getA(), context, xPos, rowYPos, true, false, lowDurColor);
                                    remainingLowDur--;
                                }
                            } else if (remainingLowDur == 1) {
                                am.getB().drawOutLine(am.getA(), context, xPos, rowYPos, true, true, lowDurColor);
                                remainingLowDur = 0;
                            } else {
                                am.getB().drawOutLine(am.getA(), context, xPos, rowYPos, false, false, lowDurColor);
                                remainingLowDur -= 2;
                            }
                        }
                    }
                }
            }
        }

        //Mending Color
        if (options.toggleMending && totalArmorPoint != 0) {
            var mendingTime = DetailArmorBar.getTicks() - LAST_MENDING;
            var mendingSpeed = 3;

            if (mendingTime < (mendingSpeed * 4)) {
                if (mendingTime % (mendingSpeed * 2) < mendingSpeed) {
                    for (int rowIndex = 0; rowIndex < renderedArmorRows; rowIndex++) {
                        int rowStart = getArmorRowStart(totalArmorPoint, rowIndex, stackArmorBars);
                        int rowEnd = Math.min(totalArmorPoint, rowStart + ARMOR_POINTS_PER_ROW);
                        int rowYPos = getArmorRowY(baseYPos, rowIndex);

                        for (int count = 0; count < ARMOR_SLOTS_PER_ROW; count++) {
                            int xPos = getArmorSlotX(screenWidth, count, options.toggleInverseSlot);
                            int armorIndex = rowStart + count * 2;

                            if (armorIndex >= rowEnd) {
                                if (options.toggleEmptyBar) {
                                    CustomArmorBar.DEFAULT.drawOutLine(ItemStack.EMPTY, context, xPos, rowYPos, false, false, Color.WHITE);
                                }
                            } else {
                                Tuple<ItemStack, CustomArmorBar> am = armorPoints.get(armorIndex);
                                am.getB().drawOutLine(am.getA(), context, xPos, rowYPos, false, false, Color.WHITE);
                            }
                        }
                    }
                }
            }
        }
        
        if (options.toggleDurabilityNotifications && 
            options.toggleDurabilityVisualEffect && totalArmorPoint != 0) {
            Color notificationColor = getDurabilityNotificationColor();
            
            if (notificationColor != null && notificationColor.getAlpha() > 0) {
                for (int rowIndex = 0; rowIndex < renderedArmorRows; rowIndex++) {
                    int rowStart = getArmorRowStart(totalArmorPoint, rowIndex, stackArmorBars);
                    int rowEnd = Math.min(totalArmorPoint, rowStart + ARMOR_POINTS_PER_ROW);
                    int rowYPos = getArmorRowY(baseYPos, rowIndex);
                    int maxSlots = Math.min(ARMOR_SLOTS_PER_ROW, (int) Math.ceil((rowEnd - rowStart) / 2.0));

                    for (int count = 0; count < maxSlots; count++) {
                        int xPos = getArmorSlotX(screenWidth, count, options.toggleInverseSlot);
                        int armorIndex = rowStart + count * 2;

                        if (armorIndex < rowEnd) {
                            Tuple<ItemStack, CustomArmorBar> am = armorPoints.get(armorIndex);
                            am.getB().drawOutLine(am.getA(), context, xPos, rowYPos, false, false, notificationColor);
                        }
                    }
                }
            }
        }

        //Armor Enchantments
        if (options.toggleEnchants && totalEnchants > 0 && totalArmorPoint > 0) {
            if (options.toggleAlignEnchantments) {
                // New behavior - align with armor points
                // Check if uniform color is enabled
                boolean useUniformColor = options.toggleUniformColor;
                Color baseUniformColor = useUniformColor ? options.getUniformColor() : null;
                // Apply animation to the uniform color
                Color animatedUniformColor = baseUniformColor != null ? getAnimatedUniformColor(baseUniformColor) : null;

                for (int rowIndex = 0; rowIndex < renderedArmorRows; rowIndex++) {
                    int rowStart = getArmorRowStart(totalArmorPoint, rowIndex, stackArmorBars);
                    int rowEnd = Math.min(totalArmorPoint, rowStart + ARMOR_POINTS_PER_ROW);
                    int rowYPos = getArmorRowY(baseYPos, rowIndex);
                    int displayedArmorIcons = Math.min(ARMOR_SLOTS_PER_ROW, (int) Math.ceil((rowEnd - rowStart) / 2.0));

                    for (int count = 0; count < displayedArmorIcons; count++) {
                        int xPos = getArmorSlotX(screenWidth, count, options.toggleInverseSlot);
                        int armorIndex = rowStart + count * 2;
                        int nextArmorIndex = armorIndex + 1;
                        boolean hasFirstPoint = armorIndex < rowEnd;
                        boolean hasSecondPoint = nextArmorIndex < rowEnd;

                        if (hasFirstPoint || hasSecondPoint) {
                            if (useUniformColor) {
                                if (hasFirstPoint && hasSecondPoint) {
                                    drawEnchantTexture(context, xPos, rowYPos, animatedUniformColor, 0);
                                } else if (hasFirstPoint) {
                                    drawEnchantTexture(context, xPos, rowYPos, animatedUniformColor, 2);
                                } else if (hasSecondPoint) {
                                    drawEnchantTexture(context, xPos, rowYPos, animatedUniformColor, 1);
                                }
                            } else {
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
                                                        drawEnchantTexture(context, xPos, rowYPos, getProtectColor(armorProtectArr), 0);
                                                    } else {
                                                        drawEnchantTexture(context, xPos, rowYPos, getProtectColor(armorProtectArr), 2);
                                                    }
                                                } else {
                                                    drawEnchantTexture(context, xPos, rowYPos, getProtectColor(armorProtectArr), 2);
                                                }
                                            } else {
                                                drawEnchantTexture(context, xPos, rowYPos, getProtectColor(armorProtectArr), 2);
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
                                                        drawEnchantTexture(context, xPos, rowYPos, getProtectColor(nextProtectArr), 1);
                                                    }
                                                }
                                            } else {
                                                drawEnchantTexture(context, xPos, rowYPos, getProtectColor(nextProtectArr), 1);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Original behavior - based on enchantment levels
                int maxSlots = stackArmorBars ? renderedArmorRows * ARMOR_SLOTS_PER_ROW : ARMOR_SLOTS_PER_ROW;

                for (int count = 0; count * 2 + 1 <= totalEnchants; count++) {
                    if (count >= maxSlots) break;

                    int rowIndex = stackArmorBars ? count / ARMOR_SLOTS_PER_ROW : 0;
                    int slotIndex = count % ARMOR_SLOTS_PER_ROW;
                    int xPos = getArmorSlotX(screenWidth, slotIndex, options.toggleInverseSlot);
                    int rowYPos = getArmorRowY(baseYPos, rowIndex);
                    
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
                            drawEnchantTexture(context, xPos, rowYPos, getProtectColor(protectArr), 2);
                            protectArr[min] = 0;
                            drawEnchantTexture(context, xPos, rowYPos, getProtectColor(protectArr), 1);
                            protectArr[max] -= 1;
                        } else {
                            drawEnchantTexture(context, xPos, rowYPos, getProtectColor(protectArr), 0);
                            protectArr[min] -= 2;
                        }
                    }
                    if (count * 2 + 1 == totalEnchants) {
                        drawEnchantTexture(context, xPos, rowYPos, getProtectColor(protectArr), 2);
                    }
                }
            }
        }

        //Thorns Check
        if (options.toggleThorns && thorns.level > 0 && totalArmorPoint > 0) {
            Color thornsColor = getThornColor();
            
            if (options.toggleAlignEnchantments) {
                // New behavior - align thorns with armor points (similar to protection overlay)

                for (int rowIndex = 0; rowIndex < renderedArmorRows; rowIndex++) {
                    int rowStart = getArmorRowStart(totalArmorPoint, rowIndex, stackArmorBars);
                    int rowEnd = Math.min(totalArmorPoint, rowStart + ARMOR_POINTS_PER_ROW);
                    int rowYPos = getArmorRowY(baseYPos, rowIndex);
                    int displayedArmorIcons = Math.min(ARMOR_SLOTS_PER_ROW, (int) Math.ceil((rowEnd - rowStart) / 2.0));

                    for (int count = 0; count < displayedArmorIcons; count++) {
                        int xPos = getArmorSlotX(screenWidth, count, options.toggleInverseSlot);
                        int armorIndex = rowStart + count * 2;
                        int nextArmorIndex = armorIndex + 1;
                        boolean hasFirstPoint = armorIndex < rowEnd;
                        boolean hasSecondPoint = nextArmorIndex < rowEnd;

                        if (hasFirstPoint || hasSecondPoint) {
                            boolean firstHasThorns = false;
                            boolean secondHasThorns = false;

                            if (hasFirstPoint) {
                                ItemStack armorItem = armorPoints.get(armorIndex).getA();
                                if (!armorItem.isEmpty()) {
                                    var thornLevel = getEnchantLevel(Collections.singleton(armorItem), Enchantments.THORNS);
                                    firstHasThorns = thornLevel.level > 0;
                                }
                            }

                            if (hasSecondPoint) {
                                ItemStack nextArmorItem = armorPoints.get(nextArmorIndex).getA();
                                if (!nextArmorItem.isEmpty()) {
                                    var thornLevel = getEnchantLevel(Collections.singleton(nextArmorItem), Enchantments.THORNS);
                                    secondHasThorns = thornLevel.level > 0;
                                }
                            }

                            if (firstHasThorns && secondHasThorns) {
                                InGameDrawer.drawTexture(GUI_ARMOR_BAR, context, xPos, rowYPos, 36, 18, thornsColor, false);
                            } else if (firstHasThorns) {
                                InGameDrawer.drawTexture(GUI_ARMOR_BAR, context, xPos, rowYPos, 27, 18, thornsColor, false);
                            } else if (secondHasThorns) {
                                InGameDrawer.drawTexture(GUI_ARMOR_BAR, context, xPos, rowYPos, 27, 18, thornsColor, true);
                            }
                        }
                    }
                }
            } else {
                // Original behavior - based on total thorns level
                int maxSlots = stackArmorBars ? renderedArmorRows * ARMOR_SLOTS_PER_ROW : ARMOR_SLOTS_PER_ROW;

                for (int count = 0; count < maxSlots; count++) {
                    if (count * 2 + 1 > thorns.level) break;

                    int rowIndex = stackArmorBars ? count / ARMOR_SLOTS_PER_ROW : 0;
                    int slotIndex = count % ARMOR_SLOTS_PER_ROW;
                    int xPos = getArmorSlotX(screenWidth, slotIndex, options.toggleInverseSlot);
                    int rowYPos = getArmorRowY(baseYPos, rowIndex);
                    
                    if (count * 2 + 1 < thorns.level) {
                        InGameDrawer.drawTexture(GUI_ARMOR_BAR, context, xPos, rowYPos, 36, 18, thornsColor, false);
                    }
                    if (count * 2 + 1 == thorns.level) {
                        InGameDrawer.drawTexture(GUI_ARMOR_BAR, context, xPos, rowYPos, 27, 18, thornsColor, false);
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
        float speedFactor = ArmorEffectUtils.getAnimationSpeedMultiplier();
        
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
        double fastAnimationSpeed = ArmorEffectUtils.getAnimationSpeed() / 2.5; // 2.5x faster than normal effects
        
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
        
        // Calculate base position based on preset
        int baseX, baseY;
        boolean renderUpward; // Whether to render items going upward or downward
        
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
            case BOTTOM_RIGHT:
                baseX = screenWidth - hudWidth - padding;
                baseY = screenHeight - padding;
                renderUpward = true;
                break;
            case BOTTOM_LEFT:
            default:
                baseX = padding;
                baseY = screenHeight - padding;
                renderUpward = true;
                break;
        }
        
        // Apply user offsets
        baseX += getConfig().getOptions().durabilityHudOffsetX;
        baseY += getConfig().getOptions().durabilityHudOffsetY;
        
        int yOffset = 0; // Relative offset from base position
        
        // Equipment slots in order from bottom to top: boots, leggings, chestplate, helmet
        EquipmentSlot[] armorSlots = { EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD };
        
        // Fixed spacing - icon is 16x16, add gap based on scale
        int iconSize = 16;
        int gap = (int)(4 * scale); // Gap between items scales with size
        int spacing = iconSize + gap;
        int textXOffset = iconSize + 4; // Text offset from icon
        int textYOffset = 4; // Center text vertically with icon
        
        for (EquipmentSlot slot : armorSlots) {
            ItemStack itemStack = player.getItemBySlot(slot);
            
            if (!itemStack.isEmpty() && itemStack.getMaxDamage() > 0) {
                // Calculate durability percentage
                float durabilityPercent = 1.0f - ((float)itemStack.getDamageValue() / itemStack.getMaxDamage());
                int percentage = Math.round(durabilityPercent * 100);
                
                // Move for this item based on render direction
                if (renderUpward) {
                    yOffset -= spacing;
                }
                
                // Calculate actual positions
                int xPos = baseX;
                int yPos = baseY + yOffset;
                
                // Draw the item icon (16x16)
                context.item(itemStack, xPos, yPos);
                
                // Calculate color based on durability
                int textColor = getDurabilityColor(durabilityPercent);
                
                // Draw the percentage text next to the icon
                String text = percentage + "%";
                context.text(client.font, text, xPos + textXOffset, yPos + textYOffset, textColor, true);
                
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
     *
     * @param durabilityPercent Durability percentage (0.0 to 1.0)
     * @return ARGB color value
     */
    private int getDurabilityColor(float durabilityPercent) {
        int red, green, blue;
        
        if (durabilityPercent > 0.5f) {
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
        return (255 << 24) | (red << 16) | (green << 8) | blue; // ARGB format
    }
}
