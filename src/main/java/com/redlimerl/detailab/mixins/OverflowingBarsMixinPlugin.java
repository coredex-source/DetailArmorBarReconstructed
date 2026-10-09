package com.redlimerl.detailab.mixins;

import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class OverflowingBarsMixinPlugin implements IMixinConfigPlugin {

    private static final String OVERFLOWINGBARS_MOD_ID = "overflowingbars";
    private boolean overflowingBarsLoaded;

    @Override
    public void onLoad(String mixinPackage) {
        // ModList is not available yet while mixin configs load on NeoForge 21.1, so use the loading mod list.
        overflowingBarsLoaded = FMLLoader.getLoadingModList().getModFileById(OVERFLOWINGBARS_MOD_ID) != null;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains("OverflowingBars")) {
            return overflowingBarsLoaded;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
