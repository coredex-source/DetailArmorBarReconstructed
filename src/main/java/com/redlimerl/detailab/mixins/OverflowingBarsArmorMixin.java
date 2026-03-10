package com.redlimerl.detailab.mixins;

import com.redlimerl.detailab.render.ArmorBarRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for OverflowingBars compatibility.
 */
@Mixin(targets = "fuzs.overflowingbars.client.gui.BarOverlayRenderer", remap = false)
public class OverflowingBarsArmorMixin {

    @Inject(method = "renderArmorLevelBar", at = @At("RETURN"), require = 0)
    private static void detailab$afterRenderArmorBar(GuiGraphicsExtractor guiGraphics, Player player, int leftHeight, boolean rowCount, CallbackInfo ci) {
        int posY = guiGraphics.guiHeight() - leftHeight;

        ArmorBarRenderer.INSTANCE.render(guiGraphics, player, posY);
    }
}