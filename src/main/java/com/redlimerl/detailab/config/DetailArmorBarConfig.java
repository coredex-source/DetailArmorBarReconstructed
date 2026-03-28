package com.redlimerl.detailab.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redlimerl.detailab.DetailArmorBar;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.awt.Color;

import static com.redlimerl.detailab.config.ConfigEnumType.*;
import com.redlimerl.detailab.config.ConfigEnumType.HudPosition;

public class DetailArmorBarConfig {

    private final File file;
    private Options options = null;
    private static final Gson GSON = new GsonBuilder()
                .setPrettyPrinting()
                .create();

    public DetailArmorBarConfig(File file) {
        this.file = file;
    }

    public Options getOptions() {
        return options;
    }

    public void load() {
        if (file.exists()) {
            try {
                options = GSON.fromJson(Files.readString(file.toPath(), StandardCharsets.UTF_8), Options.class);
            } catch (IOException e) {
                DetailArmorBar.LOGGER.error("Error loading config", e);
            }

            if (options != null && options.replaceInvalidOptions()) {
                save();
            }
        }
        if (options == null) {
            options = new Options();
            save();
        }
    }

    public void save() {
        try {
            Files.writeString(file.toPath(), GSON.toJson(options), StandardCharsets.UTF_8);
        } catch (IOException e) {
            DetailArmorBar.LOGGER.error("Error saving config", e);
        }
    }

    public static class Options {
        public static final Options DEFAULT = new Options();
        public ProtectionEffect effectType = ProtectionEffect.AURA;
        public EffectSpeed effectSpeed = EffectSpeed.NORMAL;
        public Animation effectThorn = Animation.ANIMATION;

        public boolean toggleEnchants = true;
        public boolean toggleArmorTypes = true;
        public boolean toggleThorns = true;
        public boolean toggleDurability = true;
        public boolean toggleMending = true;
        public boolean toggleEmptyBar = true;
        public boolean toggleItemBar = true;
        public boolean toggleHideBarWithoutArmor = true;
        public boolean toggleVanillaTexture = true;
        public boolean toggleCompatibleHeartMod = false;
        public boolean toggleInverseSlot = false;
        public boolean toggleSortSpecialItem = true;
        public boolean toggleAlignEnchantments = true;
        public boolean toggleUniformColor = false;
        public Integer uniformColorArgb = 0x5099FFFF;
        @Deprecated
        public UniformColor uniformColorType = UniformColor.AQUA;
        public int armorBarOffsetX = 0;
        public int armorBarOffsetY = 0;
        public boolean toggleArmorTrims = true;
        public boolean toggleDurabilityOverlay = false;
        public HudPosition durabilityHudPosition = HudPosition.BOTTOM_LEFT;
        public int durabilityHudOffsetX = 0;
        public int durabilityHudOffsetY = 0;
        public float durabilityHudScale = 1.0f;
        public boolean toggleDurabilityNotifications = true;
        public boolean toggleRepeatedDurabilityNotifications = false;
        public boolean toggleDurabilitySoundNotification = true;
        public boolean toggleDurabilityToastNotification = true;
        public boolean toggleDurabilityVisualEffect = true;
        public boolean toggleThreshold50 = false;
        public boolean toggleThreshold25 = true;
        public boolean toggleThreshold10 = true;
        public boolean toggleThreshold5 = true;
        public boolean toggleInventoryOverlay = false;

        public int getUniformColorArgb() {
            if (uniformColorArgb == null) {
                return DEFAULT.uniformColorArgb;
            }
            return uniformColorArgb;
        }

        public void setUniformColorArgb(int uniformColorArgb) {
            this.uniformColorArgb = uniformColorArgb;
        }

        public Color getUniformColor() {
            return new Color(getUniformColorArgb(), true);
        }

        boolean replaceInvalidOptions() {
            var invalid = false;
            if (effectType == null) {
                effectType = Options.DEFAULT.effectType;
                invalid = true;
            }
            if (effectSpeed == null) {
                effectSpeed = Options.DEFAULT.effectSpeed;
                invalid = true;
            }
            if (effectThorn == null) {
                effectThorn = Options.DEFAULT.effectThorn;
                invalid = true;
            }
            if (uniformColorArgb == null) {
                if (uniformColorType != null) {
                    uniformColorArgb = uniformColorType.getColor().getRGB();
                } else {
                    uniformColorArgb = Options.DEFAULT.uniformColorArgb;
                }
                invalid = true;
            }
            return invalid;
        }
    }
}