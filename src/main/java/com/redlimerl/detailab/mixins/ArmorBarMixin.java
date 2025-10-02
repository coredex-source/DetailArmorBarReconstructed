package com.redlimerl.detailab.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redlimerl.detailab.render.ArmorBarRenderer;
import static com.redlimerl.detailab.DetailArmorBar.getConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class ArmorBarMixin {

    @Inject(method = "renderArmor", at = @At("RETURN"))
    private static void renderArmorOverlay(DrawContext context, PlayerEntity player, int y_base, int num_rows, int line_width, int x, CallbackInfo ci) {
        if(getConfig().getOptions().toggleCompatibleHeartMod){ num_rows = 1; }
        int y = y_base - (num_rows - 1) * line_width - 10;
        ArmorBarRenderer.INSTANCE.render(context, player, y);
    }

    @WrapOperation(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getArmor()I"))
    private static int supressGameArmorRenderer(PlayerEntity playerEntity, Operation<Integer> operation) {
        return 0;
    }
}
