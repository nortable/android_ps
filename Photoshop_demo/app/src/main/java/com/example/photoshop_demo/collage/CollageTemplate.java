package com.example.photoshop_demo.collage;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 拼图布局模板
 */
public class CollageTemplate {
    private int imageCount;          // 图片数量
    private String name;              // 模板名称
    private List<RectF> frames;       // 每个图片的位置和大小（比例值 0-1）
    
    public CollageTemplate(int imageCount, String name, List<RectF> frames) {
        this.imageCount = imageCount;
        this.name = name;
        this.frames = frames;
    }
    
    public int getImageCount() {
        return imageCount;
    }
    
    public String getName() {
        return name;
    }
    
    public List<RectF> getFrames() {
        return frames;
    }
    
    // ==================== 2张图片模板 ====================
    
    /**
     * 2张 - 左右平分
     */
    public static CollageTemplate getTwoHorizontal() {
        return new CollageTemplate(2, "左右", Arrays.asList(
            new RectF(0, 0, 0.5f, 1.0f),      // 左半边
            new RectF(0.5f, 0, 1.0f, 1.0f)    // 右半边
        ));
    }
    
    /**
     * 2张 - 上下平分
     */
    public static CollageTemplate getTwoVertical() {
        return new CollageTemplate(2, "上下", Arrays.asList(
            new RectF(0, 0, 1.0f, 0.5f),      // 上半边
            new RectF(0, 0.5f, 1.0f, 1.0f)    // 下半边
        ));
    }
    
    // ==================== 3张图片模板 ====================
    
    /**
     * 3张 - 左1右2
     */
    public static CollageTemplate getThreeLeft() {
        return new CollageTemplate(3, "左1右2", Arrays.asList(
            new RectF(0, 0, 0.5f, 1.0f),      // 左边大图
            new RectF(0.5f, 0, 1.0f, 0.5f),   // 右上
            new RectF(0.5f, 0.5f, 1.0f, 1.0f) // 右下
        ));
    }
    
    /**
     * 3张 - 右1左2
     */
    public static CollageTemplate getThreeRight() {
        return new CollageTemplate(3, "右1左2", Arrays.asList(
            new RectF(0, 0, 0.5f, 0.5f),      // 左上
            new RectF(0, 0.5f, 0.5f, 1.0f),   // 左下
            new RectF(0.5f, 0, 1.0f, 1.0f)    // 右边大图
        ));
    }
    
    /**
     * 3张 - 上1下2
     */
    public static CollageTemplate getThreeTop() {
        return new CollageTemplate(3, "上1下2", Arrays.asList(
            new RectF(0, 0, 1.0f, 0.5f),      // 上边大图
            new RectF(0, 0.5f, 0.5f, 1.0f),   // 左下
            new RectF(0.5f, 0.5f, 1.0f, 1.0f) // 右下
        ));
    }
    
    // ==================== 4张图片模板 ====================
    
    /**
     * 4张 - 田字格
     */
    public static CollageTemplate getFourGrid() {
        return new CollageTemplate(4, "田字格", Arrays.asList(
            new RectF(0, 0, 0.5f, 0.5f),      // 左上
            new RectF(0.5f, 0, 1.0f, 0.5f),   // 右上
            new RectF(0, 0.5f, 0.5f, 1.0f),   // 左下
            new RectF(0.5f, 0.5f, 1.0f, 1.0f) // 右下
        ));
    }
    
    /**
     * 4张 - 上1下3
     */
    public static CollageTemplate getFourTopOne() {
        return new CollageTemplate(4, "上1下3", Arrays.asList(
            new RectF(0, 0, 1.0f, 0.5f),           // 上边大图
            new RectF(0, 0.5f, 0.333f, 1.0f),      // 左下
            new RectF(0.333f, 0.5f, 0.666f, 1.0f), // 中下
            new RectF(0.666f, 0.5f, 1.0f, 1.0f)    // 右下
        ));
    }
    
    // ==================== 获取所有模板 ====================
    
    /**
     * 根据图片数量获取所有可用模板
     */
    public static List<CollageTemplate> getTemplatesForCount(int count) {
        List<CollageTemplate> templates = new ArrayList<>();
        
        switch (count) {
            case 2:
                templates.add(getTwoHorizontal());
                templates.add(getTwoVertical());
                break;
            case 3:
                templates.add(getThreeLeft());
                templates.add(getThreeRight());
                templates.add(getThreeTop());
                break;
            case 4:
                templates.add(getFourGrid());
                templates.add(getFourTopOne());
                break;
            default:
                // 默认使用田字格式平铺
                templates.add(generateGridTemplate(count));
                break;
        }
        
        return templates;
    }
    
    /**
     * 生成网格模板（用于5-9张图片）
     */
    private static CollageTemplate generateGridTemplate(int count) {
        List<RectF> frames = new ArrayList<>();
        
        // 计算行列数
        int cols = (int)Math.ceil(Math.sqrt(count));
        int rows = (int)Math.ceil((double)count / cols);
        
        float cellWidth = 1.0f / cols;
        float cellHeight = 1.0f / rows;
        
        for (int i = 0; i < count; i++) {
            int row = i / cols;
            int col = i % cols;
            
            float left = col * cellWidth;
            float top = row * cellHeight;
            float right = left + cellWidth;
            float bottom = top + cellHeight;
            
            frames.add(new RectF(left, top, right, bottom));
        }
        
        return new CollageTemplate(count, "网格", frames);
    }
}

