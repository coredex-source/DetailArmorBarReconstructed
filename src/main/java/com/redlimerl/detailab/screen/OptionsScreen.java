package com.redlimerl.detailab.screen;

import com.redlimerl.detailab.config.ConfigEnumType;
import com.redlimerl.detailab.config.DetailArmorBarConfig;
import dev.eclipseui.EclipseUI;
import dev.eclipseui.api.Theme;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

import static com.redlimerl.detailab.DetailArmorBar.getConfig;

public final class OptionsScreen {

    private OptionsScreen() {
    }

    public static Screen create(Screen parent) {
        var config = getConfig();
        var options = config.getOptions();
        var defaults = DetailArmorBarConfig.Options.DEFAULT;

        return EclipseUI.configScreen()
                .title(Component.translatable("options.title"))
                .parent(parent)
                .theme(Theme.MODERN)
                .onSave(config::save)
                .category(cat -> cat
                        .name(option("title.features"))
                        .toggle(t -> t.name(option("toggle.enchantments")).description(context("toggle.enchantments")).binding(() -> options.toggleEnchants, v -> options.toggleEnchants = v).defaultValue(defaults.toggleEnchants))
                        .toggle(t -> t.name(option("toggle.thorns")).description(context("toggle.thorns")).binding(() -> options.toggleThorns, v -> options.toggleThorns = v).defaultValue(defaults.toggleThorns))
                        .toggle(t -> t.name(option("toggle.durability")).description(context("toggle.durability")).binding(() -> options.toggleDurability, v -> options.toggleDurability = v).defaultValue(defaults.toggleDurability))
                        .toggle(t -> t.name(option("toggle.mending")).description(context("toggle.mending")).binding(() -> options.toggleMending, v -> options.toggleMending = v).defaultValue(defaults.toggleMending))
                        .toggle(t -> t.name(option("toggle.armor_types")).description(context("toggle.armor_types")).binding(() -> options.toggleArmorTypes, v -> options.toggleArmorTypes = v).defaultValue(defaults.toggleArmorTypes))
                        .toggle(t -> t.name(option("toggle.item_types")).description(context("toggle.item_types")).binding(() -> options.toggleItemBar, v -> options.toggleItemBar = v).defaultValue(defaults.toggleItemBar))
                        .toggle(t -> t.name(option("toggle.empty_bar")).description(context("toggle.empty_bar")).binding(() -> options.toggleEmptyBar, v -> options.toggleEmptyBar = v).defaultValue(defaults.toggleEmptyBar))
                        .toggle(t -> t.name(option("toggle.stack_armor_bars")).description(context("toggle.stack_armor_bars")).binding(() -> options.toggleStackArmorBars, v -> options.toggleStackArmorBars = v).defaultValue(defaults.toggleStackArmorBars))
                        .toggle(t -> t.name(option("toggle.align_enchantments")).description(context("toggle.align_enchantments")).binding(() -> options.toggleAlignEnchantments, v -> options.toggleAlignEnchantments = v).defaultValue(defaults.toggleAlignEnchantments))
                        .toggle(t -> t.name(option("toggle.uniform_color")).description(context("toggle.uniform_color")).binding(() -> options.toggleUniformColor, v -> options.toggleUniformColor = v).defaultValue(defaults.toggleUniformColor))
                        .toggle(t -> t.name(option("toggle.armor_trims")).description(context("toggle.armor_trims")).binding(() -> options.toggleArmorTrims, v -> options.toggleArmorTrims = v).defaultValue(defaults.toggleArmorTrims))
                        .toggle(t -> t.name(option("toggle.durability_overlay")).description(context("toggle.durability_overlay")).binding(() -> options.toggleDurabilityOverlay, v -> options.toggleDurabilityOverlay = v).defaultValue(defaults.toggleDurabilityOverlay))
                        .toggle(t -> t.name(option("toggle.inventory_overlay")).description(context("toggle.inventory_overlay")).binding(() -> options.toggleInventoryOverlay, v -> options.toggleInventoryOverlay = v).defaultValue(defaults.toggleInventoryOverlay))
                )
                .category(cat -> cat
                        .name(option("title.animation"))
                        .<ConfigEnumType.ProtectionEffect>dropdown(d -> d
                                .name(option("effects.effect_type"))
                                .description(context("effects.effect_type"))
                                .enumClass(ConfigEnumType.ProtectionEffect.class)
                                .binding(() -> options.effectType, v -> options.effectType = v)
                                .defaultValue(defaults.effectType)
                                .formatter(value -> enumOption("effects.effect_type", value))
                        )
                        .<ConfigEnumType.EffectSpeed>dropdown(d -> d
                                .name(option("effects.effect_speed"))
                                .description(context("effects.effect_speed"))
                                .enumClass(ConfigEnumType.EffectSpeed.class)
                                .binding(() -> options.effectSpeed, v -> options.effectSpeed = v)
                                .defaultValue(defaults.effectSpeed)
                                .formatter(value -> enumOption("effects.effect_speed", value))
                        )
                        .<ConfigEnumType.Animation>dropdown(d -> d
                                .name(option("effects.thorn"))
                                .description(context("effects.thorn"))
                                .enumClass(ConfigEnumType.Animation.class)
                                .binding(() -> options.effectThorn, v -> options.effectThorn = v)
                                .defaultValue(defaults.effectThorn)
                                .formatter(value -> enumOption("effects.thorn", value))
                        )
                        .colorPicker(c -> c
                                .name(option("effects.uniform_color"))
                                .description(context("effects.uniform_color"))
                                .binding(options::getUniformColorArgb, options::setUniformColorArgb)
                                .defaultValue(defaults.getUniformColorArgb())
                                .allowAlpha(true)
                                .presets(
                                        0x5099FFFF,
                                        0x507033AD,
                                        0x50FFFF00,
                                        0x50D23800,
                                        0x50FFFFFF,
                                        0x5000FF00,
                                        0x500000FF,
                                        0x50FF0000
                                )
                        )
                        .colorPicker(c -> c
                                .name(option("effects.protection_generic_color"))
                                .description(context("effects.protection_generic_color"))
                                .binding(options::getProtectionColorGenericArgb, options::setProtectionColorGenericArgb)
                                .defaultValue(defaults.getProtectionColorGenericArgb())
                                .allowAlpha(true)
                                .presets(
                                        0x5099FFFF,
                                        0x507033AD,
                                        0x50FFFF00,
                                        0x50D23800,
                                        0x50FFFFFF,
                                        0x5000FF00,
                                        0x500000FF,
                                        0x50FF0000
                                )
                        )
                        .colorPicker(c -> c
                                .name(option("effects.protection_projectile_color"))
                                .description(context("effects.protection_projectile_color"))
                                .binding(options::getProtectionColorProjectileArgb, options::setProtectionColorProjectileArgb)
                                .defaultValue(defaults.getProtectionColorProjectileArgb())
                                .allowAlpha(true)
                                .presets(
                                        0x507033AD,
                                        0x5099FFFF,
                                        0x50FFFF00,
                                        0x50D23800,
                                        0x50FFFFFF,
                                        0x5000FF00,
                                        0x500000FF,
                                        0x50FF0000
                                )
                        )
                        .colorPicker(c -> c
                                .name(option("effects.protection_blast_color"))
                                .description(context("effects.protection_blast_color"))
                                .binding(options::getProtectionColorBlastArgb, options::setProtectionColorBlastArgb)
                                .defaultValue(defaults.getProtectionColorBlastArgb())
                                .allowAlpha(true)
                                .presets(
                                        0x50FFFF00,
                                        0x5099FFFF,
                                        0x507033AD,
                                        0x50D23800,
                                        0x50FFFFFF,
                                        0x5000FF00,
                                        0x500000FF,
                                        0x50FF0000
                                )
                        )
                        .colorPicker(c -> c
                                .name(option("effects.protection_fire_color"))
                                .description(context("effects.protection_fire_color"))
                                .binding(options::getProtectionColorFireArgb, options::setProtectionColorFireArgb)
                                .defaultValue(defaults.getProtectionColorFireArgb())
                                .allowAlpha(true)
                                .presets(
                                        0x50D23800,
                                        0x5099FFFF,
                                        0x507033AD,
                                        0x50FFFF00,
                                        0x50FFFFFF,
                                        0x5000FF00,
                                        0x500000FF,
                                        0x50FF0000
                                )
                        )
                )
                .category(cat -> cat
                        .name(option("title.positioning"))
                        .slider(s -> s
                                .name(Component.literal("X Offset"))
                                .description(context("positioning.offset_x"))
                                .range(-200, 200, 1)
                                .bindingInt(() -> options.armorBarOffsetX, v -> options.armorBarOffsetX = v)
                                .defaultValue(defaults.armorBarOffsetX)
                                .suffix(" px")
                        )
                        .slider(s -> s
                                .name(Component.literal("Y Offset"))
                                .description(context("positioning.offset_y"))
                                .range(-200, 200, 1)
                                .bindingInt(() -> options.armorBarOffsetY, v -> options.armorBarOffsetY = v)
                                .defaultValue(defaults.armorBarOffsetY)
                                .suffix(" px")
                        )
                )
                .category(cat -> cat
                        .name(option("title.durability"))
                        .toggle(t -> t.name(option("toggle.durability_notifications")).description(context("toggle.durability_notifications")).binding(() -> options.toggleDurabilityNotifications, v -> options.toggleDurabilityNotifications = v).defaultValue(defaults.toggleDurabilityNotifications))
                        .toggle(t -> t.name(option("toggle.repeated_notifications")).description(context("toggle.repeated_notifications")).binding(() -> options.toggleRepeatedDurabilityNotifications, v -> options.toggleRepeatedDurabilityNotifications = v).defaultValue(defaults.toggleRepeatedDurabilityNotifications))
                        .toggle(t -> t.name(option("toggle.sound_notification")).description(context("toggle.sound_notification")).binding(() -> options.toggleDurabilitySoundNotification, v -> options.toggleDurabilitySoundNotification = v).defaultValue(defaults.toggleDurabilitySoundNotification))
                        .toggle(t -> t.name(option("toggle.toast_notification")).description(context("toggle.toast_notification")).binding(() -> options.toggleDurabilityToastNotification, v -> options.toggleDurabilityToastNotification = v).defaultValue(defaults.toggleDurabilityToastNotification))
                        .toggle(t -> t.name(option("toggle.visual_effect")).description(context("toggle.visual_effect")).binding(() -> options.toggleDurabilityVisualEffect, v -> options.toggleDurabilityVisualEffect = v).defaultValue(defaults.toggleDurabilityVisualEffect))
                        .toggle(t -> t.name(option("toggle.threshold_50")).description(context("toggle.threshold_50")).binding(() -> options.toggleThreshold50, v -> options.toggleThreshold50 = v).defaultValue(defaults.toggleThreshold50))
                        .toggle(t -> t.name(option("toggle.threshold_25")).description(context("toggle.threshold_25")).binding(() -> options.toggleThreshold25, v -> options.toggleThreshold25 = v).defaultValue(defaults.toggleThreshold25))
                        .toggle(t -> t.name(option("toggle.threshold_10")).description(context("toggle.threshold_10")).binding(() -> options.toggleThreshold10, v -> options.toggleThreshold10 = v).defaultValue(defaults.toggleThreshold10))
                        .toggle(t -> t.name(option("toggle.threshold_5")).description(context("toggle.threshold_5")).binding(() -> options.toggleThreshold5, v -> options.toggleThreshold5 = v).defaultValue(defaults.toggleThreshold5))
                        .<ConfigEnumType.HudPosition>dropdown(d -> d
                                .name(option("durability_hud.position"))
                                .description(context("durability_hud.position"))
                                .enumClass(ConfigEnumType.HudPosition.class)
                                .binding(() -> options.durabilityHudPosition, v -> options.durabilityHudPosition = v)
                                .defaultValue(defaults.durabilityHudPosition)
                                .formatter(value -> enumOption("durability_hud.position", value))
                        )
                        .slider(s -> s
                                .name(Component.literal("HUD X Offset"))
                                .description(context("durability_hud.offset_x"))
                                .range(-200, 200, 1)
                                .bindingInt(() -> options.durabilityHudOffsetX, v -> options.durabilityHudOffsetX = v)
                                .defaultValue(defaults.durabilityHudOffsetX)
                                .suffix(" px")
                        )
                        .slider(s -> s
                                .name(Component.literal("HUD Y Offset"))
                                .description(context("durability_hud.offset_y"))
                                .range(-200, 200, 1)
                                .bindingInt(() -> options.durabilityHudOffsetY, v -> options.durabilityHudOffsetY = v)
                                .defaultValue(defaults.durabilityHudOffsetY)
                                .suffix(" px")
                        )
                        .slider(s -> s
                                .name(Component.literal("HUD Scale"))
                                .description(context("durability_hud.scale"))
                                .range(0.5, 2.0, 0.25)
                                .bindingDouble(() -> (double) options.durabilityHudScale, v -> options.durabilityHudScale = v.floatValue())
                                .defaultValue((double) defaults.durabilityHudScale)
                                .suffix("x")
                        )
                )
                .category(cat -> cat
                        .name(option("title.etc"))
                        .toggle(t -> t.name(option("toggle.vanilla_texture")).description(context("toggle.vanilla_texture")).binding(() -> options.toggleVanillaTexture, v -> options.toggleVanillaTexture = v).defaultValue(defaults.toggleVanillaTexture))
                        .toggle(t -> t.name(option("toggle.compatible_heart_mod")).description(context("toggle.compatible_heart_mod")).binding(() -> options.toggleCompatibleHeartMod, v -> options.toggleCompatibleHeartMod = v).defaultValue(defaults.toggleCompatibleHeartMod))
                        .toggle(t -> t.name(option("toggle.inverse_slot")).description(context("toggle.inverse_slot")).binding(() -> options.toggleInverseSlot, v -> options.toggleInverseSlot = v).defaultValue(defaults.toggleInverseSlot))
                        .toggle(t -> t.name(option("toggle.inverse_overflow_icon")).description(context("toggle.inverse_overflow_icon")).binding(() -> options.toggleInverseOverflowIcon, v -> options.toggleInverseOverflowIcon = v).defaultValue(defaults.toggleInverseOverflowIcon))
                        .toggle(t -> t.name(option("toggle.hide_bar_without_armor")).description(context("toggle.hide_bar_without_armor")).binding(() -> options.toggleHideBarWithoutArmor, v -> options.toggleHideBarWithoutArmor = v).defaultValue(defaults.toggleHideBarWithoutArmor))
                )
                .build();
    }

    private static Component option(String key) {
        return Component.translatable("option.detailarmorbar." + key);
    }

    private static Component context(String key) {
        return Component.translatable("context.detailarmorbar." + key);
    }

    private static <T extends Enum<T>> Component enumOption(String prefix, T value) {
        return Component.translatable("option.detailarmorbar." + prefix + "." + value.name().toLowerCase(Locale.ROOT));
    }
}
