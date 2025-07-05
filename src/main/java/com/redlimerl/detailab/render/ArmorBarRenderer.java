package com.redlimerl.detailab.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.redlimerl.detailab.DetailArmorBar;
import com.redlimerl.detailab.api.DetailArmorBarAPI;
import com.redlimerl.detailab.api.render.CustomArmorBar;
import com.redlimerl.detailab.config.ConfigEnumType.Animation;
import com.redlimerl.detailab.config.ConfigEnumType.ProtectionEffect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;

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


    private static int getAnimationSpeed() {
        return switch (getConfig().getOptions().effectSpeed) {
            case VERY_SLOW -> 45;
            case SLOW -> 37;
            case FAST -> 23;
            case VERY_FAST -> 15;
            default -> 30;
        };
    }

    private static Color getProtectColor(int g, int p, int e, int f, int a) {
        int speed = getAnimationSpeed();
        int alpha;
        if (getConfig().getOptions().effectType == ProtectionEffect.AURA) alpha = 80;
        else if (getConfig().getOptions().effectType == ProtectionEffect.OUTLINE) {
            long time = DetailArmorBar.getTicks();
            if (time % (speed*4L) < (speed*2L)) alpha = 0;
            else if (time % (speed*2L) < speed)
                alpha = Math.round(MathHelper.lerp((time % speed) / (speed - 1f), 0f, 0.65f) * 255);
            else alpha = Math.round(MathHelper.lerp((time % speed) / (speed - 1f), 0.65f, 0f) * 255);
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
                alpha = Math.round(MathHelper.lerp((time % speed) / (speed - 1f), 0f, 0.65f) * 255);
            } else {
                alpha = Math.round(MathHelper.lerp((time % speed) / (speed - 1f), 0.65f, 0f) * 255);
            }
        } else {
            alpha = 0;
        }
        
        // Apply the calculated alpha to the base color while preserving its RGB values
        return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);
    }
    
    private static Color getLowDurabilityColor() {
        int speed = getAnimationSpeed();
        long time = DetailArmorBar.getTicks();
        int alpha;
        if (time % (speed*4L) >= (speed*2L)) alpha = 0;
        else if (time % (speed*2L) < speed)
            alpha = Math.round(MathHelper.lerp((time % speed) / (speed - 1f), 0f, 0.65f) * 255);
        else alpha = Math.round(MathHelper.lerp((time % speed) / (speed - 1f), 0.65f, 0f) * 255);

        return new Color(255, 25, 25, alpha);
    }

    private static Color getThornColor() {
        if (getConfig().getOptions().effectThorn == Animation.STATIC) return Color.WHITE;
        
        // Check if player was recently hit
        long currentTime = DetailArmorBar.getTicks();
        long timeSinceLastHit = currentTime - LAST_THORNS;
        
        // Set total duration based on animation speed setting
        int totalDuration;
        switch (getConfig().getOptions().effectSpeed) {
            case VERY_SLOW -> totalDuration = 20;
            case SLOW -> totalDuration = 16;
            case FAST -> totalDuration = 8;
            case VERY_FAST -> totalDuration = 4;
            default -> totalDuration = 12; // NORMAL speed
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

    private static Map<RegistryKey<Enchantment>, LevelData> getEnchantments(Iterable<ItemStack> equipment) {
        HashMap<RegistryKey<Enchantment>, LevelData> result = new HashMap<>();

        for (ItemStack itemStack : equipment) {
            if (!itemStack.isEmpty()) {
                EnchantmentHelper.getEnchantments(itemStack).getEnchantmentEntries().forEach(enchantment -> {
                    RegistryKey<Enchantment> enchantType = enchantment.getKey().getKey().orElse(null);
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

    private static LevelData getEnchantLevel(Iterable<ItemStack> equipment, RegistryKey<Enchantment> type) {
        return getEnchantments(equipment).getOrDefault(type, new LevelData(0, 0));
    }

    private int getLowDurabilityItem(Iterable<Pair<EquipmentSlot, ItemStack>> equipment) {
        var count = 0;
        for (Pair<EquipmentSlot, ItemStack> pair : equipment) {
            ItemStack itemStack = pair.getRight();
            EquipmentSlot slot = pair.getLeft();
            if (!itemStack.isEmpty()) {
                if (itemStack.getMaxDamage() != 0 && ((itemStack.getDamage() * 100f) / (itemStack.getMaxDamage() * 100f)) >= 0.92f) {
                    if(itemStack.contains(DataComponentTypes.ATTRIBUTE_MODIFIERS)) {
                        AttributeModifiersComponent component = itemStack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
                        assert component != null;
                        count += component.modifiers().stream()
                                .filter((attr) -> attr.attribute().equals(EntityAttributes.ARMOR) && attr.slot().getSlots().contains(slot))
                                .findFirst()
                                .map(x -> x.modifier().value())
                                .orElse(0.0);
                    }
                }
            }
        }
        return count;
    }

    private static List<Pair<ItemStack, CustomArmorBar>> getArmorPoints(PlayerEntity player) {
        ArrayList<Pair<ItemStack, CustomArmorBar>> armorItem = new ArrayList<>();
        Stack<Pair<EquipmentSlot, ItemStack>> equipment = new Stack<>();

        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            ItemStack itemStack = player.getEquippedStack(equipmentSlot);
            EquippableComponent equippableComponent = itemStack.get(DataComponentTypes.EQUIPPABLE);
            
            // Check if item has equippable component OR if it's a special item like elytra
            boolean isEquippable = equippableComponent != null && equippableComponent.slot() == equipmentSlot;
            boolean isSpecialItem = !itemStack.isEmpty() && DetailArmorBarAPI.getItemBarList().containsKey(itemStack.getItem());
            
            if (isEquippable || isSpecialItem) {
                if (getConfig().getOptions().toggleInverseSlot) {
                    equipment.push(new Pair<>(equipmentSlot, itemStack));
                } else {
                    equipment.add(new Pair<>(equipmentSlot, itemStack));
                }
            }
        }

        EntityAttributeInstance attribute = player.getAttributeInstance(EntityAttributes.ARMOR);
        if (attribute != null) {
            double d = attribute.getBaseValue();
            for (int i = 0; i < d; i++) {
                armorItem.add(new Pair<>(ItemStack.EMPTY, CustomArmorBar.DEFAULT));
            }
        }

        for (Pair<EquipmentSlot, ItemStack> pair : equipment) {
            ItemStack itemStack = pair.getRight();
            EquipmentSlot slot = pair.getLeft();
            if (!itemStack.isEmpty()) {
                var component = itemStack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
                if (component != null) {
                    // Handle regular armor items
                    CustomArmorBar barData;
                    if (getConfig().getOptions().toggleArmorTypes) {
                        barData = DetailArmorBarAPI.getArmorBarList().getOrDefault(itemStack.getItem(), CustomArmorBar.DEFAULT);
                    }
//                    else if (getConfig().getOptions().toggleNetherites) {
//                        barData = DetailArmorBarAPI.getArmorBarList().getOrDefault(armor, CustomArmorBar.DEFAULT);
//                    }
                    else {
                        barData = CustomArmorBar.DEFAULT;
                    }

                    for (int i = 0; i < getDefense(itemStack, slot); i++) {
                        armorItem.add(new Pair<>(itemStack, barData));
                    }
                } else if (getConfig().getOptions().toggleItemBar && DetailArmorBarAPI.getItemBarList().containsKey(itemStack.getItem())) {
                    // Handle special items like elytra
                    if (!getConfig().getOptions().toggleSortSpecialItem) {
                        if (armorItem.size() % 2 == 1)
                            armorItem.add(new Pair<>(ItemStack.EMPTY, CustomArmorBar.EMPTY));

                        var barData = DetailArmorBarAPI.getItemBarList().get(itemStack.getItem());
                        armorItem.add(new Pair<>(itemStack, barData));
                        armorItem.add(new Pair<>(itemStack, barData));
                    }
                }
            }
        }

        if (getConfig().getOptions().toggleItemBar && getConfig().getOptions().toggleSortSpecialItem) {
            for (Pair<EquipmentSlot, ItemStack> pair : equipment) {
                ItemStack itemStack = pair.getRight();
                EquipmentSlot slot = pair.getLeft();
                if (!itemStack.isEmpty() && DetailArmorBarAPI.getItemBarList().containsKey(itemStack.getItem())) {
                    if (armorItem.size() % 2 == 1)
                        armorItem.add(new Pair<>(ItemStack.EMPTY, CustomArmorBar.EMPTY));

                    var barData = DetailArmorBarAPI.getItemBarList().get(itemStack.getItem());
                    armorItem.add(new Pair<>(itemStack, barData));
                    armorItem.add(new Pair<>(itemStack, barData));
                }
            }
        }
        return armorItem;
    }

    private static int getDefense(ItemStack itemStack, EquipmentSlot slot) {
        AttributeModifiersComponent modifier = itemStack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        for (AttributeModifiersComponent.Entry entry : modifier.modifiers()) {
            if (entry.slot().matches(slot) && entry.attribute().equals(EntityAttributes.ARMOR)) {
                return (int) entry.modifier().value();
            }
        }
        return 0;
    }



    private final MinecraftClient client = MinecraftClient.getInstance();
    private final InGameHud hud = client.inGameHud;

    private Iterable<ItemStack> getArmorItems(PlayerEntity player) {
        var list = new ArrayList<ItemStack>();
        Optional.ofNullable(player.getEquippedStack(EquipmentSlot.HEAD))
                .ifPresent(list::add);
        Optional.ofNullable(player.getEquippedStack(EquipmentSlot.CHEST))
                .ifPresent(list::add);
        Optional.ofNullable(player.getEquippedStack(EquipmentSlot.LEGS))
                .ifPresent(list::add);
        Optional.ofNullable(player.getEquippedStack(EquipmentSlot.FEET))
                .ifPresent(list::add);
        return list;
    }

    // Add this helper method to check if all armor pieces have the same protection enchantments
    private boolean hasSameProtectionEnchantments(Iterable<ItemStack> equipment) {
        if (!getConfig().getOptions().toggleUniformColor) {
            return false;
        }
        
        List<ItemStack> armorPieces = new ArrayList<>();
        equipment.forEach(item -> {
            if (!item.isEmpty()) {
                armorPieces.add(item);
            }
        });
        
        if (armorPieces.isEmpty()) {
            return false;
        }
        
        // Get enchantments of first piece to compare with others
        var firstGeneric = getEnchantLevel(Collections.singleton(armorPieces.get(0)), Enchantments.PROTECTION);
        var firstProjectile = getEnchantLevel(Collections.singleton(armorPieces.get(0)), Enchantments.PROJECTILE_PROTECTION);
        var firstExplosive = getEnchantLevel(Collections.singleton(armorPieces.get(0)), Enchantments.BLAST_PROTECTION);
        var firstFire = getEnchantLevel(Collections.singleton(armorPieces.get(0)), Enchantments.FIRE_PROTECTION);
        
        // Check if all pieces have the same enchantments
        for (int i = 1; i < armorPieces.size(); i++) {
            var nextGeneric = getEnchantLevel(Collections.singleton(armorPieces.get(i)), Enchantments.PROTECTION);
            var nextProjectile = getEnchantLevel(Collections.singleton(armorPieces.get(i)), Enchantments.PROJECTILE_PROTECTION);
            var nextExplosive = getEnchantLevel(Collections.singleton(armorPieces.get(i)), Enchantments.BLAST_PROTECTION);
            var nextFire = getEnchantLevel(Collections.singleton(armorPieces.get(i)), Enchantments.FIRE_PROTECTION);
            
            if (firstGeneric.level != nextGeneric.level || 
                firstProjectile.level != nextProjectile.level ||
                firstExplosive.level != nextExplosive.level ||
                firstFire.level != nextFire.level) {
                return false;
            }
        }
        
        return true;
    }

    public void render(DrawContext context, PlayerEntity player) {
        var generic = getEnchantLevel(getArmorItems(player), Enchantments.PROTECTION);
        var projectile = getEnchantLevel(getArmorItems(player), Enchantments.PROJECTILE_PROTECTION);
        var explosive = getEnchantLevel(getArmorItems(player), Enchantments.BLAST_PROTECTION);
        var fire = getEnchantLevel(getArmorItems(player), Enchantments.FIRE_PROTECTION);
        var protectArr = new int[] { generic.level + generic.count, projectile.level, explosive.level, fire.level, 0 };
        var armorPoints = getArmorPoints(player);
        var thorns = getEnchantLevel(getArmorItems(player), Enchantments.THORNS);

        var playerHealth = MathHelper.ceil(player.getHealth());
        var totalArmorPoint = armorPoints.size();
        var totalEnchants = Arrays.stream(protectArr).sum();
        var maxHealth = Math.max(player.getAttributeValue(EntityAttributes.MAX_HEALTH), playerHealth);
        var absorptionHealth = MathHelper.ceil(player.getAbsorptionAmount());
        var healthRow = getConfig().getOptions().toggleCompatibleHeartMod ? 1 : MathHelper.ceil((maxHealth + absorptionHealth) / 20.0f);
        var screenWidth = client.getWindow().getScaledWidth() / 2 - 91;
        var screenHeight = client.getWindow().getScaledHeight() - 39;
        var yPos = screenHeight - (healthRow - 1) * Math.max(10 - (healthRow - 2), 3) - 10;

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

            for (int count = 0; count < 10; count++) {
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
                    Pair<ItemStack, CustomArmorBar> am1 = armorPoints.get(count * 2 + stackRow);
                    Pair<ItemStack, CustomArmorBar> am2 = armorPoints.get(count * 2 + 1 + stackRow);
                    if (am1.getRight() == am2.getRight()) {
                        am1.getRight().draw(am1.getLeft(), context, xPos, yPos, false, false);
                    } else {
                        am2.getRight().draw(am2.getLeft(), context, xPos, yPos, true, true);
                        am1.getRight().draw(am1.getLeft(), context, xPos, yPos, true, false);
                    }
                }
                if (count * 2 + 1 + stackRow == totalArmorPoint) {
                    CustomArmorBar.EMPTY.draw(ItemStack.EMPTY, context, xPos, yPos, false, false);
                    Pair<ItemStack, CustomArmorBar> am = armorPoints.get(count * 2 + stackRow);
                    am.getRight().draw(am.getLeft(), context, xPos, yPos, true, false);
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

        //Durability Color
        if (getConfig().getOptions().toggleDurability) {
            List<Pair<EquipmentSlot, ItemStack>> equipment = new ArrayList<>();

            for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
                ItemStack itemStack = player.getEquippedStack(equipmentSlot);
                EquippableComponent equippableComponent = itemStack.get(DataComponentTypes.EQUIPPABLE);
                if (equippableComponent != null && equippableComponent.slot() == equipmentSlot) {
                    equipment.add(new Pair<>(equipmentSlot, itemStack));
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
                        
                        Pair<ItemStack, CustomArmorBar> am = armorPoints.get((halfArmors - count) * 2 + stackRow);
                        if (armorPreset == (halfArmors - count) * 2 + 1) {
                            if (count == 0) {
                                am.getRight().drawOutLine(am.getLeft(), context, xPos, yPos, true, false, lowDurColor);
                                lowDur--;
                            }
                        } else {
                            if (lowDur == 1) {
                                am.getRight().drawOutLine(am.getLeft(), context, xPos, yPos, true, true, lowDurColor);
                                lowDur = 0;
                            } else {
                                am.getRight().drawOutLine(am.getLeft(), context, xPos, yPos, false, false, lowDurColor);
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
                for (int count = 0; count < 10; count++) {
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
                            Pair<ItemStack, CustomArmorBar> am = armorPoints.get(count * 2 + stackRow);
                            am.getRight().drawOutLine(am.getLeft(), context, xPos, yPos, false, false, Color.WHITE);
                        }
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
                                ItemStack armorItem = armorPoints.get(armorIndex).getLeft();
                                if (!armorItem.isEmpty()) {
                                    var armorGeneric = getEnchantLevel(Collections.singleton(armorItem), Enchantments.PROTECTION);
                                    var armorProjectile = getEnchantLevel(Collections.singleton(armorItem), Enchantments.PROJECTILE_PROTECTION);
                                    var armorExplosive = getEnchantLevel(Collections.singleton(armorItem), Enchantments.BLAST_PROTECTION);
                                    var armorFire = getEnchantLevel(Collections.singleton(armorItem), Enchantments.FIRE_PROTECTION);
                                    var armorProtectArr = new int[] { armorGeneric.level, armorProjectile.level, armorExplosive.level, armorFire.level, 0 };
                                    if (Arrays.stream(armorProtectArr).sum() > 0) {
                                        if (hasSecondPoint) {
                                            ItemStack nextArmorItem = armorPoints.get(nextArmorIndex).getLeft();
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
                                ItemStack nextArmorItem = armorPoints.get(nextArmorIndex).getLeft();
                                if (!nextArmorItem.isEmpty()) {
                                    var nextGeneric = getEnchantLevel(Collections.singleton(nextArmorItem), Enchantments.PROTECTION);
                                    var nextProjectile = getEnchantLevel(Collections.singleton(nextArmorItem), Enchantments.PROJECTILE_PROTECTION);
                                    var nextExplosive = getEnchantLevel(Collections.singleton(nextArmorItem), Enchantments.BLAST_PROTECTION);
                                    var nextFire = getEnchantLevel(Collections.singleton(nextArmorItem), Enchantments.FIRE_PROTECTION);
                                    var nextProtectArr = new int[] { nextGeneric.level, nextProjectile.level, nextExplosive.level, nextFire.level, 0 };
                                    
                                    if (Arrays.stream(nextProtectArr).sum() > 0) {
                                        if (hasFirstPoint) {
                                            ItemStack armorItem = armorPoints.get(armorIndex).getLeft();
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
                for (int count = 0; count * 2 + 1 <= totalEnchants; count++) {
                    if (count > 9) break;

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
                            ItemStack armorItem = armorPoints.get(armorIndex).getLeft();
                            if (!armorItem.isEmpty()) {
                                var thornLevel = getEnchantLevel(Collections.singleton(armorItem), Enchantments.THORNS);
                                firstHasThorns = thornLevel.level > 0;
                            }
                        }
                        
                        // Check if second armor point has thorns
                        if (hasSecondPoint) {
                            ItemStack nextArmorItem = armorPoints.get(nextArmorIndex).getLeft();
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
                for (int count = 0; count < 10; count++) {
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

    private void drawEnchantTexture(DrawContext context, int x, int y, Color color, int half) {
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
        var t = (hud.getTicks() / tickDivisor) % 36;

        if (getConfig().getOptions().effectType == ProtectionEffect.AURA) {
            if (t < 12) {
                u = (t % 12) * 9;
                v = 27 + (half * 9);
            }
        } else if (getConfig().getOptions().effectType == ProtectionEffect.OUTLINE) {
            u = 9 + (half * 9);
        } else return;

        InGameDrawer.drawTexture(GUI_ARMOR_BAR, context, x, y, u, v, color, false);
    }
}