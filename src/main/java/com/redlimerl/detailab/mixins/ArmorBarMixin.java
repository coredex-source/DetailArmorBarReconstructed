package com.redlimerl.detailab.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redlimerl.detailab.render.ArmorBarRenderer;
import static com.redlimerl.detailab.DetailArmorBar.getConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class ArmorBarMixin {

    @Inject(method = "renderArmor", at = @At("RETURN"))
    private static void renderArmorOverlay(GuiGraphics context, Player player, int y_base, int num_rows, int line_width, int x, CallbackInfo ci) {
        if(getConfig().getOptions().toggleCompatibleHeartMod){ num_rows = 1; }
        int y = y_base - (num_rows - 1) * line_width - 10;
        ArmorBarRenderer.INSTANCE.render(context, player, y);
    }

    @WrapOperation(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getArmorValue()I"))
    private static int supressGameArmorRenderer(Player playerEntity, Operation<Integer> operation) {
        return 0;
    }
}
