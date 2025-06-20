package com.redlimerl.detailab.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.client.gl.RenderPipelines;
import com.mojang.blaze3d.pipeline.RenderPipeline;

import java.awt.*;

@SuppressWarnings({"SuspiciousNameCombination", "SameParameterValue"})
public class InGameDrawer {
    public static void drawTexture(Identifier identifier, DrawContext context, int x, int y, int u, int v, Color color, boolean mirror) {
        drawTexture(identifier, context, x, y, u, v, 128, 128, color, mirror);
    }

    public static void drawTexture(Identifier identifier, DrawContext context, int x, int y, int u, int v, int width, int height, Color color, boolean mirror) {
        drawTexture(identifier, context, x, y, (float) u, (float) v, 9, 9, width, height, color, mirror);
    }

    public static void drawTexture(Identifier identifier, DrawContext context, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, Color color, boolean mirror) {
        drawTexture(identifier, context, x, y, width, height, u, v, width, height, textureWidth, textureHeight, color, mirror);
    }

    public static void drawTexture(Identifier identifier, DrawContext context, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, boolean mirror) {
        drawTexture(identifier, context, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight, -1, mirror);
    }

    private static void drawTexture(Identifier identifier, DrawContext context, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, Color color, boolean mirror) {
        drawTexture(identifier, context, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight, color.getRGB(), mirror);
    }

    static RenderPipeline pipeline = RenderPipelines.GUI_TEXTURED; // Update for 1.21.6
    private static void drawTexture(Identifier identifier, DrawContext context, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, int color, boolean mirror) {
        if(!mirror) {
            context.drawTexture(pipeline, identifier, x, y, u, v, width, height, regionWidth, regionHeight, textureWidth, textureHeight, color);
        } else {
            context.drawTexture(pipeline, identifier, x, y, u + (float)regionWidth, v, width, height, -regionWidth, regionHeight, textureWidth, textureHeight, color);
        }
    }
}
