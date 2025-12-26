package com.example.photoshop_demo.beautify;

import android.graphics.Bitmap;
import com.example.photoshop_demo.ImageProcessor;

/**
 * 自动增强滤镜
 * 智能分析图片并自动优化亮度、对比度、饱和度
 */
public class AutoEnhanceFilter {
    
    /**
     * 应用自动增强效果
     * @param source 原始图片
     * @param intensity 强度 0.0-1.0
     * @return 增强后的图片
     */
    public static Bitmap apply(Bitmap source, float intensity) {
        if (source == null || intensity <= 0) {
            return source;
        }
        
        // 分析图片
        EnhanceParams params = analyzeImage(source);
        
        // 根据强度调整参数
        int brightness = (int)(params.brightness * intensity);
        float contrast = 1.0f + (params.contrast - 1.0f) * intensity;
        float saturation = 1.0f + (params.saturation - 1.0f) * intensity;
        
        // 应用调整（使用已有的ImageProcessor）
        return ImageProcessor.adjustAll(source, brightness, contrast, saturation);
    }
    
    /**
     * 分析图片并计算优化参数
     */
    private static EnhanceParams analyzeImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        
        // 计算亮度直方图
        float totalBrightness = 0;
        
        for (int pixel : pixels) {
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            
            // 计算感知亮度
            int brightness = (int)(0.299 * r + 0.587 * g + 0.114 * b);
            totalBrightness += brightness;
        }
        
        // 计算平均亮度
        float avgBrightness = totalBrightness / pixels.length;
        
        // 计算对比度（标准差）
        float variance = 0;
        for (int pixel : pixels) {
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            int brightness = (int)(0.299 * r + 0.587 * g + 0.114 * b);
            variance += Math.pow(brightness - avgBrightness, 2);
        }
        float contrast = (float)Math.sqrt(variance / pixels.length);
        
        // 计算饱和度
        float totalSaturation = 0;
        for (int pixel : pixels) {
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            
            int max = Math.max(r, Math.max(g, b));
            int min = Math.min(r, Math.min(g, b));
            float saturation = max == 0 ? 0 : (float)(max - min) / max;
            totalSaturation += saturation;
        }
        float avgSaturation = totalSaturation / pixels.length;
        
        // 制定优化策略
        EnhanceParams params = new EnhanceParams();
        
        // 亮度调整（目标：128附近）
        if (avgBrightness < 100) {
            params.brightness = (int)((120 - avgBrightness) * 0.5f);
        } else if (avgBrightness < 120) {
            params.brightness = (int)((128 - avgBrightness) * 0.3f);
        } else if (avgBrightness > 150) {
            params.brightness = (int)((135 - avgBrightness) * 0.2f);
        }
        
        // 对比度调整（目标：50-60）
        if (contrast < 40) {
            params.contrast = 1.0f + (45 - contrast) / 100f;
        } else if (contrast < 50) {
            params.contrast = 1.0f + (50 - contrast) / 200f;
        }
        params.contrast = Math.min(1.3f, params.contrast);
        
        // 饱和度调整（目标：0.4-0.5）
        if (avgSaturation < 0.35f) {
            params.saturation = 1.0f + (0.45f - avgSaturation) * 0.5f;
        } else if (avgSaturation < 0.4f) {
            params.saturation = 1.0f + (0.45f - avgSaturation) * 0.3f;
        }
        params.saturation = Math.min(1.2f, params.saturation);
        
        return params;
    }
    
    /**
     * 增强参数
     */
    private static class EnhanceParams {
        int brightness = 0;
        float contrast = 1.0f;
        float saturation = 1.0f;
    }
}

