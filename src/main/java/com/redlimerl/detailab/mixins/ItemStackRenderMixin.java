package com.redlimerl.detailab.mixins;

import com.redlimerl.detailab.render.InventoryArmorOverlayRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to render protection type overlays on armor items in the inventory.
 * Hooks into the item rendering to add the enchantment display overlays.
 */
@Mixin(GuiGraphicsExtractor.class)
public class ItemStackRenderMixin {
    
    /**
     * Injects after item decorations are rendered to add our protection overlay.
     * This ensures our overlay appears on top of the item but can be covered by tooltips.
     */
    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", 
            at = @At("RETURN"))
    private void renderProtectionOverlay(net.minecraft.client.gui.Font font, ItemStack itemStack, int x, int y, String text, CallbackInfo ci) {
        if (itemStack != null && !itemStack.isEmpty()) {
            InventoryArmorOverlayRenderer.INSTANCE.renderOverlay((GuiGraphicsExtractor)(Object)this, itemStack, x, y);
        }
    }
}