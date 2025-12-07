package com.redlimerl.detailab.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

@SuppressWarnings({"SuspiciousNameCombination", "SameParameterValue"})
public class InGameDrawer {
    public static void drawTexture(ResourceLocation identifier, GuiGraphics context, int x, int y, int u, int v, Color color, boolean mirror) {
        drawTexture(identifier, context, x, y, u, v, 128, 128, color, mirror);
    }

    public static void drawTexture(ResourceLocation identifier, GuiGraphics context, int x, int y, int u, int v, int width, int height, Color color, boolean mirror) {
        drawTexture(identifier, context, x, y, (float) u, (float) v, 9, 9, width, height, color, mirror);
    }

    public static void drawTexture(ResourceLocation identifier, GuiGraphics context, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, Color color, boolean mirror) {
        drawTexture(identifier, context, x, y, width, height, u, v, width, height, textureWidth, textureHeight, color, mirror);
    }

    public static void drawTexture(ResourceLocation identifier, GuiGraphics context, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, boolean mirror) {
        drawTexture(identifier, context, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight, Color.WHITE, mirror);
    }

    private static void drawTexture(ResourceLocation identifier, GuiGraphics context, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, Color color, boolean mirror) {
        RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        
        if(!mirror) {
            context.blit(identifier, x, y, u, v, width, height, textureWidth, textureHeight);
        } else {
            context.blit(identifier, x, y, u + regionWidth, v, width, height, textureWidth, textureHeight);
        }
        
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }
}
