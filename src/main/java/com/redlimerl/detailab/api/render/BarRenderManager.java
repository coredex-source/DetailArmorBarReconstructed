package com.redlimerl.detailab.api.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public interface BarRenderManager {

    @NotNull
    Texture getTextureFull();

    @NotNull
    Texture getTextureHalf();

    @NotNull
    Texture getTextureOutline();

    @NotNull
    Texture getTextureOutlineHalf();

    boolean isHidden();

    @NotNull
    Color getColor();

    record Texture(Identifier location, int width, int height, TextureOffset offset) {

        public static final Codec<Texture> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("texture").forGetter(Texture::location),
                ExtraCodecs.POSITIVE_INT.fieldOf("texture_width").forGetter(Texture::width),
                ExtraCodecs.POSITIVE_INT.fieldOf("texture_height").forGetter(Texture::height),
                TextureOffset.CODEC.fieldOf("offset").forGetter(Texture::offset)
        ).apply(instance, Texture::new));

    }
}
