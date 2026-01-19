package com.redlimerl.detailab.mixins;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiGraphics.class)
public interface GuiGraphicsInvoker {
    @Invoker("innerBlit")
    void invokeInnerBlit(RenderPipeline pipeline, ResourceLocation texture, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color);
}
