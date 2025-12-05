package com.redlimerl.detailab.render;

import com.redlimerl.detailab.DetailArmorBar;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimAssets;
import net.minecraft.util.Identifier;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import static com.redlimerl.detailab.DetailArmorBar.getConfig;

public class ArmorTrimHandler {
    
    public static final Identifier TRIM_OVERLAY_TEXTURE = Identifier.of(DetailArmorBar.MOD_ID, "textures/armor_trim.png");
    public enum TrimMaterial {
        AMETHYST("amethyst", new Color(0x9A, 0x5C, 0xC6)),      // #9A5CC6
        COPPER("copper", new Color(0xB4, 0x68, 0x4D)),          // #B4684D
        DIAMOND("diamond", new Color(0x6E, 0xEC, 0xD2)),        // #6EECD2
        EMERALD("emerald", new Color(0x11, 0xA0, 0x36)),        // #11A036
        GOLD("gold", new Color(0xDE, 0xB1, 0x2D)),              // #DEB12D
        IRON("iron", new Color(0xEC, 0xEC, 0xEC)),              // #ECECEC
        LAPIS("lapis", new Color(0x41, 0x6E, 0x97)),            // #416E97
        NETHERITE("netherite", new Color(0x62, 0x58, 0x59)),    // #625859
        QUARTZ("quartz", new Color(0xE3, 0xD4, 0xC4)),          // #E3D4C4
        REDSTONE("redstone", new Color(0x97, 0x16, 0x07)),      // #971607
        RESIN("resin", new Color(0xFC, 0x78, 0x12));            // #FC7812
        
        private final String name;
        private final Color color;
        
        TrimMaterial(String name, Color color) {
            this.name = name;
            this.color = color;
        }
        
        public String getName() {
            return name;
        }
        
        public Color getColor() {
            return color;
        }
        
        public Color getColorWithAlpha(int alpha) {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        }

        public static TrimMaterial fromArmorTrimAssets(ArmorTrimAssets assets) {
            if (assets == null) return null;
            
            if (assets.equals(ArmorTrimAssets.AMETHYST)) return AMETHYST;
            if (assets.equals(ArmorTrimAssets.COPPER)) return COPPER;
            if (assets.equals(ArmorTrimAssets.DIAMOND)) return DIAMOND;
            if (assets.equals(ArmorTrimAssets.EMERALD)) return EMERALD;
            if (assets.equals(ArmorTrimAssets.GOLD)) return GOLD;
            if (assets.equals(ArmorTrimAssets.IRON)) return IRON;
            if (assets.equals(ArmorTrimAssets.LAPIS)) return LAPIS;
            if (assets.equals(ArmorTrimAssets.NETHERITE)) return NETHERITE;
            if (assets.equals(ArmorTrimAssets.QUARTZ)) return QUARTZ;
            if (assets.equals(ArmorTrimAssets.REDSTONE)) return REDSTONE;
            if (assets.equals(ArmorTrimAssets.RESIN)) return RESIN;
            
            return null;
        }
    }
    
    public record TrimInfo(TrimMaterial material, int armorPoints) {
        public Color getColor() {
            return material != null ? material.getColor() : null;
        }
    }
    
    public static boolean isEnabled() {
        return getConfig().getOptions().toggleArmorTrims;
    }
    
    public static TrimInfo getTrimInfo(PlayerEntity player, EquipmentSlot slot) {
        if (!isEnabled()) return null;
        
        ItemStack itemStack = player.getEquippedStack(slot);
        if (itemStack.isEmpty()) return null;
        
        ArmorTrim trim = itemStack.get(DataComponentTypes.TRIM);
        if (trim == null) return null;
        
        ArmorTrimAssets assets = trim.material().value().assets();
        TrimMaterial material = TrimMaterial.fromArmorTrimAssets(assets);
        
        if (material == null) return null;

        int armorPoints = getDefense(itemStack, slot);
        
        return new TrimInfo(material, armorPoints);
    }
    
    public static Map<EquipmentSlot, TrimInfo> getAllTrimInfo(PlayerEntity player) {
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
        return itemStack.get(DataComponentTypes.TRIM) != null;
    }
    
    public static Color getTrimColor(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        
        ArmorTrim trim = itemStack.get(DataComponentTypes.TRIM);
        if (trim == null) return null;
        
        ArmorTrimAssets assets = trim.material().value().assets();
        TrimMaterial material = TrimMaterial.fromArmorTrimAssets(assets);
        
        return material != null ? material.getColor() : null;
    }
    
    public static TrimMaterial getTrimMaterial(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        
        ArmorTrim trim = itemStack.get(DataComponentTypes.TRIM);
        if (trim == null) return null;
        
        ArmorTrimAssets assets = trim.material().value().assets();
        return TrimMaterial.fromArmorTrimAssets(assets);
    }
    
    private static int getDefense(ItemStack itemStack, EquipmentSlot slot) {
        var modifier = itemStack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        for (var entry : modifier.modifiers()) {
            if (entry.slot().matches(slot) && entry.attribute().equals(net.minecraft.entity.attribute.EntityAttributes.ARMOR)) {
                return (int) entry.modifier().value();
            }
        }
        return 0;
    }
}
