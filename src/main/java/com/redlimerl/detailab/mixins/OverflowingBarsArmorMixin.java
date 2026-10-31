package com.redlimerl.detailab.mixins;

import com.redlimerl.detailab.render.ArmorBarRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.redlimerl.detailab.DetailArmorBar.getConfig;

/**
 * Mixin for OverflowingBars compatibility.
 */
@Mixin(targets = "fuzs.overflowingbars.client.gui.BarOverlayRenderer", remap = false)
public class OverflowingBarsArmorMixin {

    @Inject(method = "renderArmorLevelBar", at = @At("HEAD"), cancellable = true, require = 0)
    private static void detailab$replaceRenderArmorLevelBar(Minecraft minecraft, GuiGraphics guiGraphics, int leftHeight, boolean rowCount, boolean unmodified, CallbackInfo ci) {
        if (!getConfig().getOptions().toggleCompatibleHeartMod || !(minecraft.getCameraEntity() instanceof Player player)) {
            return;
        }

        int posY = guiGraphics.guiHeight() - leftHeight;

        ArmorBarRenderer.INSTANCE.render(guiGraphics, player, posY);
        ci.cancel();
    }
}
