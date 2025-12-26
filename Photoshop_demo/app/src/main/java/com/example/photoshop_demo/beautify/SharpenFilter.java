package com.example.photoshop_demo.beautify;

import android.graphics.Bitmap;

/**
 * 锐化滤镜
 * 使用卷积核增强图片细节和边缘
 */
public class SharpenFilter {
    
    /**
     * 应用锐化效果
     * @param source 原始图片
     * @param intensity 强度 0.0-1.0
     * @return 锐化后的图片
     */
    public static Bitmap apply(Bitmap source, float intensity) {
        if (source == null || intensity <= 0) {
            return source;
        }
        
        int width = source.getWidth();
        int height = source.getHeight();
        
        int[] srcPixels = new int[width * height];
        source.getPixels(srcPixels, 0, width, 0, 0, width, height);
        
        int[] dstPixels = new int[width * height];
        
        // 根据强度选择锐化核
        float[] kernel;
        if (intensity < 0.5f) {
            // 弱锐化（3x3）
            kernel = new float[] {
                0, -1,  0,
               -1,  5, -1,
                0, -1,  0
            };
        } else {
            // 强锐化（3x3）
            kernel = new float[] {
               -1, -1, -1,
               -1,  9, -1,
               -1, -1, -1
            };
        }
        
        int kernelSize = 3;
        int radius = kernelSize / 2;
        
        // 卷积处理
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float r = 0, g = 0, b = 0;
                
                // 应用卷积核
                for (int ky = -radius; ky <= radius; ky++) {
                    for (int kx = -radius; kx <= radius; kx++) {
                        int px = Math.max(0, Math.min(width - 1, x + kx));
                        int py = Math.max(0, Math.min(height - 1, y + ky));
                        
                        int pixel = srcPixels[py * width + px];
                        int kernelIndex = (ky + radius) * kernelSize + (kx + radius);
                        float weight = kernel[kernelIndex];
                        
                        r += ((pixel >> 16) & 0xFF) * weight;
                        g += ((pixel >> 8) & 0xFF) * weight;
                        b += (pixel & 0xFF) * weight;
                    }
                }
                
                // 限制范围
                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));
                
                int originalPixel = srcPixels[y * width + x];
                int sharpenedPixel = (0xFF << 24) | 
                                    ((int)r << 16) | 
                                    ((int)g << 8) | 
                                    (int)b;
                
                // 根据强度混合原图和锐化图
                int finalR = (int)(((originalPixel >> 16) & 0xFF) * (1 - intensity) + 
                                  ((sharpenedPixel >> 16) & 0xFF) * intensity);
                int finalG = (int)(((originalPixel >> 8) & 0xFF) * (1 - intensity) + 
                                  ((sharpenedPixel >> 8) & 0xFF) * intensity);
                int finalB = (int)((originalPixel & 0xFF) * (1 - intensity) + 
                                  (sharpenedPixel & 0xFF) * intensity);
                
                dstPixels[y * width + x] = (0xFF << 24) | 
                                          (finalR << 16) | 
                                          (finalG << 8) | 
                                          finalB;
            }
        }
        
        Bitmap result = Bitmap.createBitmap(width, height, 
            source.getConfig() != null ? source.getConfig() : Bitmap.Config.ARGB_8888);
        result.setPixels(dstPixels, 0, width, 0, 0, width, height);
        return result;
    }
}

