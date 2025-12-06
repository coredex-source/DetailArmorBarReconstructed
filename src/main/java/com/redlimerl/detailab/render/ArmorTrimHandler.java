package com.redlimerl.detailab.render;

import com.redlimerl.detailab.DetailArmorBar;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.MaterialAssetGroup;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.resources.Identifier;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.redlimerl.detailab.DetailArmorBar.getConfig;

public class ArmorTrimHandler {
    
    public static final Identifier TRIM_OVERLAY_TEXTURE = Identifier.fromNamespaceAndPath(DetailArmorBar.MOD_ID, "textures/armor_trim.png");
    
    // Cache for dynamically generated colored trim textures
    private static final Map<TrimMaterial, Identifier> COLORED_TEXTURE_CACHE = new HashMap<>();
    private static boolean texturesGenerated = false;
    
    /**
     * Trim material colors matching Minecraft's official trim palette.
     * Each material has an 8-color gradient palette for proper shading.
     */
    public enum TrimMaterial {
        AMETHYST("amethyst", new Color(0x9A, 0x5C, 0xC6), new Color[]{
            new Color(0xFC, 0xC7, 0xFC), new Color(0xD8, 0xA3, 0xE8), new Color(0xB4, 0x7F, 0xD4),
            new Color(0x9A, 0x5C, 0xC6), new Color(0x7E, 0x49, 0xA8), new Color(0x62, 0x36, 0x8A),
            new Color(0x46, 0x23, 0x6C), new Color(0x2A, 0x10, 0x4E)
        }),
        COPPER("copper", new Color(0xB4, 0x68, 0x4D), new Color[]{
            new Color(0xFC, 0xC4, 0xA4), new Color(0xE0, 0x96, 0x78), new Color(0xCA, 0x7F, 0x62),
            new Color(0xB4, 0x68, 0x4D), new Color(0x9A, 0x54, 0x3C), new Color(0x80, 0x40, 0x2B),
            new Color(0x66, 0x2C, 0x1A), new Color(0x4C, 0x18, 0x09)
        }),
        DIAMOND("diamond", new Color(0x6E, 0xEC, 0xD2), new Color[]{
            new Color(0xD4, 0xFC, 0xFC), new Color(0xA0, 0xF4, 0xE8), new Color(0x87, 0xF0, 0xDD),
            new Color(0x6E, 0xEC, 0xD2), new Color(0x55, 0xD4, 0xBC), new Color(0x3C, 0xBC, 0xA6),
            new Color(0x23, 0xA4, 0x90), new Color(0x0A, 0x8C, 0x7A)
        }),
        EMERALD("emerald", new Color(0x11, 0xA0, 0x36), new Color[]{
            new Color(0x80, 0xFC, 0x90), new Color(0x50, 0xD8, 0x60), new Color(0x30, 0xBC, 0x4B),
            new Color(0x11, 0xA0, 0x36), new Color(0x0D, 0x86, 0x2D), new Color(0x09, 0x6C, 0x24),
            new Color(0x05, 0x52, 0x1B), new Color(0x01, 0x38, 0x12)
        }),
        GOLD("gold", new Color(0xDE, 0xB1, 0x2D), new Color[]{
            new Color(0xFC, 0xFC, 0x90), new Color(0xFC, 0xE0, 0x58), new Color(0xED, 0xC8, 0x42),
            new Color(0xDE, 0xB1, 0x2D), new Color(0xC0, 0x94, 0x20), new Color(0xA2, 0x77, 0x13),
            new Color(0x84, 0x5A, 0x06), new Color(0x66, 0x3D, 0x00)
        }),
        IRON("iron", new Color(0xEC, 0xEC, 0xEC), new Color[]{
            new Color(0xFC, 0xFC, 0xFC), new Color(0xEC, 0xEC, 0xEC), new Color(0xD4, 0xD4, 0xD4),
            new Color(0xBC, 0xBC, 0xBC), new Color(0xA0, 0xA0, 0xA0), new Color(0x84, 0x84, 0x84),
            new Color(0x68, 0x68, 0x68), new Color(0x4C, 0x4C, 0x4C)
        }),
        LAPIS("lapis", new Color(0x41, 0x6E, 0x97), new Color[]{
            new Color(0xA0, 0xC8, 0xFC), new Color(0x70, 0x9E, 0xD4), new Color(0x58, 0x86, 0xB5),
            new Color(0x41, 0x6E, 0x97), new Color(0x34, 0x5A, 0x7E), new Color(0x27, 0x46, 0x65),
            new Color(0x1A, 0x32, 0x4C), new Color(0x0D, 0x1E, 0x33)
        }),
        NETHERITE("netherite", new Color(0x62, 0x58, 0x59), new Color[]{
            new Color(0xA8, 0xA0, 0xA0), new Color(0x8C, 0x84, 0x84), new Color(0x77, 0x6E, 0x6F),
            new Color(0x62, 0x58, 0x59), new Color(0x50, 0x46, 0x47), new Color(0x3E, 0x34, 0x35),
            new Color(0x2C, 0x22, 0x23), new Color(0x1A, 0x10, 0x11)
        }),
        QUARTZ("quartz", new Color(0xE3, 0xD4, 0xC4), new Color[]{
            new Color(0xFC, 0xFC, 0xF0), new Color(0xF0, 0xE8, 0xD8), new Color(0xE9, 0xDE, 0xCE),
            new Color(0xE3, 0xD4, 0xC4), new Color(0xC8, 0xBC, 0xAC), new Color(0xAD, 0xA4, 0x94),
            new Color(0x92, 0x8C, 0x7C), new Color(0x77, 0x74, 0x64)
        }),
        REDSTONE("redstone", new Color(0x97, 0x16, 0x07), new Color[]{
            new Color(0xFC, 0x80, 0x70), new Color(0xE0, 0x50, 0x40), new Color(0xBB, 0x33, 0x23),
            new Color(0x97, 0x16, 0x07), new Color(0x7A, 0x10, 0x05), new Color(0x5D, 0x0A, 0x03),
            new Color(0x40, 0x04, 0x01), new Color(0x23, 0x00, 0x00)
        }),
        RESIN("resin", new Color(0xFC, 0x78, 0x12), new Color[]{
            new Color(0xFC, 0xD0, 0x90), new Color(0xFC, 0xA4, 0x50), new Color(0xFC, 0x8E, 0x31),
            new Color(0xFC, 0x78, 0x12), new Color(0xD8, 0x60, 0x08), new Color(0xB4, 0x48, 0x04),
            new Color(0x90, 0x30, 0x00), new Color(0x6C, 0x18, 0x00)
        });
        
        private final String name;
        private final Color color;
        private final Color[] palette;
        
        TrimMaterial(String name, Color color, Color[] palette) {
            this.name = name;
            this.color = color;
            this.palette = palette;
        }
        
        public String getName() {
            return name;
        }
        
        public Color getColor() {
            return color;
        }
        
        public Color[] getPalette() {
            return palette;
        }
        
        public Color getColorWithAlpha(int alpha) {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        }

        public static TrimMaterial fromArmorTrimAssets(MaterialAssetGroup assets) {
            if (assets == null) return null;
            
            if (assets.equals(MaterialAssetGroup.AMETHYST)) return AMETHYST;
            if (assets.equals(MaterialAssetGroup.COPPER)) return COPPER;
            if (assets.equals(MaterialAssetGroup.DIAMOND)) return DIAMOND;
            if (assets.equals(MaterialAssetGroup.EMERALD)) return EMERALD;
            if (assets.equals(MaterialAssetGroup.GOLD)) return GOLD;
            if (assets.equals(MaterialAssetGroup.IRON)) return IRON;
            if (assets.equals(MaterialAssetGroup.LAPIS)) return LAPIS;
            if (assets.equals(MaterialAssetGroup.NETHERITE)) return NETHERITE;
            if (assets.equals(MaterialAssetGroup.QUARTZ)) return QUARTZ;
            if (assets.equals(MaterialAssetGroup.REDSTONE)) return REDSTONE;
            if (assets.equals(MaterialAssetGroup.RESIN)) return RESIN;
            
            return null;
        }
    }
    
    public record TrimInfo(TrimMaterial material, int armorPoints) {
        public Color getColor() {
            return material != null ? material.getColor() : null;
        }
    }

    public static void generateColoredTextures() {
        if (texturesGenerated) return;
        
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getResourceManager() == null) return;
        
        try {
            Optional<Resource> resourceOpt = client.getResourceManager().getResource(TRIM_OVERLAY_TEXTURE);
            if (resourceOpt.isEmpty()) {
                DetailArmorBar.LOGGER.warn("Could not find armor trim overlay texture: {}", TRIM_OVERLAY_TEXTURE);
                return;
            }
            
            NativeImage baseImage;
            try (InputStream stream = resourceOpt.get().open()) {
                baseImage = NativeImage.read(stream);
            }
            
            for (TrimMaterial material : TrimMaterial.values()) {
                try {
                    Identifier coloredTextureId = generateColoredTexture(client, baseImage, material);
                    if (coloredTextureId != null) {
                        COLORED_TEXTURE_CACHE.put(material, coloredTextureId);
                    }
                } catch (Exception e) {
                    DetailArmorBar.LOGGER.error("Failed to generate colored trim texture for {}: {}", material.getName(), e.getMessage());
                }
            }
            
            baseImage.close();
            texturesGenerated = true;
            DetailArmorBar.LOGGER.info("Generated {} colored trim textures", COLORED_TEXTURE_CACHE.size());
            
        } catch (IOException e) {
            DetailArmorBar.LOGGER.error("Failed to load armor trim overlay texture: {}", e.getMessage());
        }
    }
    
    private static Identifier generateColoredTexture(Minecraft client, NativeImage baseImage, TrimMaterial material) {
        int width = baseImage.getWidth();
        int height = baseImage.getHeight();
        
        NativeImage coloredImage = new NativeImage(width, height, false);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = baseImage.getPixel(x, y);
                int newPixel = applyPaletteColor(pixel, material.getPalette());
                coloredImage.setPixel(x, y, newPixel);
            }
        }

        Identifier textureId = Identifier.fromNamespaceAndPath(DetailArmorBar.MOD_ID, "dynamic/trim_" + material.getName());
        DynamicTexture dynamicTexture = new DynamicTexture(() -> "trim_" + material.getName(), coloredImage);
        client.getTextureManager().register(textureId, dynamicTexture);
        
        return textureId;
    }
    
    /**
     * Apply palette coloring to a pixel based on its grayscale value.
     * The grayscale value determines which palette color to use.
     * 
     * Grayscale mapping:
     * 0xE0 (224) -> palette[0] (lightest)
     * 0xC0 (192) -> palette[1]
     * 0xA0 (160) -> palette[2]
     * 0x80 (128) -> palette[3] (mid)
     * 0x60 (96)  -> palette[4]
     * 0x40 (64)  -> palette[5]
     * 0x20 (32)  -> palette[6]
     * 0x00 (0)   -> palette[7] (darkest)
     */
    private static int applyPaletteColor(int pixel, Color[] palette) {
        int a = (pixel >> 24) & 0xFF;
        int r = (pixel >> 16) & 0xFF;
        int g = (pixel >> 8) & 0xFF;
        int b = pixel & 0xFF;

        if (a == 0) return pixel;

        int gray = (r + g + b) / 3;
        
        int index;
        if (gray >= 224) index = 0;      // 0xE0+
        else if (gray >= 192) index = 1; // 0xC0-0xDF
        else if (gray >= 160) index = 2; // 0xA0-0xBF
        else if (gray >= 128) index = 3; // 0x80-0x9F
        else if (gray >= 96) index = 4;  // 0x60-0x7F
        else if (gray >= 64) index = 5;  // 0x40-0x5F
        else if (gray >= 32) index = 6;  // 0x20-0x3F
        else index = 7;                   // 0x00-0x1F
        
        Color paletteColor = palette[index];
        
        return (a << 24) | (paletteColor.getRed() << 16) | (paletteColor.getGreen() << 8) | paletteColor.getBlue();
    }
    
    public static Identifier getColoredTexture(TrimMaterial material) {
        if (!texturesGenerated) {
            generateColoredTextures();
        }
        
        Identifier coloredTexture = COLORED_TEXTURE_CACHE.get(material);
        return coloredTexture != null ? coloredTexture : TRIM_OVERLAY_TEXTURE;
    }
    
    public static boolean hasColoredTextures() {
        if (!texturesGenerated) {
            generateColoredTextures();
        }
        return !COLORED_TEXTURE_CACHE.isEmpty();
    }
    
    public static boolean isEnabled() {
        return getConfig().getOptions().toggleArmorTrims;
    }
    
    public static TrimInfo getTrimInfo(Player player, EquipmentSlot slot) {
        if (!isEnabled()) return null;
        
        ItemStack itemStack = player.getItemBySlot(slot);
        if (itemStack.isEmpty()) return null;
        
        ArmorTrim trim = itemStack.get(DataComponents.TRIM);
        if (trim == null) return null;
        
        MaterialAssetGroup assets = trim.material().value().assets();
        TrimMaterial material = TrimMaterial.fromArmorTrimAssets(assets);
        
        if (material == null) return null;

        int armorPoints = getDefense(itemStack, slot);
        
        return new TrimInfo(material, armorPoints);
    }
    
    public static Map<EquipmentSlot, TrimInfo> getAllTrimInfo(Player player) {
        Map<EquipmentSlot, TrimInfo> result = new HashMap<>();
        
        if (!isEnabled()) return result;
        
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            TrimInfo info = getTrimInfo(player, slot);
            if (info != null) {
                result.put(slot, info);
            }
        }
        
        return result;
    }
    
    public static boolean hasTrim(ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;
        return itemStack.get(DataComponents.TRIM) != null;
    }
    
    public static Color getTrimColor(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        
        ArmorTrim trim = itemStack.get(DataComponents.TRIM);
        if (trim == null) return null;
        
        MaterialAssetGroup assets = trim.material().value().assets();
        TrimMaterial material = TrimMaterial.fromArmorTrimAssets(assets);
        
        return material != null ? material.getColor() : null;
    }
    
    public static TrimMaterial getTrimMaterial(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        
        ArmorTrim trim = itemStack.get(DataComponents.TRIM);
        if (trim == null) return null;
        
        MaterialAssetGroup assets = trim.material().value().assets();
        return TrimMaterial.fromArmorTrimAssets(assets);
    }
    
    private static int getDefense(ItemStack itemStack, EquipmentSlot slot) {
        var modifier = itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (var entry : modifier.modifiers()) {
            if (entry.slot().test(slot) && entry.attribute().equals(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR)) {
                return (int) entry.modifier().amount();
            }
        }
        return 0;
    }
    
    public static void clearCache() {
        COLORED_TEXTURE_CACHE.clear();
        texturesGenerated = false;
    }
}
