//? if neoforge {
/*package com.redlimerl.detailab.loaders.neoforge;

import com.redlimerl.detailab.DetailArmorBar;
import com.redlimerl.detailab.loaders.Platform;
import com.redlimerl.detailab.screen.OptionsScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(DetailArmorBar.MOD_CONTAINER_ID)
public class DetailArmorBarNeoForge {
    public DetailArmorBarNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> OptionsScreen.create(parent));
        Platform.registerNeoForgeEvents(modEventBus);
        DetailArmorBar.initializeClient();
    }
}
*///?}
