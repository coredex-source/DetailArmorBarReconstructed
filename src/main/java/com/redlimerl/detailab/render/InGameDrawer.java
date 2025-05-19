package com.redlimerl.detailab.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;

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
        RenderSystem.setShaderColor(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/100f);
        drawTexture(identifier, context, x, y, width, height, u, v, width, height, textureWidth, textureHeight, mirror);
    }

    public static void drawTexture(Identifier identifier, DrawContext context, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, boolean mirror) {
        if(!mirror) {
            context.drawTexture(RenderLayer::getGuiTextured, identifier, x, y, u, v, width, height, regionWidth, regionHeight, textureWidth, textureHeight);
        } else {
            context.drawTexture(RenderLayer::getGuiTextured, identifier, x, y, u, v, width, height, -regionWidth, regionHeight, textureWidth, textureHeight);
        }
    }
}
