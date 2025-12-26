package com.example.photoshop_demo.filter;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * LUT生成工具
 * 用于生成基础的LUT图片
 */
public class LutGenerator {
    
    private static final int LUT_SIZE = 512;  // 512x512 LUT图片
    private static final int BLOCK_SIZE = 64;  // 每个色块64x64
    
    /**
     * 生成单位LUT（原图 - 不做任何处理）
     */
    public static Bitmap generateIdentityLut() {
        Bitmap lut = Bitmap.createBitmap(LUT_SIZE, LUT_SIZE, Bitmap.Config.ARGB_8888);
        
        for (int b = 0; b < 8; b++) {
            for (int g = 0; g < 64; g++) {
                for (int r = 0; r < 64; r++) {
                    int row = b / 8;
                    int col = b % 8;
                    int x = col * BLOCK_SIZE + r;
                    int y = row * BLOCK_SIZE + g;
                    
                    int red = (r * 255) / 63;
                    int green = (g * 255) / 63;
                    int blue = (b * 255) / 7;
                    
                    lut.setPixel(x, y, Color.rgb(red, green, blue));
                }
            }
        }
        
        return lut;
    }
    
    /**
     * 生成黑白LUT
     */
    public static Bitmap generateGrayscaleLut() {
        Bitmap lut = generateIdentityLut();
        int[] pixels = new int[LUT_SIZE * LUT_SIZE];
        lut.getPixels(pixels, 0, LUT_SIZE, 0, 0, LUT_SIZE, LUT_SIZE);
        
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            
            // 转换为灰度
            int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);
            pixels[i] = Color.rgb(gray, gray, gray);
        }
        
        lut.setPixels(pixels, 0, LUT_SIZE, 0, 0, LUT_SIZE, LUT_SIZE);
        return lut;
    }
    
    /**
     * 生成暖色调LUT
     */
    public static Bitmap generateWarmLut() {
        Bitmap lut = generateIdentityLut();
        int[] pixels = new int[LUT_SIZE * LUT_SIZE];
        lut.getPixels(pixels, 0, LUT_SIZE, 0, 0, LUT_SIZE, LUT_SIZE);
        
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            
            // 增加红黄，减少蓝色
            r = Math.min(255, (int)(r * 1.15));
            g = Math.min(255, (int)(g * 1.05));
            b = Math.max(0, (int)(b * 0.85));
            
            pixels[i] = Color.rgb(r, g, b);
        }
        
        lut.setPixels(pixels, 0, LUT_SIZE, 0, 0, LUT_SIZE, LUT_SIZE);
        return lut;
    }
    
    /**
     * 生成冷色调LUT
     */
    public static Bitmap generateCoolLut() {
        Bitmap lut = generateIdentityLut();
        int[] pixels = new int[LUT_SIZE * LUT_SIZE];
        lut.getPixels(pixels, 0, LUT_SIZE, 0, 0, LUT_SIZE, LUT_SIZE);
        
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            
            // 减少红色，增加蓝色
            r = Math.max(0, (int)(r * 0.85));
            g = Math.min(255, (int)(g * 1.0));
            b = Math.min(255, (int)(b * 1.15));
            
            pixels[i] = Color.rgb(r, g, b);
        }
        
        lut.setPixels(pixels, 0, LUT_SIZE, 0, 0, LUT_SIZE, LUT_SIZE);
        return lut;
    }
    
    /**
     * 生成怀旧LUT
     */
    public static Bitmap generateVintageLut() {
        Bitmap lut = generateIdentityLut();
        int[] pixels = new int[LUT_SIZE * LUT_SIZE];
        lut.getPixels(pixels, 0, LUT_SIZE, 0, 0, LUT_SIZE, LUT_SIZE);
        
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            
            // 怀旧效果：泛黄、低饱和度
            int newR = (int)(0.393 * r + 0.769 * g + 0.189 * b);
            int newG = (int)(0.349 * r + 0.686 * g + 0.168 * b);
            int newB = (int)(0.272 * r + 0.534 * g + 0.131 * b);
            
            newR = Math.min(255, newR);
            newG = Math.min(255, newG);
            newB = Math.min(255, newB);
            
            pixels[i] = Color.rgb(newR, newG, newB);
        }
        
        lut.setPixels(pixels, 0, LUT_SIZE, 0, 0, LUT_SIZE, LUT_SIZE);
        return lut;
    }
    
    /**
     * 生成鲜艳LUT
     */
    public static Bitmap generateVividLut() {
        Bitmap lut = generateIdentityLut();
        int[] pixels = new int[LUT_SIZE * LUT_SIZE];
        lut.getPixels(pixels, 0, LUT_SIZE, 0, 0, LUT_SIZE, LUT_SIZE);
        
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            
            // 增强饱和度
            float max = Math.max(r, Math.max(g, b)) / 255.0f;
            float min = Math.min(r, Math.min(g, b)) / 255.0f;
            float delta = max - min;
            
            if (delta > 0) {
                float saturationBoost = 1.5f;
                float avg = (r + g + b) / 3.0f;
                
                r = (int)(avg + (r - avg) * saturationBoost);
                g = (int)(avg + (g - avg) * saturationBoost);
                b = (int)(avg + (b - avg) * saturationBoost);
                
                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));
            }
            
            pixels[i] = Color.rgb(r, g, b);
        }
        
        lut.setPixels(pixels, 0, LUT_SIZE, 0, 0, LUT_SIZE, LUT_SIZE);
        return lut;
    }
    
    /**
     * 生成浪漫（粉色调）LUT
     */
    public static Bitmap generateRomanticLut() {
        Bitmap lut = generateIdentityLut();
        int[] pixels = new int[LUT_SIZE * LUT_SIZE];
        lut.getPixels(pixels, 0, LUT_SIZE, 0, 0, LUT_SIZE, LUT_SIZE);
        
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            
            // 增加红色和蓝色，略微提亮
            r = Math.min(255, (int)(r * 1.1 + 10));
            g = Math.min(255, (int)(g * 0.95 + 5));
            b = Math.min(255, (int)(b * 1.05 + 8));
            
            pixels[i] = Color.rgb(r, g, b);
        }
        
        lut.setPixels(pixels, 0, LUT_SIZE, 0, 0, LUT_SIZE, LUT_SIZE);
        return lut;
    }
    
    /**
     * 生成电影感LUT
     */
    public static Bitmap generateCinematicLut() {
        Bitmap lut = generateIdentityLut();
        int[] pixels = new int[LUT_SIZE * LUT_SIZE];
        lut.getPixels(pixels, 0, LUT_SIZE, 0, 0, LUT_SIZE, LUT_SIZE);
        
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            
            // 电影感：高对比，暗部偏蓝，高光偏黄
            float brightness = (r + g + b) / 3.0f / 255.0f;
            
            if (brightness < 0.5f) {
                // 暗部偏蓝
                r = (int)(r * 0.9);
                g = (int)(g * 0.95);
                b = (int)(b * 1.1);
            } else {
                // 高光偏黄
                r = (int)(r * 1.05);
                g = (int)(g * 1.02);
                b = (int)(b * 0.95);
            }
            
            // 增强对比度
            int contrast = (int)((r - 128) * 1.2 + 128);
            r = Math.max(0, Math.min(255, contrast));
            contrast = (int)((g - 128) * 1.2 + 128);
            g = Math.max(0, Math.min(255, contrast));
            contrast = (int)((b - 128) * 1.2 + 128);
            b = Math.max(0, Math.min(255, contrast));
            
            pixels[i] = Color.rgb(r, g, b);
        }
        
        lut.setPixels(pixels, 0, LUT_SIZE, 0, 0, LUT_SIZE, LUT_SIZE);
        return lut;
    }
}

