package com.example.photoshop_demo.filter;

import android.graphics.Bitmap;

/**
 * LUT滤镜模型
 */
public class LutFilter {
    private String id;
    private String name;
    private String lutFile;      // assets中的LUT文件路径
    private String category;
    private int defaultIntensity; // 默认强度 0-100
    
    private Bitmap lutBitmap;    // LUT位图缓存
    
    public LutFilter(String id, String name, String lutFile, 
                     String category, int defaultIntensity) {
        this.id = id;
        this.name = name;
        this.lutFile = lutFile;
        this.category = category;
        this.defaultIntensity = defaultIntensity;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getLutFile() {
        return lutFile;
    }
    
    public void setLutFile(String lutFile) {
        this.lutFile = lutFile;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public int getDefaultIntensity() {
        return defaultIntensity;
    }
    
    public void setDefaultIntensity(int defaultIntensity) {
        this.defaultIntensity = defaultIntensity;
    }
    
    public Bitmap getLutBitmap() {
        return lutBitmap;
    }
    
    public void setLutBitmap(Bitmap lutBitmap) {
        this.lutBitmap = lutBitmap;
    }
}

