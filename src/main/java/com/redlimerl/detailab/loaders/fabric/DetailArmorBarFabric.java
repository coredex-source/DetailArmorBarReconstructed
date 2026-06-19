//? if fabric {
package com.redlimerl.detailab.loaders.fabric;

import com.redlimerl.detailab.DetailArmorBar;
import net.fabricmc.api.ClientModInitializer;

public class DetailArmorBarFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DetailArmorBar.initializeClient();
    }
}
//?}
