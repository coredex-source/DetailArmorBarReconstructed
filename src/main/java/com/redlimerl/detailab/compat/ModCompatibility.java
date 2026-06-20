package com.redlimerl.detailab.compat;

import com.redlimerl.detailab.DetailArmorBar;
import com.redlimerl.detailab.loaders.Platform;

public class ModCompatibility {
    
    private static Boolean overflowingBarsLoaded = null;
    private static Boolean overflowingBarsArmorLayerEnabled = null;
    
    public static boolean isOverflowingBarsLoaded() {
        if (overflowingBarsLoaded == null) {
            overflowingBarsLoaded = Platform.isModLoaded("overflowingbars");
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
            Class<?> overflowingBarsClass = getClass("fuzs.overflowingbars.common.OverflowingBars", "fuzs.overflowingbars.OverflowingBars");
            Object configHolder = overflowingBarsClass.getField("CONFIG").get(null);
            Class<?> clientConfigClass = getClass("fuzs.overflowingbars.common.config.ClientConfig", "fuzs.overflowingbars.config.ClientConfig");
            Object clientConfig = configHolder.getClass().getMethod("get", Class.class).invoke(configHolder, clientConfigClass);
            Object armorConfig = clientConfigClass.getField("armor").get(clientConfig);
            
            overflowingBarsArmorLayerEnabled = readBooleanField(armorConfig, "allowArmorLayers", "allowLayers");
            return overflowingBarsArmorLayerEnabled;
        } catch (Throwable e) {
            DetailArmorBar.LOGGER.warn("Could not read OverflowingBars armor layer config, assuming disabled", e);
            overflowingBarsArmorLayerEnabled = false;
            return false;
        }
    }
    
    public static void resetCache() {
        overflowingBarsArmorLayerEnabled = null;
    }

    private static boolean readBooleanField(Object object, String... fieldNames) throws ReflectiveOperationException {
        ReflectiveOperationException lastException = null;
        for (String fieldName : fieldNames) {
            try {
                return (Boolean) object.getClass().getField(fieldName).get(object);
            } catch (ReflectiveOperationException e) {
                lastException = e;
            }
        }

        throw lastException != null ? lastException : new NoSuchFieldException();
    }

    private static Class<?> getClass(String... classNames) throws ClassNotFoundException {
        ClassNotFoundException lastException = null;
        for (String className : classNames) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                lastException = e;
            }
        }

        throw lastException != null ? lastException : new ClassNotFoundException();
    }
}
