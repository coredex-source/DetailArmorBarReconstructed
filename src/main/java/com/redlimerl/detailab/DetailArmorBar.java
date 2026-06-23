package com.redlimerl.detailab;

import com.redlimerl.detailab.api.DetailArmorBarAPI;
import com.redlimerl.detailab.api.render.ArmorBarRenderManager;
import com.redlimerl.detailab.api.render.ItemBarRenderManager;
import com.redlimerl.detailab.api.render.TextureOffset;
import com.redlimerl.detailab.compat.ModCompatibility;
import com.redlimerl.detailab.config.DetailArmorBarConfig;
import com.redlimerl.detailab.loaders.Platform;
import com.redlimerl.detailab.screen.OptionsScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;

public class DetailArmorBar {

    public static Logger LOGGER = LogManager.getLogger("DetailArmorBar");
    public static final String MOD_CONTAINER_ID = "detailabreconst";
    public static final String MOD_ID = "detailab";
    public static final Identifier GUI_ARMOR_BAR = Identifier.fromNamespaceAndPath(MOD_ID, "textures/armor_bar.png");
    private final static String[] compatibilityMods = { "healthoverlay", "overflowingbars" };

    private static DetailArmorBarConfig config = null;
    
    // Keybind for opening config screen (unbound by default)
    private static KeyMapping openConfigKey;

    public static DetailArmorBarConfig getConfig() {
        if (config == null) loadConfig();
        return config;
    }

    public static long getTicks() {
        return System.currentTimeMillis()/50;
    }

    private static void loadConfig() {
        Path configPath = Platform.getConfigDir();
        File configFile = new File(configPath.toFile(), "detailarmorbar.json");
        config = new DetailArmorBarConfig(configFile);
        config.load();
    }

    public static void initializeClient() {
        Platform.registerResourceReloadListener(DetailArmorBarAPI.LOADER);

        // Register custom keybind category
        KeyMapping.Category detailArmorBarCategory = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "keybinds"));
        
        // Register keybind for opening config screen (unbound by default)
        openConfigKey = Platform.registerKeyMapping(new KeyMapping(
                "key.detailab.open_config",
                GLFW.GLFW_KEY_UNKNOWN, // Unbound by default
                detailArmorBarCategory
        ));
        
        // Register client tick event to check for keybind press
        Platform.registerClientTick(client -> {
            while (openConfigKey.consumeClick()) {
                //? if minecraft_26_1 {
                /*if (client.screen == null) {
                    client.setScreen(OptionsScreen.create(null));
                }
                *///?} else {
                if (client.gui.screen() == null) {
                    client.gui.setScreen(OptionsScreen.create(null));
                }
                //?}
            }
        });

        TextureOffset outline = new TextureOffset(9, 0);
        TextureOffset outlineHalf = new TextureOffset(27, 0);

        DetailArmorBarAPI.customArmorBarBuilder().armor(Items.NETHERITE_CHESTPLATE, Items.NETHERITE_HELMET, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS)
                .render((ItemStack itemStack) ->
            new ArmorBarRenderManager(GUI_ARMOR_BAR, 128, 128,
                    new TextureOffset(9, 9 + isVanillaTexture()), new TextureOffset(0, 9 + isVanillaTexture()), outline, outlineHalf)
        ).register();

        DetailArmorBarAPI.customArmorBarBuilder().armor(Items.DIAMOND_HELMET, Items.DIAMOND_LEGGINGS, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_BOOTS)
                .render((ItemStack itemStack) ->
            new ArmorBarRenderManager(GUI_ARMOR_BAR, 128, 128,
                    new TextureOffset(27, 9 + isVanillaTexture()), new TextureOffset(18, 9 + isVanillaTexture()), outline, outlineHalf)
        ).register();

        DetailArmorBarAPI.customArmorBarBuilder().armor(Items.TURTLE_HELMET)
                .render((ItemStack itemStack) ->
            new ArmorBarRenderManager(GUI_ARMOR_BAR, 128, 128,
                    new TextureOffset(45, 9 + isVanillaTexture()), new TextureOffset(36, 9 + isVanillaTexture()), outline, outlineHalf)
        ).register();

        DetailArmorBarAPI.customArmorBarBuilder().armor(Items.IRON_HELMET, Items.IRON_LEGGINGS, Items.IRON_CHESTPLATE, Items.IRON_BOOTS)
                .render((ItemStack itemStack) ->
            new ArmorBarRenderManager(GUI_ARMOR_BAR, 128, 128,
                    new TextureOffset(63, 9 + isVanillaTexture()), new TextureOffset(54, 9 + isVanillaTexture()), outline, outlineHalf)
        ).register();

        DetailArmorBarAPI.customArmorBarBuilder().armor(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_BOOTS)
                .render((ItemStack itemStack) ->
            new ArmorBarRenderManager(GUI_ARMOR_BAR, 128, 128,
                    new TextureOffset(81, 9 + isVanillaTexture()), new TextureOffset(72, 9 + isVanillaTexture()), outline, outlineHalf)
        ).register();

        DetailArmorBarAPI.customArmorBarBuilder().armor(Items.GOLDEN_HELMET, Items.GOLDEN_LEGGINGS, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_BOOTS)
                .render((ItemStack itemStack) ->
            new ArmorBarRenderManager(GUI_ARMOR_BAR, 128, 128,
                    new TextureOffset(99, 9 + isVanillaTexture()), new TextureOffset(90, 9 + isVanillaTexture()), outline, outlineHalf)
        ).register();

        //Shifted it down to match the portion in armor_bar.png .. Just for my ease of development.
        DetailArmorBarAPI.customArmorBarBuilder().armor(Items.COPPER_HELMET, Items.COPPER_LEGGINGS, Items.COPPER_CHESTPLATE, Items.COPPER_BOOTS)
                .render((ItemStack itemStack) ->
            new ArmorBarRenderManager(GUI_ARMOR_BAR, 128, 128,
                    new TextureOffset(9, 74 + isVanillaTexture()), new TextureOffset(0, 74 + isVanillaTexture()), outline, outlineHalf)
        ).register();

        DetailArmorBarAPI.customArmorBarBuilder().armor(Items.LEATHER_HELMET, Items.LEATHER_LEGGINGS, Items.LEATHER_CHESTPLATE, Items.LEATHER_BOOTS)
                .render((ItemStack itemStack) -> {
                    var leatherArmor = DyedItemColor.getOrDefault(itemStack, -6265536);
                    var color = new Color(leatherArmor);
                    return new ArmorBarRenderManager(GUI_ARMOR_BAR, 128, 128,
                                    new TextureOffset(117, 9 + isVanillaTexture()), new TextureOffset(108, 9 + isVanillaTexture()), outline, outlineHalf, color);
                }
        ).register();

        DetailArmorBarAPI.customItemBarBuilder().item(Items.ELYTRA)
                .render((ItemStack itemStack) ->
            new ItemBarRenderManager(GUI_ARMOR_BAR, 128, 128,
                    new TextureOffset(36, 0), new TextureOffset(54, 0), true)
        ).register();

        // Register damage event handler for thorns animation
        com.redlimerl.detailab.events.DamageEventHandler.register();
        
        // Register durability notification handler
        com.redlimerl.detailab.events.DurabilityNotificationHandler.register();

        for (String compatibilityMod : compatibilityMods) {
            if (Platform.isModLoaded(compatibilityMod)) {
                getConfig().getOptions().toggleCompatibleHeartMod = true;
            }
        }
        
        if (ModCompatibility.isOverflowingBarsLoaded()) {
            LOGGER.info("OverflowingBars detected - using compatible rendering mode");
        }
    }

    public static int isVanillaTexture() {
        return getConfig().getOptions().toggleVanillaTexture ? 45 : 0;
    }
}
