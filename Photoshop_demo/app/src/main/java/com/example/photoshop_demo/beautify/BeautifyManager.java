package com.example.photoshop_demo.beautify;

import android.graphics.Bitmap;

/**
 * 美化管理器
 * 统一管理所有美化效果的应用
 */
public class BeautifyManager {
    
    /**
     * 应用美化效果
     * @param source 原始图片
     * @param effect 美化效果
     * @param intensity 强度 0.0-1.0
     * @return 处理后的图片
     */
    public static Bitmap applyEffect(Bitmap source, BeautifyEffect effect, float intensity) {
        if (source == null || effect == null) {
            return source;
        }
        
        switch (effect) {
            case AUTO_ENHANCE:
                return AutoEnhanceFilter.apply(source, intensity);
            case SHARPEN:
                return SharpenFilter.apply(source, intensity);
            case VIGNETTE:
                return VignetteFilter.apply(source, intensity);
            default:
                return source;
        }
    }
}

