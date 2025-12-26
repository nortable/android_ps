package com.example.photoshop_demo.filter;

import android.content.Context;
import android.graphics.Bitmap;
import java.util.ArrayList;
import java.util.List;

/**
 * LUT滤镜管理器
 */
public class LutFilterManager {
    private static LutFilterManager instance;
    private List<LutFilter> filters;
    private Context context;
    
    private LutFilterManager(Context context) {
        this.context = context.getApplicationContext();
        this.filters = new ArrayList<>();
        initFilters();
    }
    
    public static LutFilterManager getInstance(Context context) {
        if (instance == null) {
            instance = new LutFilterManager(context);
        }
        return instance;
    }
    
    /**
     * 初始化所有LUT滤镜
     */
    private void initFilters() {
        filters.add(new LutFilter(
            "identity", "原图", "luts/identity.png", 
            "basic", 100));
            
        filters.add(new LutFilter(
            "grayscale", "黑白", "luts/grayscale.png",
            "basic", 80));
            
        filters.add(new LutFilter(
            "warm", "暖阳", "luts/warm.png",
            "color", 75));
            
        filters.add(new LutFilter(
            "cool", "冷峻", "luts/cool.png",
            "color", 75));
            
        filters.add(new LutFilter(
            "vintage", "怀旧", "luts/vintage.png",
            "artistic", 70));
            
        filters.add(new LutFilter(
            "vivid", "鲜艳", "luts/vivid.png",
            "artistic", 80));
            
        filters.add(new LutFilter(
            "romantic", "浪漫", "luts/romantic.png",
            "artistic", 75));
            
        filters.add(new LutFilter(
            "cinematic", "电影", "luts/cinematic.png",
            "professional", 85));
    }
    
    /**
     * 获取所有滤镜
     */
    public List<LutFilter> getAllFilters() {
        return filters;
    }
    
    /**
     * 根据ID获取滤镜
     */
    public LutFilter getFilterById(String id) {
        for (LutFilter filter : filters) {
            if (filter.getId().equals(id)) {
                return filter;
            }
        }
        return null;
    }
    
    /**
     * 预加载LUT位图（优化性能）
     */
    public void preloadLuts() {
        new Thread(() -> {
            for (LutFilter filter : filters) {
                if (filter.getLutBitmap() == null) {
                    Bitmap lut = LutProcessor.loadLutFromAssets(
                        context, filter.getLutFile());
                    filter.setLutBitmap(lut);
                }
            }
        }).start();
    }
    
    /**
     * 应用滤镜
     */
    public Bitmap applyFilter(Bitmap source, LutFilter filter, float intensity) {
        // 原图滤镜直接返回
        if (filter.getId().equals("identity")) {
            return source;
        }
        
        // 确保LUT已加载
        if (filter.getLutBitmap() == null) {
            Bitmap lut = LutProcessor.loadLutFromAssets(context, filter.getLutFile());
            filter.setLutBitmap(lut);
        }
        
        return LutProcessor.applyLut(source, filter.getLutBitmap(), intensity);
    }
    
    /**
     * 释放所有LUT资源
     */
    public void releaseResources() {
        for (LutFilter filter : filters) {
            if (filter.getLutBitmap() != null && !filter.getLutBitmap().isRecycled()) {
                filter.getLutBitmap().recycle();
                filter.setLutBitmap(null);
            }
        }
    }
}

