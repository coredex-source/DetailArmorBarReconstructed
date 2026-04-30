package com.redlimerl.detailab.screen;

import com.redlimerl.detailab.config.ConfigEnumType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import static com.redlimerl.detailab.DetailArmorBar.getConfig;

public class OptionsScreen extends Screen {
    private final Screen parent;
    private final OptionType optionType;

    public enum OptionType {
        FEATURES, ANIMATION, POSITIONING, DURABILITY, ETC
    }

    public OptionsScreen(Screen screen) {
        this(screen, OptionType.FEATURES);
    }

    public OptionsScreen(Screen parent, OptionType optionType) {
        super(Component.translatable("options.title"));
        this.parent = parent;
        this.optionType = optionType;
    }

    private int buttonCount = 0;

    @Override
    protected void init() {
        super.init();

        if (optionType == OptionType.FEATURES) {
            addRenderableWidget(
                    Button
                        .builder(getToggleName("enchantments", getConfig().getOptions().toggleEnchants), (button) -> {
                            getConfig().getOptions().toggleEnchants = !getConfig().getOptions().toggleEnchants; getConfig().save();
                            button.setMessage(getToggleName("enchantments", getConfig().getOptions().toggleEnchants));
                        })
                        .bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                        .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.enchantments")))
                        .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getToggleName("thorns", getConfig().getOptions().toggleThorns), (button) -> {
                        getConfig().getOptions().toggleThorns = !getConfig().getOptions().toggleThorns; getConfig().save();
                        button.setMessage(getToggleName("thorns", getConfig().getOptions().toggleThorns));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.thorns")))
                    .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getToggleName("durability", getConfig().getOptions().toggleDurability), (button) -> {
                        getConfig().getOptions().toggleDurability = !getConfig().getOptions().toggleDurability; getConfig().save();
                        button.setMessage(getToggleName("durability", getConfig().getOptions().toggleDurability));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.durability")))
                            .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getToggleName("mending", getConfig().getOptions().toggleMending), (button) -> {
                        getConfig().getOptions().toggleMending = !getConfig().getOptions().toggleMending; getConfig().save();
                        button.setMessage(getToggleName("mending", getConfig().getOptions().toggleMending));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.mending")))
                    .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getToggleName("armor_types", getConfig().getOptions().toggleArmorTypes), (button) -> {
                        getConfig().getOptions().toggleArmorTypes = !getConfig().getOptions().toggleArmorTypes; getConfig().save();
                        button.setMessage(getToggleName("armor_types", getConfig().getOptions().toggleArmorTypes));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.armor_types")))
                    .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getToggleName("item_types", getConfig().getOptions().toggleItemBar), (button) -> {
                        getConfig().getOptions().toggleItemBar = !getConfig().getOptions().toggleItemBar; getConfig().save();
                        button.setMessage(getToggleName("item_types", getConfig().getOptions().toggleItemBar));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.item_types")))
                    .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getToggleName("empty_bar", getConfig().getOptions().toggleEmptyBar), (button) -> {
                        getConfig().getOptions().toggleEmptyBar = !getConfig().getOptions().toggleEmptyBar; getConfig().save();
                        button.setMessage(getToggleName("empty_bar", getConfig().getOptions().toggleEmptyBar));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.empty_bar")))
                    .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getToggleName("align_enchantments", getConfig().getOptions().toggleAlignEnchantments), (button) -> {
                        getConfig().getOptions().toggleAlignEnchantments = !getConfig().getOptions().toggleAlignEnchantments; getConfig().save();
                        button.setMessage(getToggleName("align_enchantments", getConfig().getOptions().toggleAlignEnchantments));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.align_enchantments")))
                    .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getToggleName("uniform_color", getConfig().getOptions().toggleUniformColor), (button) -> {
                        getConfig().getOptions().toggleUniformColor = !getConfig().getOptions().toggleUniformColor; getConfig().save();
                        button.setMessage(getToggleName("uniform_color", getConfig().getOptions().toggleUniformColor));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.uniform_color")))
                    .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getToggleName("armor_trims", getConfig().getOptions().toggleArmorTrims), (button) -> {
                        getConfig().getOptions().toggleArmorTrims = !getConfig().getOptions().toggleArmorTrims; getConfig().save();
                        button.setMessage(getToggleName("armor_trims", getConfig().getOptions().toggleArmorTrims));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.armor_trims")))
                    .build()
            );
            buttonCount++;
        }

        if (optionType == OptionType.ANIMATION) {
            addRenderableWidget(Button.builder(getEnumName("effect_type", getConfig().getOptions().effectType), (button) -> {
                        getConfig().getOptions().effectType = getEnumNext(getConfig().getOptions().effectType);
                        getConfig().save();
                        button.setMessage(getEnumName("effect_type", getConfig().getOptions().effectType));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.literal(getEnumDescription("effect_type", getConfig().getOptions().effectType))))
                    .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getEnumName("effect_speed", getConfig().getOptions().effectSpeed), (button) -> {
                        getConfig().getOptions().effectSpeed = getEnumNext(getConfig().getOptions().effectSpeed);
                        getConfig().save();
                        button.setMessage(getEnumName("effect_speed", getConfig().getOptions().effectSpeed));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.literal(getEnumDescription("effect_speed", getConfig().getOptions().effectSpeed))))
                    .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getEnumName("thorn", getConfig().getOptions().effectThorn), (button) -> {
                        getConfig().getOptions().effectThorn = getEnumNext(getConfig().getOptions().effectThorn);
                        getConfig().save();
                        button.setMessage(getEnumName("thorn", getConfig().getOptions().effectThorn));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.literal(getEnumDescription("thorn", getConfig().getOptions().effectThorn))))
                            .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getUniformColorName("uniform_color", getConfig().getOptions().uniformColorType), (button) -> {
                    getConfig().getOptions().uniformColorType = getUniformColorNext(getConfig().getOptions().uniformColorType);
                    getConfig().save();
                    button.setMessage(getUniformColorName("uniform_color", getConfig().getOptions().uniformColorType));
                }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                        .tooltip(Tooltip.create(Component.literal(getUniformColorDescription("uniform_color", getConfig().getOptions().uniformColorType))))
                .build()
            );
            buttonCount++;
        }

        if (optionType == OptionType.POSITIONING) {
            // X Offset button - opens text input dialog
            addRenderableWidget(Button.builder(Component.literal("X Offset: " + getConfig().getOptions().armorBarOffsetX), (button) -> {
                if (minecraft != null) {
                    minecraft.setScreen(new TextInputScreen(
                        this,
                        Component.literal("X Offset"),
                        String.valueOf(getConfig().getOptions().armorBarOffsetX),
                        (value) -> {
                            getConfig().getOptions().armorBarOffsetX = value;
                            getConfig().save();
                        }
                    ));
                }
            }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                    .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.positioning.offset_x")))
                    .build()
            );
            buttonCount++;

            // Y Offset button - opens text input dialog
            addRenderableWidget(Button.builder(Component.literal("Y Offset: " + getConfig().getOptions().armorBarOffsetY), (button) -> {
                if (minecraft != null) {
                    minecraft.setScreen(new TextInputScreen(
                        this,
                        Component.literal("Y Offset"),
                        String.valueOf(getConfig().getOptions().armorBarOffsetY),
                        (value) -> {
                            getConfig().getOptions().armorBarOffsetY = value;
                            getConfig().save();
                        }
                    ));
                }
            }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                    .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.positioning.offset_y")))
                    .build()
            );
            buttonCount++;

            // Reset button
            addRenderableWidget(Button.builder(Component.translatable("option.detailarmorbar.positioning.reset"), (button) -> {
                getConfig().getOptions().armorBarOffsetX = 0;
                getConfig().getOptions().armorBarOffsetY = 0;
                getConfig().save();
                // Force re-initialization to update button labels
                if (minecraft != null) {
                    minecraft.setScreen(new OptionsScreen(parent, OptionType.POSITIONING));
                }
            }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                    .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.positioning.reset")))
                    .build()
            );
            buttonCount++;
        }

        if (optionType == OptionType.DURABILITY) {
            addRenderableWidget(Button.builder(getToggleName("durability_notifications", getConfig().getOptions().toggleDurabilityNotifications), (button) -> {
                        getConfig().getOptions().toggleDurabilityNotifications = !getConfig().getOptions().toggleDurabilityNotifications; getConfig().save();
                        button.setMessage(getToggleName("durability_notifications", getConfig().getOptions().toggleDurabilityNotifications));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.durability_notifications")))
                    .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getToggleName("repeated_notifications", getConfig().getOptions().toggleRepeatedDurabilityNotifications), (button) -> {
                        getConfig().getOptions().toggleRepeatedDurabilityNotifications = !getConfig().getOptions().toggleRepeatedDurabilityNotifications; getConfig().save();
                        button.setMessage(getToggleName("repeated_notifications", getConfig().getOptions().toggleRepeatedDurabilityNotifications));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.repeated_notifications")))
                    .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getToggleName("sound_notification", getConfig().getOptions().toggleDurabilitySoundNotification), (button) -> {
                        getConfig().getOptions().toggleDurabilitySoundNotification = !getConfig().getOptions().toggleDurabilitySoundNotification; getConfig().save();
                        button.setMessage(getToggleName("sound_notification", getConfig().getOptions().toggleDurabilitySoundNotification));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.sound_notification")))
                    .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getToggleName("toast_notification", getConfig().getOptions().toggleDurabilityToastNotification), (button) -> {
                        getConfig().getOptions().toggleDurabilityToastNotification = !getConfig().getOptions().toggleDurabilityToastNotification; getConfig().save();
                        button.setMessage(getToggleName("toast_notification", getConfig().getOptions().toggleDurabilityToastNotification));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.toast_notification")))
                    .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getToggleName("visual_effect", getConfig().getOptions().toggleDurabilityVisualEffect), (button) -> {
                        getConfig().getOptions().toggleDurabilityVisualEffect = !getConfig().getOptions().toggleDurabilityVisualEffect; getConfig().save();
                        button.setMessage(getToggleName("visual_effect", getConfig().getOptions().toggleDurabilityVisualEffect));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.visual_effect")))
                    .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getToggleName("threshold_50", getConfig().getOptions().toggleThreshold50), (button) -> {
                        getConfig().getOptions().toggleThreshold50 = !getConfig().getOptions().toggleThreshold50; getConfig().save();
                        button.setMessage(getToggleName("threshold_50", getConfig().getOptions().toggleThreshold50));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.threshold_50")))
                    .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getToggleName("threshold_25", getConfig().getOptions().toggleThreshold25), (button) -> {
                        getConfig().getOptions().toggleThreshold25 = !getConfig().getOptions().toggleThreshold25; getConfig().save();
                        button.setMessage(getToggleName("threshold_25", getConfig().getOptions().toggleThreshold25));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.threshold_25")))
                    .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getToggleName("threshold_10", getConfig().getOptions().toggleThreshold10), (button) -> {
                        getConfig().getOptions().toggleThreshold10 = !getConfig().getOptions().toggleThreshold10; getConfig().save();
                        button.setMessage(getToggleName("threshold_10", getConfig().getOptions().toggleThreshold10));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.threshold_10")))
                    .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getToggleName("threshold_5", getConfig().getOptions().toggleThreshold5), (button) -> {
                        getConfig().getOptions().toggleThreshold5 = !getConfig().getOptions().toggleThreshold5; getConfig().save();
                        button.setMessage(getToggleName("threshold_5", getConfig().getOptions().toggleThreshold5));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.threshold_5")))
                    .build()
            );
            buttonCount++;
        }

        if (optionType == OptionType.ETC) {
            addRenderableWidget(Button.builder(getToggleName("vanilla_texture", getConfig().getOptions().toggleVanillaTexture), (button) -> {
                        getConfig().getOptions().toggleVanillaTexture = !getConfig().getOptions().toggleVanillaTexture; getConfig().save();
                        button.setMessage(getToggleName("vanilla_texture", getConfig().getOptions().toggleVanillaTexture));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.vanilla_texture")))
                    .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getToggleName("compatible_heart_mod", getConfig().getOptions().toggleCompatibleHeartMod), (button) -> {
                        getConfig().getOptions().toggleCompatibleHeartMod = !getConfig().getOptions().toggleCompatibleHeartMod; getConfig().save();
                        button.setMessage(getToggleName("compatible_heart_mod", getConfig().getOptions().toggleCompatibleHeartMod));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.compatible_heart_mod")))
                            .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getToggleName("inverse_slot", getConfig().getOptions().toggleInverseSlot), (button) -> {
                        getConfig().getOptions().toggleInverseSlot = !getConfig().getOptions().toggleInverseSlot; getConfig().save();
                        button.setMessage(getToggleName("inverse_slot", getConfig().getOptions().toggleInverseSlot));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.inverse_slot")))
                            .build()
            );
            buttonCount++;

            addRenderableWidget(Button.builder(getToggleName("inverse_overflow_icon", getConfig().getOptions().toggleInverseOverflowIcon), (button) -> {
                        getConfig().getOptions().toggleInverseOverflowIcon = !getConfig().getOptions().toggleInverseOverflowIcon; getConfig().save();
                        button.setMessage(getToggleName("inverse_overflow_icon", getConfig().getOptions().toggleInverseOverflowIcon));
                    }).bounds(width / 2 - 155 + buttonCount % 2 * 160, height / 6 - 12 + 24 * (buttonCount / 2), 150, 20)
                            .tooltip(Tooltip.create(Component.translatable("context.detailarmorbar.toggle.inverse_overflow_icon")))
                            .build()
            );
            buttonCount++;
        }

        Button features = addRenderableWidget(Button.builder(
                Component.translatable("option.detailarmorbar.title.features"), (matrixStack) -> {
            if (minecraft != null) {
                minecraft.setScreen(new OptionsScreen(parent, OptionType.FEATURES));
            }
        }).bounds(width / 2 - 152, height / 6 + 140, 60, 20).build());
        features.active = optionType != OptionType.FEATURES;

        Button animation = addRenderableWidget(Button.builder(
                Component.translatable("option.detailarmorbar.title.animation"), (matrixStack) -> {
            if (minecraft != null) {
                minecraft.setScreen(new OptionsScreen(parent, OptionType.ANIMATION));
            }
        }).bounds(width / 2 - 90, height / 6 + 140, 60, 20).build());
        animation.active = optionType != OptionType.ANIMATION;

        Button positioning = addRenderableWidget(Button.builder(
                Component.translatable("option.detailarmorbar.title.positioning"), (matrixStack) -> {
            if (minecraft != null) {
                minecraft.setScreen(new OptionsScreen(parent, OptionType.POSITIONING));
            }
        }).bounds(width / 2 - 28, height / 6 + 140, 60, 20).build());
        positioning.active = optionType != OptionType.POSITIONING;

        Button durability = addRenderableWidget(Button.builder(
                Component.translatable("option.detailarmorbar.title.durability"), (matrixStack) -> {
            if (minecraft != null) {
                minecraft.setScreen(new OptionsScreen(parent, OptionType.DURABILITY));
            }
        }).bounds(width / 2 + 34, height / 6 + 140, 60, 20).build());
        durability.active = optionType != OptionType.DURABILITY;

        Button etc = addRenderableWidget(Button.builder(
                Component.translatable("option.detailarmorbar.title.etc"), (matrixStack) -> {
            if (minecraft != null) {
                minecraft.setScreen(new OptionsScreen(parent, OptionType.ETC));
            }
        }).bounds(width / 2 + 96, height / 6 + 140, 60, 20).build());
        etc.active = optionType != OptionType.ETC;

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (matrixStack) -> {
            if (minecraft != null) {
                minecraft.setScreen(parent);
            }
        }).bounds(width / 2 - 100, height / 6 + 168, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        buttonCount = 0;
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    private <T extends Enum<T>> T getEnumNext(T target) {
        List<T> v = EnumSet.allOf(target.getDeclaringClass()).stream().toList();
        return v.get((v.indexOf(target) + 1) % v.size());
    }

    private <T extends Enum<T>> MutableComponent getEnumName(String type, T target) {
        return Component.translatable("option.detailarmorbar.effects."+type)
                .append(": ")
                .append(Component.translatable("option.detailarmorbar.effects."+type+"."+target.name().toLowerCase(Locale.ROOT)));
    }

    private <T extends Enum<T>> String getEnumDescription(String type, T target) {
        ArrayList<String> list = new ArrayList<>();
        for (String s : Component.translatable("context.detailarmorbar.effects." + type).getString().split("/"))
            list.add(Component.literal("§e" + s).getString());

        list.add("§f");

        List<T> v = EnumSet.allOf(target.getDeclaringClass()).stream().toList();
        for (T t : v) {
            list.add(Component.literal(" ")
                    .append(Component.translatable("option.detailarmorbar.effects."+type+"."+t.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.ITALIC))
                    .append(" - ")
                    .append(Component.translatable("context.detailarmorbar.effects."+type+"."+t.name().toLowerCase(Locale.ROOT))).getString());
        }
        return String.join("\n", list);
    }

    private MutableComponent getToggleName(String type, boolean target) {
        return Component.translatable("option.detailarmorbar.toggle."+type)
                .append(": ")
                .append(target ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }
    
    // Add this helper method for uniform color enum
    private <T extends Enum<T>> T getUniformColorNext(T value) {
        T[] values = value.getDeclaringClass().getEnumConstants();
        return values[(value.ordinal() + 1) % values.length];
    }
    
    private MutableComponent getUniformColorName(String key, ConfigEnumType.UniformColor value) {
        return Component.translatable("option.detailarmorbar.effects." + key)
                .append(": ")
                .append(Component.translatable("option.detailarmorbar.effects." + key + "." + value.name().toLowerCase(Locale.ROOT)));
    }
    
    private String getUniformColorDescription(String key, ConfigEnumType.UniformColor value) {
        return Component.translatable("context.detailarmorbar.effects." + key + "." + value.name().toLowerCase(Locale.ROOT)).getString();
    }

}
