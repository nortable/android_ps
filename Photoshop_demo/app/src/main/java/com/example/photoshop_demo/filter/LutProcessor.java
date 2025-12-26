package com.example.photoshop_demo.filter;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import java.io.IOException;
import java.io.InputStream;

/**
 * LUT处理引擎
 * 核心功能：将LUT应用到图片
 */
public class LutProcessor {
    
    private static final int LUT_SIZE = 64;  // 64x64x64色彩分辨率
    
    /**
     * 从assets加载LUT图片
     */
    public static Bitmap loadLutFromAssets(Context context, String lutPath) {
        try {
            AssetManager am = context.getAssets();
            InputStream is = am.open(lutPath);
            Bitmap lut = BitmapFactory.decodeStream(is);
            is.close();
            return lut;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 应用LUT到图片（快速版本 - 使用像素数组）
     * @param source 原始图片
     * @param lutBitmap LUT位图
     * @param intensity 强度 (0.0 - 1.0)
     * @return 处理后的图片
     */
    public static Bitmap applyLut(Bitmap source, Bitmap lutBitmap, float intensity) {
        if (source == null || lutBitmap == null) {
            return source;
        }
        
        int width = source.getWidth();
        int height = source.getHeight();
        
        // 获取像素数组
        int[] sourcePixels = new int[width * height];
        source.getPixels(sourcePixels, 0, width, 0, 0, width, height);
        
        int lutWidth = lutBitmap.getWidth();
        int lutHeight = lutBitmap.getHeight();
        int blockSize = lutWidth / 8;  // 8x8排列的色块
        
        // 缓存LUT像素数组
        int[] lutPixels = new int[lutWidth * lutHeight];
        lutBitmap.getPixels(lutPixels, 0, lutWidth, 0, 0, lutWidth, lutHeight);
        
        // 处理每个像素
        for (int i = 0; i < sourcePixels.length; i++) {
            int pixel = sourcePixels[i];
            
            int alpha = (pixel >> 24) & 0xFF;
            int red = (pixel >> 16) & 0xFF;
            int green = (pixel >> 8) & 0xFF;
            int blue = pixel & 0xFF;
            
            // 查找LUT颜色
            int blueIndex = (blue * (LUT_SIZE - 1)) / 255;
            int redIndex = (red * (LUT_SIZE - 1)) / 255;
            int greenIndex = (green * (LUT_SIZE - 1)) / 255;
            
            // 计算在LUT图片中的位置
            int row = blueIndex / 8;
            int col = blueIndex % 8;
            
            int lutX = col * blockSize + redIndex;
            int lutY = row * blockSize + greenIndex;
            
            // 边界检查
            lutX = Math.max(0, Math.min(lutX, lutWidth - 1));
            lutY = Math.max(0, Math.min(lutY, lutHeight - 1));
            
            int lutPixel = lutPixels[lutY * lutWidth + lutX];
            
            int lutR = (lutPixel >> 16) & 0xFF;
            int lutG = (lutPixel >> 8) & 0xFF;
            int lutB = lutPixel & 0xFF;
            
            // 根据强度混合原色和LUT色
            int finalR = (int)(red * (1 - intensity) + lutR * intensity);
            int finalG = (int)(green * (1 - intensity) + lutG * intensity);
            int finalB = (int)(blue * (1 - intensity) + lutB * intensity);
            
            sourcePixels[i] = (alpha << 24) | (finalR << 16) | (finalG << 8) | finalB;
        }
        
        // 创建输出位图
        Bitmap output = Bitmap.createBitmap(width, height, 
            source.getConfig() != null ? source.getConfig() : Bitmap.Config.ARGB_8888);
        output.setPixels(sourcePixels, 0, width, 0, 0, width, height);
        
        return output;
    }
}

