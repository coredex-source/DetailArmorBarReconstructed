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

import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;

import static com.redlimerl.detailab.DetailArmorBar.getConfig;


public class DurabilityNotificationHandler {
    public static final class VisualWarning {
        private final EquipmentSlot slot;
        private final DurabilityThreshold threshold;
        private final long sequence;

        private VisualWarning(EquipmentSlot slot, DurabilityThreshold threshold, long sequence) {
            this.slot = slot;
            this.threshold = threshold;
            this.sequence = sequence;
        }

        public EquipmentSlot getSlot() {
            return slot;
        }

        public DurabilityThreshold getThreshold() {
            return threshold;
        }

        public long getSequence() {
            return sequence;
        }
    }

    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET
    };
    private static final Map<EquipmentSlot, Map<DurabilityThreshold, Boolean>> triggeredThresholds = new EnumMap<>(EquipmentSlot.class);
    private static final Map<EquipmentSlot, Map<DurabilityThreshold, Long>> lastSlotWarningTimes = new EnumMap<>(EquipmentSlot.class);
    private static final Map<EquipmentSlot, ItemStack> lastKnownItems = new EnumMap<>(EquipmentSlot.class);
    private static final Map<DurabilityThreshold, Long> lastWarningTimes = new EnumMap<>(DurabilityThreshold.class);
    private static final Queue<VisualWarning> pendingVisualWarnings = new ArrayDeque<>();
    private static DurabilityThreshold currentWarningLevel = null;
    private static EquipmentSlot currentWarningSlot = null;
    private static long warningSequence = 0L;
    
    static {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            triggeredThresholds.put(slot, new EnumMap<>(DurabilityThreshold.class));
            lastSlotWarningTimes.put(slot, new EnumMap<>(DurabilityThreshold.class));
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

            DurabilityThreshold activeThreshold = getMostSevereEnabledThreshold(durabilityPercent);
            resetRecoveredThresholds(slot, durabilityPercent);
            if (activeThreshold != null) {
                checkThreshold(slot, currentItem, activeThreshold, durabilityPercent, client);
            }
        }
    }
    
    private static void checkThreshold(EquipmentSlot slot, ItemStack item, float durabilityPercent,
                                       DurabilityThreshold threshold, Minecraft client) {
        Map<DurabilityThreshold, Boolean> slotThresholds = triggeredThresholds.get(slot);
        boolean alreadyTriggered = slotThresholds.getOrDefault(threshold, false);
        
        if (!alreadyTriggered || getConfig().getOptions().toggleRepeatedDurabilityNotifications) {
            if (alreadyTriggered && getConfig().getOptions().toggleRepeatedDurabilityNotifications) {
                long lastNotification = getLastWarningTime(slot, threshold);
                if (DetailArmorBar.getTicks() - lastNotification < 100) {
                    return;
                }
            }

            triggerNotification(slot, item, threshold, durabilityPercent, client);
            slotThresholds.put(threshold, true);
        }
    }

    private static DurabilityThreshold getMostSevereEnabledThreshold(float durabilityPercent) {
        DurabilityThreshold result = null;
        for (DurabilityThreshold threshold : DurabilityThreshold.values()) {
            if (isThresholdEnabled(threshold) && durabilityPercent <= threshold.getPercentage()) {
                result = threshold;
            }
        }
        return result;
    }

    private static void resetRecoveredThresholds(EquipmentSlot slot, float durabilityPercent) {
        Map<DurabilityThreshold, Boolean> slotThresholds = triggeredThresholds.get(slot);
        for (DurabilityThreshold threshold : DurabilityThreshold.values()) {
            if (durabilityPercent > threshold.getPercentage() && slotThresholds.getOrDefault(threshold, false)) {
                slotThresholds.put(threshold, false);
            }
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

    public static long getLastWarningTime(EquipmentSlot slot, DurabilityThreshold threshold) {
        Map<DurabilityThreshold, Long> slotWarningTimes = lastSlotWarningTimes.get(slot);
        if (slotWarningTimes == null) {
            return 0L;
        }
        return slotWarningTimes.getOrDefault(threshold, 0L);
    }

    public static VisualWarning pollVisualWarning() {
        return pendingVisualWarnings.poll();
    }

    public static DurabilityThreshold getCurrentWarningLevel() {
        return currentWarningLevel;
    }

    public static EquipmentSlot getCurrentWarningSlot() {
        return currentWarningSlot;
    }

    public static void clearCurrentWarningLevel() {
        currentWarningLevel = null;
        currentWarningSlot = null;
    }
    
    private static void triggerNotification(EquipmentSlot slot, ItemStack item, DurabilityThreshold threshold,
                                            float durabilityPercent, Minecraft client) {
        long currentTick = DetailArmorBar.getTicks();
        lastWarningTimes.put(threshold, currentTick);
        lastSlotWarningTimes.get(slot).put(threshold, currentTick);
        currentWarningLevel = threshold;
        currentWarningSlot = slot;
        pendingVisualWarnings.add(new VisualWarning(slot, threshold, ++warningSequence));
        
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
                getToastManager(client),
                SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                title,
                description
            );
        }
    }

    private static net.minecraft.client.gui.components.toasts.ToastManager getToastManager(Minecraft client) {
        //? if minecraft_26_2 {
        return client.gui.toastManager();
        //?} else {
        /*return client.getToastManager();
        *///?}
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
            lastSlotWarningTimes.get(slot).clear();
            lastKnownItems.put(slot, ItemStack.EMPTY);
        }
        for (DurabilityThreshold threshold : DurabilityThreshold.values()) {
            lastWarningTimes.put(threshold, 0L);
        }
        currentWarningLevel = null;
        currentWarningSlot = null;
        pendingVisualWarnings.clear();
        warningSequence = 0L;
    }
}
