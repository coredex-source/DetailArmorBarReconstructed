package com.redlimerl.detailab.compat;

import net.fabricmc.loader.api.FabricLoader;

public class ModCompatibility {
    
    private static Boolean overflowingBarsLoaded = null;
    private static Boolean overflowingBarsArmorLayerEnabled = null;
    
    public static boolean isOverflowingBarsLoaded() {
        if (overflowingBarsLoaded == null) {
            overflowingBarsLoaded = FabricLoader.getInstance().isModLoaded("overflowingbars");
        }
        return overflowingBarsLoaded;
    }
    
    public static boolean isOverflowingBarsArmorLayerActive() {
        if (!isOverflowingBarsLoaded()) {
            return false;
        }
        
        if (overflowingBarsArmorLayerEnabled != null) {
            return overflowingBarsArmorLayerEnabled;
        }
        
        try {
            Class<?> overflowingBarsClass = Class.forName("fuzs.overflowingbars.OverflowingBars");
            Object configHolder = overflowingBarsClass.getField("CONFIG").get(null);
            Class<?> clientConfigClass = Class.forName("fuzs.overflowingbars.config.ClientConfig");
            Object clientConfig = configHolder.getClass().getMethod("get", Class.class).invoke(configHolder, clientConfigClass);
            Object armorConfig = clientConfigClass.getField("armor").get(clientConfig);
            
            overflowingBarsArmorLayerEnabled = (Boolean) armorConfig.getClass().getField("allowArmorLayers").get(armorConfig);
            return overflowingBarsArmorLayerEnabled;
        } catch (Throwable e) {
            overflowingBarsArmorLayerEnabled = true;
            return true;
        }
    }
    
    public static void resetCache() {
        overflowingBarsArmorLayerEnabled = null;
    }
}
