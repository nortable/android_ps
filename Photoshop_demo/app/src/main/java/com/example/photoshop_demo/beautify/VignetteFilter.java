package com.example.photoshop_demo.beautify;

import android.graphics.Bitmap;

/**
 * 暗角滤镜
 * 使图片四周逐渐变暗，突出中心主体
 */
public class VignetteFilter {
    
    /**
     * 应用暗角效果
     * @param source 原始图片
     * @param intensity 强度 0.0-1.0
     * @return 添加暗角后的图片
     */
    public static Bitmap apply(Bitmap source, float intensity) {
        return apply(source, intensity, 0.7f);
    }
    
    /**
     * 应用暗角效果（可调半径）
     * @param source 原始图片
     * @param intensity 强度 0.0-1.0
     * @param radius 暗角半径 0.5-1.5（相对于图片尺寸）
     * @return 添加暗角后的图片
     */
    public static Bitmap apply(Bitmap source, float intensity, float radius) {
        if (source == null || intensity <= 0) {
            return source;
        }
        
        int width = source.getWidth();
        int height = source.getHeight();
        
        int[] pixels = new int[width * height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);
        
        // 计算中心点和最大距离
        float centerX = width / 2.0f;
        float centerY = height / 2.0f;
        float maxDistance = (float)Math.sqrt(centerX * centerX + centerY * centerY);
        
        // 调整半径
        float vignetteRadius = maxDistance * radius;
        
        // 应用暗角
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int pixel = pixels[index];
                
                // 计算到中心的距离
                float dx = x - centerX;
                float dy = y - centerY;
                float distance = (float)Math.sqrt(dx * dx + dy * dy);
                
                // 计算亮度衰减系数
                float factor = 1.0f;
                if (distance > vignetteRadius) {
                    float normalizedDist = (distance - vignetteRadius) / 
                                         (maxDistance - vignetteRadius);
                    // 使用二次衰减（更自然）
                    factor = 1.0f - normalizedDist * normalizedDist * intensity;
                    factor = Math.max(0.2f, factor);  // 最暗不超过80%
                }
                
                // 应用到RGB通道
                int r = (int)(((pixel >> 16) & 0xFF) * factor);
                int g = (int)(((pixel >> 8) & 0xFF) * factor);
                int b = (int)((pixel & 0xFF) * factor);
                
                pixels[index] = (0xFF << 24) | (r << 16) | (g << 8) | b;
            }
        }
        
        Bitmap result = Bitmap.createBitmap(width, height, 
            source.getConfig() != null ? source.getConfig() : Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }
}

