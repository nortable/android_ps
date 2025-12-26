package com.example.photoshop_demo.beautify;

/**
 * 美化效果枚举
 */
public enum BeautifyEffect {
    AUTO_ENHANCE("⚡", "自动增强", 0.8f),
    SHARPEN("🔍", "锐化", 0.6f),
    VIGNETTE("🎭", "暗角", 0.7f);
    
    private final String icon;
    private final String name;
    private final float defaultIntensity;
    
    BeautifyEffect(String icon, String name, float defaultIntensity) {
        this.icon = icon;
        this.name = name;
        this.defaultIntensity = defaultIntensity;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public String getName() {
        return name;
    }
    
    public float getDefaultIntensity() {
        return defaultIntensity;
    }
    
    public int getDefaultIntensityPercent() {
        return (int)(defaultIntensity * 100);
    }
}

