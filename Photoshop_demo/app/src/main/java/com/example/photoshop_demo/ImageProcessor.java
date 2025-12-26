package com.example.photoshop_demo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * 图片处理工具类
 * 集中管理所有图片编辑算法
 */
public class ImageProcessor {
    
    /**
     * 裁切图片
     * @param source 原始图片
     * @param x 起始X坐标
     * @param y 起始Y坐标
     * @param width 裁切宽度
     * @param height 裁切高度
     * @return 裁切后的图片
     */
    public static Bitmap crop(Bitmap source, int x, int y, int width, int height) {
        // 参数验证
        x = Math.max(0, x);
        y = Math.max(0, y);
        width = Math.min(width, source.getWidth() - x);
        height = Math.min(height, source.getHeight() - y);
        
        if (width <= 0 || height <= 0) {
            return source;
        }
        
        return Bitmap.createBitmap(source, x, y, width, height);
    }
    
    /**
     * 调整亮度
     * @param source 原始图片
     * @param brightness 亮度值 (-255 到 +255)
     * @return 调整后的图片
     */
    public static Bitmap adjustBrightness(Bitmap source, int brightness) {
        ColorMatrix cm = new ColorMatrix();
        cm.set(new float[] {
            1, 0, 0, 0, brightness,
            0, 1, 0, 0, brightness,
            0, 0, 1, 0, brightness,
            0, 0, 0, 1, 0
        });
        return applyColorMatrix(source, cm);
    }
    
    /**
     * 调整对比度
     * @param source 原始图片
     * @param contrast 对比度 (0.0 - 2.0)
     *                 1.0 = 原始
     *                 < 1.0 = 减弱对比
     *                 > 1.0 = 增强对比
     * @return 调整后的图片
     */
    public static Bitmap adjustContrast(Bitmap source, float contrast) {
        float offset = (1.0f - contrast) * 128;
        ColorMatrix cm = new ColorMatrix();
        cm.set(new float[] {
            contrast, 0, 0, 0, offset,
            0, contrast, 0, 0, offset,
            0, 0, contrast, 0, offset,
            0, 0, 0, 1, 0
        });
        return applyColorMatrix(source, cm);
    }
    
    /**
     * 调整饱和度
     * @param source 原始图片
     * @param saturation 饱和度 (0.0 - 2.0)
     *                   0 = 黑白
     *                   1 = 原始
     *                   2 = 超饱和
     * @return 调整后的图片
     */
    public static Bitmap adjustSaturation(Bitmap source, float saturation) {
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(saturation);
        return applyColorMatrix(source, cm);
    }
    
    /**
     * 组合调整（亮度+对比度+饱和度）
     * 优化：一次处理，避免多次创建Bitmap
     * @param source 原始图片
     * @param brightness 亮度值 (-255 到 +255)
     * @param contrast 对比度 (0.0 - 2.0)
     * @param saturation 饱和度 (0.0 - 2.0)
     * @return 调整后的图片
     */
    public static Bitmap adjustAll(Bitmap source, int brightness, 
                                    float contrast, float saturation) {
        ColorMatrix cmBrightness = new ColorMatrix();
        cmBrightness.set(new float[] {
            1, 0, 0, 0, brightness,
            0, 1, 0, 0, brightness,
            0, 0, 1, 0, brightness,
            0, 0, 0, 1, 0
        });
        
        float offset = (1.0f - contrast) * 128;
        ColorMatrix cmContrast = new ColorMatrix();
        cmContrast.set(new float[] {
            contrast, 0, 0, 0, offset,
            0, contrast, 0, 0, offset,
            0, 0, contrast, 0, offset,
            0, 0, 0, 1, 0
        });
        
        ColorMatrix cmSaturation = new ColorMatrix();
        cmSaturation.setSaturation(saturation);
        
        // 组合所有矩阵
        ColorMatrix combined = new ColorMatrix();
        combined.postConcat(cmBrightness);
        combined.postConcat(cmContrast);
        combined.postConcat(cmSaturation);
        
        return applyColorMatrix(source, combined);
    }
    
    /**
     * 应用ColorMatrix到图片
     * @param source 原始图片
     * @param cm ColorMatrix
     * @return 处理后的图片
     */
    private static Bitmap applyColorMatrix(Bitmap source, ColorMatrix cm) {
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        
        Bitmap result = Bitmap.createBitmap(
            source.getWidth(), 
            source.getHeight(), 
            source.getConfig() != null ? source.getConfig() : Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(source, 0, 0, paint);
        
        return result;
    }
    
    /**
     * 90度旋转
     * @param source 原始图片
     * @return 旋转后的图片
     */
    public static Bitmap rotate90(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(source, 0, 0, 
            source.getWidth(), source.getHeight(), matrix, true);
    }
    
    /**
     * 180度旋转
     * @param source 原始图片
     * @return 旋转后的图片
     */
    public static Bitmap rotate180(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.postRotate(180);
        return Bitmap.createBitmap(source, 0, 0, 
            source.getWidth(), source.getHeight(), matrix, true);
    }
    
    /**
     * 270度旋转（逆时针90度）
     * @param source 原始图片
     * @return 旋转后的图片
     */
    public static Bitmap rotate270(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.postRotate(270);
        return Bitmap.createBitmap(source, 0, 0, 
            source.getWidth(), source.getHeight(), matrix, true);
    }
    
    /**
     * 水平翻转
     * @param source 原始图片
     * @return 翻转后的图片
     */
    public static Bitmap flipHorizontal(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1, source.getWidth() / 2f, source.getHeight() / 2f);
        return Bitmap.createBitmap(source, 0, 0, 
            source.getWidth(), source.getHeight(), matrix, true);
    }
    
    /**
     * 垂直翻转
     * @param source 原始图片
     * @return 翻转后的图片
     */
    public static Bitmap flipVertical(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.postScale(1, -1, source.getWidth() / 2f, source.getHeight() / 2f);
        return Bitmap.createBitmap(source, 0, 0, 
            source.getWidth(), source.getHeight(), matrix, true);
    }
    
    /**
     * 限制值在0-255范围内
     */
    private static int clamp(int value) {
        if (value < 0) return 0;
        if (value > 255) return 255;
        return value;
    }
    
    /**
     * 限制浮点值在指定范围内
     */
    private static float clampFloat(float value, float min, float max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}

