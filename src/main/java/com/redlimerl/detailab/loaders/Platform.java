package com.redlimerl.detailab.loaders;

import com.redlimerl.detailab.DetailArmorBar;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class Platform {

    private static final List<Consumer<Minecraft>> CLIENT_TICK_HANDLERS = new ArrayList<>();
    private static final List<KeyMapping> KEY_MAPPINGS = new ArrayList<>();
    private static final List<PreparableReloadListener> RELOAD_LISTENERS = new ArrayList<>();

    private Platform() {
    }

    public static Path getConfigDir() {
        //? if fabric {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
        //?} else if neoforge {
        /*return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
        *///?}
    }

    public static boolean isModLoaded(String modId) {
        //? if fabric {
        return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(modId);
        //?} else if neoforge {
        /*return net.neoforged.fml.ModList.get().isLoaded(modId);
        *///?}
    }

    public static void registerResourceReloadListener(PreparableReloadListener listener) {
        RELOAD_LISTENERS.add(listener);
        //? if fabric {
        net.fabricmc.fabric.api.resource.ResourceManagerHelper.get(net.minecraft.server.packs.PackType.CLIENT_RESOURCES)
                .registerReloadListener((net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener) listener);
        //?}
    }

    public static KeyMapping registerKeyMapping(KeyMapping keyMapping) {
        KEY_MAPPINGS.add(keyMapping);
        //? if fabric {
        return net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.registerKeyMapping(keyMapping);
        //?} else if neoforge {
        /*return keyMapping;
        *///?}
    }

    public static void registerClientTick(Consumer<Minecraft> handler) {
        CLIENT_TICK_HANDLERS.add(handler);
        //? if fabric {
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(handler::accept);
        //?}
    }

    public static void runClientTick(Minecraft client) {
        for (Consumer<Minecraft> handler : CLIENT_TICK_HANDLERS) {
            handler.accept(client);
        }
    }

    //? if neoforge {
    /*public static void registerNeoForgeEvents(net.neoforged.bus.api.IEventBus modEventBus) {
        modEventBus.addListener(Platform::registerNeoForgeReloadListeners);
        modEventBus.addListener(Platform::registerNeoForgeKeyMappings);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(Platform::onNeoForgeClientTick);
    }

    private static void registerNeoForgeReloadListeners(net.neoforged.neoforge.client.event.AddClientReloadListenersEvent event) {
        for (PreparableReloadListener listener : RELOAD_LISTENERS) {
            event.addListener(net.minecraft.resources.Identifier.fromNamespaceAndPath(DetailArmorBar.MOD_ID, "armor"), listener);
        }
    }

    private static void registerNeoForgeKeyMappings(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) {
        for (KeyMapping keyMapping : KEY_MAPPINGS) {
            event.register(keyMapping);
        }
    }

    private static void onNeoForgeClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        runClientTick(Minecraft.getInstance());
    }
    *///?}
}
