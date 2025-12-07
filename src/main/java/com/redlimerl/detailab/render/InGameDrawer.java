package com.redlimerl.detailab.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

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
        RenderSystem.setShaderColor(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/100f);
        drawTexture(identifier, context, x, y, width, height, u, v, width, height, textureWidth, textureHeight, mirror);
    }

    public static void drawTexture(ResourceLocation identifier, GuiGraphics context, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, boolean mirror) {
        drawTexture(identifier, context, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight, mirror);
    }

    private static void drawTexture(ResourceLocation identifier, GuiGraphics context, int x0, int y0, int x1, int y1, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight, boolean mirror) {
        drawTexturedQuad(identifier, context.pose().last().pose(), x0, y0, x1, y1, z, 
            (u + 0.0F) / (float)textureWidth, 
            (u + (float)regionWidth) / (float)textureWidth, 
            (v + 0.0F) / (float)textureHeight, 
            (v + (float)regionHeight) / (float)textureHeight, 
            mirror);
    }

    private static void drawTexturedQuad(ResourceLocation identifier, Matrix4f matrices, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1, boolean mirror) {
        RenderSystem.setShaderTexture(0, identifier);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        if (mirror) {
            bufferBuilder.addVertex(matrices, (float)x0, (float)y1, (float)z).setUv(u1, v1);
            bufferBuilder.addVertex(matrices, (float)x1, (float)y1, (float)z).setUv(u0, v1);
            bufferBuilder.addVertex(matrices, (float)x1, (float)y0, (float)z).setUv(u0, v0);
            bufferBuilder.addVertex(matrices, (float)x0, (float)y0, (float)z).setUv(u1, v0);
        } else {
            bufferBuilder.addVertex(matrices, (float)x0, (float)y1, (float)z).setUv(u0, v1);
            bufferBuilder.addVertex(matrices, (float)x1, (float)y1, (float)z).setUv(u1, v1);
            bufferBuilder.addVertex(matrices, (float)x1, (float)y0, (float)z).setUv(u1, v0);
            bufferBuilder.addVertex(matrices, (float)x0, (float)y0, (float)z).setUv(u0, v0);
        }
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
