package com.redlimerl.detailab.compat;

import com.redlimerl.detailab.DetailArmorBar;
import net.neoforged.fml.ModList;

public class ModCompatibility {

    private static Boolean overflowingBarsLoaded = null;
    private static Boolean overflowingBarsArmorLayerEnabled = null;
    private static Boolean overflowingBarsSkipEmptyArmorPoints = null;

    public static boolean isOverflowingBarsLoaded() {
        if (overflowingBarsLoaded == null) {
            overflowingBarsLoaded = ModList.get().isLoaded("overflowingbars");
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

            // Overflowing Bars 21.1.x names this field "allowLayers"; later versions use "allowArmorLayers".
            overflowingBarsArmorLayerEnabled = readBooleanField(armorConfig, "allowLayers", "allowArmorLayers");
            return overflowingBarsArmorLayerEnabled;
        } catch (Throwable e) {
            DetailArmorBar.LOGGER.warn("Could not read OverflowingBars armor layer config, assuming enabled", e);
            overflowingBarsArmorLayerEnabled = true;
            return true;
        }
    }

    public static boolean isOverflowingBarsSkipEmptyArmorPoints() {
        if (!isOverflowingBarsLoaded()) {
            return false;
        }

        if (overflowingBarsSkipEmptyArmorPoints != null) {
            return overflowingBarsSkipEmptyArmorPoints;
        }

        try {
            Class<?> overflowingBarsClass = Class.forName("fuzs.overflowingbars.OverflowingBars");
            Object configHolder = overflowingBarsClass.getField("CONFIG").get(null);
            Class<?> clientConfigClass = Class.forName("fuzs.overflowingbars.config.ClientConfig");
            Object clientConfig = configHolder.getClass().getMethod("get", Class.class).invoke(configHolder, clientConfigClass);
            Object armorConfig = clientConfigClass.getField("armor").get(clientConfig);

            overflowingBarsSkipEmptyArmorPoints = readBooleanField(armorConfig, "skipEmptyArmorPoints");
            return overflowingBarsSkipEmptyArmorPoints;
        } catch (Throwable e) {
            DetailArmorBar.LOGGER.warn("Could not read OverflowingBars skipEmptyArmorPoints config, assuming disabled", e);
            overflowingBarsSkipEmptyArmorPoints = false;
            return false;
        }
    }

    public static void resetCache() {
        overflowingBarsArmorLayerEnabled = null;
        overflowingBarsSkipEmptyArmorPoints = null;
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
}
