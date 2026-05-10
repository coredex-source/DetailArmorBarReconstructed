package com.redlimerl.detailab;

import com.redlimerl.detailab.api.DetailArmorBarAPI;
import com.redlimerl.detailab.events.DamageEventHandler;
import com.redlimerl.detailab.events.DurabilityNotificationHandler;
import com.redlimerl.detailab.screen.OptionsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = DetailArmorBar.MOD_ID, dist = Dist.CLIENT)
public class DetailArmorBarClient {
    
    public DetailArmorBarClient(IEventBus modEventBus, ModContainer container) {
        // Register config screen factory - shows our custom OptionsScreen when clicking config button
        container.registerExtensionPoint(IConfigScreenFactory.class, 
            (modContainer, parent) -> OptionsScreen.create(parent));
        
        // Register mod event listeners
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::registerReloadListeners);
        
        // Register game event listeners for client tick events
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }
    
    private void onClientSetup(FMLClientSetupEvent event) {
        DetailArmorBar.LOGGER.info("Detail Armor Bar Reconstructed client setup complete!");
    }
    
    private void registerReloadListeners(AddClientReloadListenersEvent event) {
        // Register the armor bar loader as a resource reload listener
        event.addListener(Identifier.fromNamespaceAndPath(DetailArmorBar.MOD_ID, "armor_bar_loader"), DetailArmorBarAPI.LOADER);
    }
    
    // Client tick counter for thorns and damage event tracking
    private float lastHealth = -1;
    
    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        
        // Handle damage event for thorns animation
        if (client.player != null) {
            float currentHealth = client.player.getHealth();
            
            // If health decreased, player took damage - update thorns timestamp
            if (lastHealth > currentHealth && lastHealth != -1 && DamageEventHandler.hasThorns(client.player)) {
                com.redlimerl.detailab.render.ArmorBarRenderer.LAST_THORNS = DetailArmorBar.getTicks();
            }
            
            lastHealth = currentHealth;
        } else {
            lastHealth = -1; // Reset when player is null
        }
        
        // Handle durability notifications
        if (DetailArmorBar.getConfig().getOptions().toggleDurabilityNotifications && client.player != null) {
            DurabilityNotificationHandler.checkArmorDurability(client.player, client);
        }
    }
}
