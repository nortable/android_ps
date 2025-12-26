package com.example.photoshop_demo.collage;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import java.util.List;

/**
 * 拼图合成引擎
 */
public class CollageEngine {
    
    /**
     * 创建拼图
     * @param images 图片列表
     * @param template 布局模板
     * @param outputWidth 输出宽度
     * @param outputHeight 输出高度
     * @param spacing 图片间距（像素）
     * @param backgroundColor 背景色
     * @return 合成后的拼图
     */
    public static Bitmap createCollage(
            List<Bitmap> images,
            CollageTemplate template,
            int outputWidth,
            int outputHeight,
            int spacing,
            int backgroundColor) {
        
        // 1. 创建画布
        Bitmap result = Bitmap.createBitmap(
            outputWidth, outputHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        
        // 2. 绘制背景色
        canvas.drawColor(backgroundColor);
        
        // 3. 绘制每张图片
        List<RectF> frames = template.getFrames();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setFilterBitmap(true);  // 启用图片过滤，提高质量
        
        for (int i = 0; i < images.size() && i < frames.size(); i++) {
            Bitmap image = images.get(i);
            if (image == null) continue;
            
            RectF frame = frames.get(i);
            
            // 计算实际像素位置（考虑间距）
            float left = frame.left * outputWidth + spacing / 2f;
            float top = frame.top * outputHeight + spacing / 2f;
            float right = frame.right * outputWidth - spacing / 2f;
            float bottom = frame.bottom * outputHeight - spacing / 2f;
            
            // 确保不超出边界
            left = Math.max(0, left);
            top = Math.max(0, top);
            right = Math.min(outputWidth, right);
            bottom = Math.min(outputHeight, bottom);
            
            // 计算目标区域
            RectF destRect = new RectF(left, top, right, bottom);
            
            // 计算源区域（裁切中心部分）
            Rect srcRect = calculateCenterCrop(image, destRect);
            
            // 绘制图片
            canvas.drawBitmap(image, srcRect, destRect, paint);
        }
        
        return result;
    }
    
    /**
     * 计算中心裁切区域
     * 保持图片比例，裁切中心部分以填充目标区域
     */
    private static Rect calculateCenterCrop(Bitmap image, RectF destRect) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        
        float destWidth = destRect.width();
        float destHeight = destRect.height();
        
        // 计算缩放比例（取较大值以填充目标区域）
        float scaleX = imageWidth / destWidth;
        float scaleY = imageHeight / destHeight;
        float scale = Math.min(scaleX, scaleY);
        
        // 计算裁切后的尺寸
        int cropWidth = (int)(destWidth * scale);
        int cropHeight = (int)(destHeight * scale);
        
        // 计算中心裁切的起始位置
        int left = (imageWidth - cropWidth) / 2;
        int top = (imageHeight - cropHeight) / 2;
        int right = left + cropWidth;
        int bottom = top + cropHeight;
        
        // 确保不超出图片边界
        left = Math.max(0, left);
        top = Math.max(0, top);
        right = Math.min(imageWidth, right);
        bottom = Math.min(imageHeight, bottom);
        
        return new Rect(left, top, right, bottom);
    }
    
    /**
     * 创建简单的预览图（快速版本，用于模板选择预览）
     */
    public static Bitmap createPreview(
            List<Bitmap> images,
            CollageTemplate template,
            int size) {
        return createCollage(images, template, size, size, 2, 0xFFFFFFFF);
    }
}

