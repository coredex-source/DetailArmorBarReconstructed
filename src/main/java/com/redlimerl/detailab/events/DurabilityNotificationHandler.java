package com.redlimerl.detailab.events;

import com.redlimerl.detailab.DetailArmorBar;
import com.redlimerl.detailab.config.ConfigEnumType.DurabilityThreshold;
import com.redlimerl.detailab.loaders.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;

import java.util.EnumMap;
import java.util.Map;

import static com.redlimerl.detailab.DetailArmorBar.getConfig;


public class DurabilityNotificationHandler {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET
    };
    private static final Map<EquipmentSlot, Map<DurabilityThreshold, Boolean>> triggeredThresholds = new EnumMap<>(EquipmentSlot.class);
    private static final Map<EquipmentSlot, ItemStack> lastKnownItems = new EnumMap<>(EquipmentSlot.class);
    private static final Map<DurabilityThreshold, Long> lastWarningTimes = new EnumMap<>(DurabilityThreshold.class);
    private static DurabilityThreshold currentWarningLevel = null;
    
    static {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            triggeredThresholds.put(slot, new EnumMap<>(DurabilityThreshold.class));
            lastKnownItems.put(slot, ItemStack.EMPTY);
        }
        for (DurabilityThreshold threshold : DurabilityThreshold.values()) {
            lastWarningTimes.put(threshold, 0L);
        }
    }

    public static void register() {
        Platform.registerClientTick(client -> {
            if (!getConfig().getOptions().toggleDurabilityNotifications) {
                return;
            }
            
            Player player = client.player;
            if (player == null) {
                return;
            }
            
            checkArmorDurability(player, client);
        });
    }

    private static void checkArmorDurability(Player player, Minecraft client) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack currentItem = player.getItemBySlot(slot);
            ItemStack lastItem = lastKnownItems.get(slot);

            if (!ItemStack.isSameItem(currentItem, lastItem) ||
                (currentItem.isEmpty() != lastItem.isEmpty())) {
                resetThresholdsForSlot(slot);
                lastKnownItems.put(slot, currentItem.copy());
            }

            if (currentItem.isEmpty() || currentItem.getMaxDamage() == 0) {
                continue;
            }
            
            float durabilityPercent = 1.0f - ((float) currentItem.getDamageValue() / (float) currentItem.getMaxDamage());
            durabilityPercent *= 100;
            for (DurabilityThreshold threshold : DurabilityThreshold.values()) {
                checkThreshold(slot, currentItem, durabilityPercent, threshold, client);
            }
        }
    }
    
    private static void checkThreshold(EquipmentSlot slot, ItemStack item, float durabilityPercent,
                                       DurabilityThreshold threshold, Minecraft client) {
        if (!isThresholdEnabled(threshold)) {
            return;
        }
        
        Map<DurabilityThreshold, Boolean> slotThresholds = triggeredThresholds.get(slot);
        boolean alreadyTriggered = slotThresholds.getOrDefault(threshold, false);
        
        if (durabilityPercent <= threshold.getPercentage()) {
            if (!alreadyTriggered || getConfig().getOptions().toggleRepeatedDurabilityNotifications) {
                if (alreadyTriggered && getConfig().getOptions().toggleRepeatedDurabilityNotifications) {
                    long lastNotification = getLastWarningTime(threshold);
                    if (DetailArmorBar.getTicks() - lastNotification < 100) {
                        return;
                    }
                }
                
                triggerNotification(slot, item, threshold, durabilityPercent, client);
                slotThresholds.put(threshold, true);
            }
        } else if (alreadyTriggered) {
            slotThresholds.put(threshold, false);
        }
    }
    
    private static boolean isThresholdEnabled(DurabilityThreshold threshold) {
        return switch (threshold) {
            case HALF -> getConfig().getOptions().toggleThreshold50;
            case QUARTER -> getConfig().getOptions().toggleThreshold25;
            case LOW -> getConfig().getOptions().toggleThreshold10;
            case CRITICAL -> getConfig().getOptions().toggleThreshold5;
        };
    }

    public static long getLastWarningTime(DurabilityThreshold threshold) {
        return lastWarningTimes.getOrDefault(threshold, 0L);
    }

    public static DurabilityThreshold getCurrentWarningLevel() {
        return currentWarningLevel;
    }

    public static void clearCurrentWarningLevel() {
        currentWarningLevel = null;
    }
    
    private static void triggerNotification(EquipmentSlot slot, ItemStack item, DurabilityThreshold threshold,
                                            float durabilityPercent, Minecraft client) {
        long currentTick = DetailArmorBar.getTicks();
        lastWarningTimes.put(threshold, currentTick);
        currentWarningLevel = threshold;
        
        if (getConfig().getOptions().toggleDurabilitySoundNotification && client.player != null) {
            var sound = switch (threshold) {
                case CRITICAL -> SoundEvents.ANVIL_LAND;
                case LOW -> SoundEvents.NOTE_BLOCK_BASS.value();
                case QUARTER -> SoundEvents.NOTE_BLOCK_PLING.value();
                case HALF -> SoundEvents.NOTE_BLOCK_HAT.value();
            };
            
            float volume = switch (threshold) {
                case CRITICAL -> 0.5f;
                case LOW -> 0.4f;
                case QUARTER -> 0.3f;
                case HALF -> 0.2f;
            };
            
            if (client.level != null) {
                client.level.playSound(
                    client.player,
                    client.player.blockPosition(),
                    sound,
                    SoundSource.PLAYERS,
                    volume,
                    1.0f
                );
            }
        }
        
        if (getConfig().getOptions().toggleDurabilityToastNotification) {
            String itemName = item.getHoverName().getString();
            int durabilityRemaining = (int) durabilityPercent;
            
            Component title = switch (threshold) {
                case CRITICAL -> Component.translatable("notification.detailarmorbar.durability.critical.title");
                case LOW -> Component.translatable("notification.detailarmorbar.durability.low.title");
                case QUARTER -> Component.translatable("notification.detailarmorbar.durability.quarter.title");
                case HALF -> Component.translatable("notification.detailarmorbar.durability.half.title");
            };
            
            Component description = Component.translatable("notification.detailarmorbar.durability.description", itemName, durabilityRemaining);
            
            SystemToast.addOrUpdate(
                client.gui.toastManager(),
                SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                title,
                description
            );
        }
    }
    
    private static void resetThresholdsForSlot(EquipmentSlot slot) {
        Map<DurabilityThreshold, Boolean> slotThresholds = triggeredThresholds.get(slot);
        if (slotThresholds != null) {
            slotThresholds.clear();
        }
    }
    
    public static void resetAll() {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            resetThresholdsForSlot(slot);
            lastKnownItems.put(slot, ItemStack.EMPTY);
        }
        for (DurabilityThreshold threshold : DurabilityThreshold.values()) {
            lastWarningTimes.put(threshold, 0L);
        }
        currentWarningLevel = null;
    }
}
