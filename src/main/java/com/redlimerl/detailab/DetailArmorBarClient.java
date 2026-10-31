package com.redlimerl.detailab;

import com.mojang.blaze3d.platform.InputConstants;
import com.redlimerl.detailab.api.DetailArmorBarAPI;
import com.redlimerl.detailab.events.DamageEventHandler;
import com.redlimerl.detailab.events.DurabilityNotificationHandler;
import com.redlimerl.detailab.screen.OptionsScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = DetailArmorBar.MOD_ID, dist = Dist.CLIENT)
public class DetailArmorBarClient {

    // Keybind for opening config screen (unbound by default)
    private static final KeyMapping OPEN_CONFIG_KEY = new KeyMapping(
            "key.detailab.open_config",
            InputConstants.UNKNOWN.getValue(), // Unbound by default
            "key.category.detailab.keybinds"
    );

    public DetailArmorBarClient(IEventBus modEventBus, ModContainer container) {
        // Register config screen factory - shows our custom OptionsScreen when clicking config button
        container.registerExtensionPoint(IConfigScreenFactory.class,
            (modContainer, parent) -> OptionsScreen.create(parent));

        // Register mod event listeners
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::registerReloadListeners);
        modEventBus.addListener(this::registerKeyMappings);

        // Register game event listeners for client tick events
        NeoForge.EVENT_BUS.addListener(this::onClientTick);

        // Register damage event handler for thorns animation
        DamageEventHandler.register();

        // Register durability notification handler
        DurabilityNotificationHandler.register();
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        DetailArmorBar.LOGGER.info("Detail Armor Bar Reconstructed client setup complete!");
    }

    private void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        // Register the armor bar loader as a resource reload listener
        event.registerReloadListener(DetailArmorBarAPI.LOADER);
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CONFIG_KEY);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        while (OPEN_CONFIG_KEY.consumeClick()) {
            if (client.screen == null) {
                client.setScreen(OptionsScreen.create(null));
            }
        }
    }
}
