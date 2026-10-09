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
 * Mixin for OverflowingBars compatibility (Overflowing Bars 21.1.x).
 * <p>
 * When Overflowing Bars' armor layers are enabled it cancels the vanilla armor layer and draws its own bar,
 * so the vanilla {@code Gui#renderArmor} hook never runs. When "Compatible Heart Mod" is enabled,
 * draw the detailed armor bar in place of Overflowing Bars' armor bar.
 */
@Mixin(targets = "fuzs.overflowingbars.client.gui.BarOverlayRenderer", remap = false)
public class OverflowingBarsArmorMixin {

    @Inject(method = "renderArmorLevelBar", at = @At("HEAD"), cancellable = true, require = 0)
    private static void detailab$replaceRenderArmorLevelBar(Minecraft minecraft, GuiGraphics guiGraphics, int leftHeight, boolean rowCount, boolean unmodified, CallbackInfo ci) {
        if (!getConfig().getOptions().toggleCompatibleHeartMod) {
            return;
        }

        if (minecraft.getCameraEntity() instanceof Player player) {
            int posY = guiGraphics.guiHeight() - leftHeight;

            ArmorBarRenderer.INSTANCE.render(guiGraphics, player, posY);
            ci.cancel();
        }
    }
}