package com.redlimerl.detailab.config;

import java.awt.Color;

public class ConfigEnumType {

    public enum ProtectionEffect {
        NONE, AURA, OUTLINE
    }

    public enum EffectSpeed {
        VERY_SLOW, SLOW, NORMAL, FAST, VERY_FAST
    }

    public enum Animation {
        STATIC, ANIMATION
    }
    
    public enum UniformColor {
        AQUA(new Color(153, 255, 255, 80)),
        PURPLE(new Color(112, 51, 173, 80)),
        YELLOW(new Color(255, 255, 0, 80)),
        ORANGE(new Color(210, 56, 0, 80)),
        WHITE(new Color(255, 255, 255, 80)),
        GREEN(new Color(0, 255, 0, 80)),
        BLUE(new Color(0, 0, 255, 80)),
        RED(new Color(255, 0, 0, 80));
        
        private final Color color;
        
        UniformColor(Color color) {
            this.color = color;
        }
        
        public Color getColor() {
            return color;
        }
    }
}
