package com.redlimerl.detailab.render;

import com.redlimerl.detailab.mixins.GuiGraphicsInvoker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

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

    public static void drawTexture(ResourceLocation identifier, GuiGraphics context, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, Color color, boolean mirror) {
        drawTexture(identifier, context, x, x + width, y, y + height, regionWidth, regionHeight, u, v, textureWidth, textureHeight, color, mirror);
    }

    private static void drawTexture(ResourceLocation identifier, GuiGraphics context, int x0, int y0, int x1, int y1, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight, Color color, boolean mirror) {
        // Convert color to ARGB int
        // Original code used alpha/100f for shader (0-100 range mapped to 0.0-1.0, values >100 clamped to 1.0)
        // So we need to handle both conventions: alpha 0-100 and alpha 0-255 (standard Java Color)
        int alpha = color.getAlpha();
        if (alpha > 100) {
            // Standard 0-255 alpha range (e.g., Color.WHITE has alpha=255)
            // Keep as-is
        } else {
            // Custom 0-100 alpha range used by this mod's color system
            alpha = (int) (alpha / 100f * 255);
        }
        int argbColor = ARGB.color(alpha, color.getRed(), color.getGreen(), color.getBlue());
        
        float u0 = (u + 0.0F) / (float)textureWidth;
        float u1 = (u + (float)regionWidth) / (float)textureWidth;
        float v0 = (v + 0.0F) / (float)textureHeight;
        float v1 = (v + (float)regionHeight) / (float)textureHeight;
        
        if (mirror) {
            // Swap u coordinates for mirroring
            float temp = u0;
            u0 = u1;
            u1 = temp;
        }
        
        // Use mixin invoker to call private innerBlit method with color support
        ((GuiGraphicsInvoker) context).invokeInnerBlit(
            RenderPipelines.GUI_TEXTURED,
            identifier,
            x0, y0, x1, y1,
            u0, u1, v0, v1,
            argbColor
        );
    }
}
