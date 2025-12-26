# 美化功能实现指南

## 📋 目录

1. [功能概述](#功能概述)
2. [自动增强](#1-自动增强)
3. [锐化](#2-锐化)
4. [去雾](#3-去雾)
5. [暗角](#4-暗角)
6. [光晕](#5-光晕)
7. [柔焦](#6-柔焦)
8. [UI设计](#ui设计)
9. [实现架构](#实现架构)
10. [性能优化](#性能优化)

---

## 功能概述

### 美化 vs 调整 vs 滤镜

| 类型 | 特点 | 示例 |
|------|------|------|
| **调整** | 基础参数调节，精确控制 | 亮度、对比度、饱和度 |
| **滤镜** | 色彩风格转换，艺术效果 | 黑白、怀旧、电影感 |
| **美化** | 智能优化，一键增强 | 自动增强、锐化、去雾 |

### 6个核心功能

```
美化面板
├── ⚡ 自动增强 - 智能分析，一键优化
├── 🔍 锐化     - 增强细节和边缘
├── 🌫️ 去雾     - 提高清晰度和通透感
├── 🎭 暗角     - 四周变暗，突出主体
├── ✨ 光晕     - 中心发光，梦幻效果
└── 🌸 柔焦     - 背景虚化，突出主体
```

---

## 1. 自动增强

### 📖 功能说明

**作用**：一键智能优化图片，自动调整亮度、对比度、饱和度，让照片更通透明亮。

**使用场景**：
- 光线不足的照片
- 对比度偏低的照片
- 色彩不够鲜艳的照片
- 不知道如何调整的情况

**效果**：
- 暗部提亮 5-15%
- 高光保留（避免过曝）
- 对比度增强 10-20%
- 饱和度提升 5-10%

### 🔬 算法原理

#### 直方图分析法

```
1. 分析图片直方图
   ├─ 计算平均亮度
   ├─ 检测亮度分布
   └─ 计算对比度范围

2. 制定优化策略
   ├─ 过暗：增加亮度 + 提升阴影
   ├─ 过亮：降低高光
   ├─ 对比度低：拉伸直方图
   └─ 饱和度低：适当增强

3. 应用调整
   └─ 使用ColorMatrix组合变换
```

#### 核心算法

```java
public class AutoEnhance {
    
    /**
     * 分析图片并计算优化参数
     */
    public static EnhanceParams analyzeImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        
        // 计算亮度直方图
        int[] histogram = new int[256];
        float totalBrightness = 0;
        
        for (int pixel : pixels) {
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            
            // 计算亮度（感知亮度）
            int brightness = (int)(0.299 * r + 0.587 * g + 0.114 * b);
            histogram[brightness]++;
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
     * 应用自动增强
     */
    public static Bitmap apply(Bitmap source, float intensity) {
        EnhanceParams params = analyzeImage(source);
        
        // 根据强度调整参数
        int brightness = (int)(params.brightness * intensity);
        float contrast = 1.0f + (params.contrast - 1.0f) * intensity;
        float saturation = 1.0f + (params.saturation - 1.0f) * intensity;
        
        // 应用调整（使用已有的ImageProcessor）
        return ImageProcessor.adjustAll(source, brightness, contrast, saturation);
    }
    
    static class EnhanceParams {
        int brightness = 0;
        float contrast = 1.0f;
        float saturation = 1.0f;
    }
}
```

### 💡 实现要点

1. **分析要准确**：正确计算平均亮度和对比度
2. **调整要适度**：避免过度增强（参数限制在合理范围）
3. **强度可调**：用户可以控制增强程度（0-100%）
4. **性能优化**：分析可以在后台线程进行

### 🎯 参数范围

| 参数 | 调整范围 | 说明 |
|------|---------|------|
| 亮度 | -20 ~ +20 | 根据原图亮度智能调整 |
| 对比度 | 1.0 ~ 1.3 | 适度增强，避免过曝 |
| 饱和度 | 1.0 ~ 1.2 | 轻微提升，保持自然 |

---

## 2. 锐化

### 📖 功能说明

**作用**：增强图片细节和边缘，让照片更清晰。

**使用场景**：
- 轻微失焦的照片
- 需要强调细节的照片
- 放大后显示模糊的照片

**效果**：
- 边缘更清晰
- 细节更突出
- 整体更锐利

**注意**：过度锐化会产生光晕和噪点。

### 🔬 算法原理

#### 卷积锐化法

使用卷积核（Convolution Kernel）增强边缘。

```
锐化核心原理：
原图 - 模糊图 = 边缘信息
原图 + 边缘信息 = 锐化图
```

#### 常用锐化核

```java
// 1. 基础锐化核（3x3）
float[] sharpenKernel = {
    0, -1,  0,
   -1,  5, -1,
    0, -1,  0
};

// 2. 强锐化核
float[] strongSharpenKernel = {
   -1, -1, -1,
   -1,  9, -1,
   -1, -1, -1
};

// 3. 高斯锐化核（5x5，更平滑）
float[] gaussianSharpenKernel = {
   -1, -1, -1, -1, -1,
   -1,  2,  2,  2, -1,
   -1,  2,  8,  2, -1,
   -1,  2,  2,  2, -1,
   -1, -1, -1, -1, -1
};
```

#### 实现代码

```java
public class SharpenFilter {
    
    /**
     * 应用锐化效果
     * @param source 原始图片
     * @param intensity 强度 0.0-1.0
     * @return 锐化后的图片
     */
    public static Bitmap apply(Bitmap source, float intensity) {
        int width = source.getWidth();
        int height = source.getHeight();
        
        int[] srcPixels = new int[width * height];
        source.getPixels(srcPixels, 0, width, 0, 0, width, height);
        
        int[] dstPixels = new int[width * height];
        
        // 根据强度选择锐化核
        float[] kernel;
        if (intensity < 0.5f) {
            // 弱锐化
            kernel = new float[] {
                0, -1,  0,
               -1,  5, -1,
                0, -1,  0
            };
        } else {
            // 强锐化
            kernel = new float[] {
               -1, -1, -1,
               -1,  9, -1,
               -1, -1, -1
            };
        }
        
        int kernelSize = (int)Math.sqrt(kernel.length);
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
                
                // 根据强度混合
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
        
        Bitmap result = Bitmap.createBitmap(width, height, source.getConfig());
        result.setPixels(dstPixels, 0, width, 0, 0, width, height);
        return result;
    }
}
```

### 💡 实现要点

1. **边界处理**：卷积时注意图片边缘
2. **强度控制**：原图和锐化图混合
3. **性能优化**：使用RenderScript加速
4. **避免过度**：强度不宜超过80%

### 🎯 参数说明

| 强度 | 效果 | 适用场景 |
|------|------|---------|
| 0-30% | 轻微锐化 | 人像，柔和场景 |
| 30-60% | 适度锐化 | 风景，建筑 |
| 60-100% | 强烈锐化 | 文字，产品 |

---

## 3. 去雾

### 📖 功能说明

**作用**：去除照片中的雾气和朦胧感，提高清晰度和通透感。

**使用场景**：
- 雾天拍摄的照片
- 远景不清晰的照片
- 整体发灰的照片
- 需要提高通透感的照片

**效果**：
- 提高对比度
- 增强色彩饱和度
- 恢复远景细节
- 整体更通透

### 🔬 算法原理

#### 暗通道先验算法（简化版）

**何恺明的暗通道先验理论**：
- 无雾图像的暗通道接近0
- 有雾图像的暗通道值较大
- 通过暗通道估算透射率，恢复原图

**简化实现**（不计算透射图）：
```
1. 增强对比度（拉伸直方图）
2. 提升饱和度（恢复色彩）
3. 适度增加亮度（补偿暗部）
4. 锐化细节（增强清晰度）
```

#### 实现代码

```java
public class DehazeFilter {
    
    /**
     * 应用去雾效果
     * @param source 原始图片
     * @param intensity 强度 0.0-1.0
     * @return 去雾后的图片
     */
    public static Bitmap apply(Bitmap source, float intensity) {
        // 方案1：简化版（快速）
        return applySimple(source, intensity);
        
        // 方案2：暗通道版（高质量，较慢）
        // return applyDarkChannel(source, intensity);
    }
    
    /**
     * 简化版去雾（推荐）
     */
    private static Bitmap applySimple(Bitmap source, float intensity) {
        int width = source.getWidth();
        int height = source.getHeight();
        
        int[] pixels = new int[width * height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);
        
        // 1. 计算直方图
        int[] histR = new int[256];
        int[] histG = new int[256];
        int[] histB = new int[256];
        
        for (int pixel : pixels) {
            histR[(pixel >> 16) & 0xFF]++;
            histG[(pixel >> 8) & 0xFF]++;
            histB[pixel & 0xFF]++;
        }
        
        // 2. 计算累积直方图（用于直方图均衡化）
        int[] cumHistR = new int[256];
        int[] cumHistG = new int[256];
        int[] cumHistB = new int[256];
        
        cumHistR[0] = histR[0];
        cumHistG[0] = histG[0];
        cumHistB[0] = histB[0];
        
        for (int i = 1; i < 256; i++) {
            cumHistR[i] = cumHistR[i-1] + histR[i];
            cumHistG[i] = cumHistG[i-1] + histG[i];
            cumHistB[i] = cumHistB[i-1] + histB[i];
        }
        
        // 3. 构建映射表（对比度拉伸）
        int totalPixels = width * height;
        int[] mapR = new int[256];
        int[] mapG = new int[256];
        int[] mapB = new int[256];
        
        float stretchFactor = 1.0f + intensity * 0.5f;
        
        for (int i = 0; i < 256; i++) {
            // 直方图均衡化
            mapR[i] = (int)(255.0f * cumHistR[i] / totalPixels);
            mapG[i] = (int)(255.0f * cumHistG[i] / totalPixels);
            mapB[i] = (int)(255.0f * cumHistB[i] / totalPixels);
            
            // 应用拉伸
            mapR[i] = (int)Math.min(255, (mapR[i] - 128) * stretchFactor + 128);
            mapG[i] = (int)Math.min(255, (mapG[i] - 128) * stretchFactor + 128);
            mapB[i] = (int)Math.min(255, (mapB[i] - 128) * stretchFactor + 128);
        }
        
        // 4. 应用映射
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            
            r = mapR[r];
            g = mapG[g];
            b = mapB[b];
            
            // 增强饱和度
            float[] hsv = new float[3];
            android.graphics.Color.RGBToHSV(r, g, b, hsv);
            hsv[1] = Math.min(1.0f, hsv[1] * (1.0f + intensity * 0.3f));
            int color = android.graphics.Color.HSVToColor(hsv);
            
            r = (color >> 16) & 0xFF;
            g = (color >> 8) & 0xFF;
            b = color & 0xFF;
            
            // 根据强度混合
            int origR = (pixel >> 16) & 0xFF;
            int origG = (pixel >> 8) & 0xFF;
            int origB = pixel & 0xFF;
            
            r = (int)(origR * (1 - intensity) + r * intensity);
            g = (int)(origG * (1 - intensity) + g * intensity);
            b = (int)(origB * (1 - intensity) + b * intensity);
            
            pixels[i] = (0xFF << 24) | (r << 16) | (g << 8) | b;
        }
        
        Bitmap result = Bitmap.createBitmap(width, height, source.getConfig());
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }
}
```

### 💡 实现要点

1. **直方图均衡化**：增强对比度
2. **适度增强**：避免过度处理导致失真
3. **保留细节**：不要丢失暗部和高光信息
4. **色彩恢复**：提升饱和度恢复原有色彩

### 🎯 效果对比

| 强度 | 对比度 | 饱和度 | 适用场景 |
|------|--------|--------|---------|
| 0-30% | +10% | +5% | 轻雾 |
| 30-60% | +20% | +10% | 中雾 |
| 60-100% | +30% | +15% | 重雾 |

---

## 4. 暗角

### 📖 功能说明

**作用**：使图片四周逐渐变暗，突出中心主体，营造氛围感。

**使用场景**：
- 人像摄影（突出人物）
- 产品摄影（聚焦产品）
- 艺术效果（复古感、电影感）
- 去除杂乱背景的视觉干扰

**效果**：
- 四周自然渐暗
- 中心保持原亮度
- 增加照片层次感
- 营造专业摄影感

### 🔬 算法原理

#### 径向渐变遮罩

```
暗角原理：
1. 以图片中心为原点
2. 计算每个像素到中心的距离
3. 距离越远，降低亮度越多
4. 使用平滑的衰减函数
```

#### 衰减函数

```java
// 1. 线性衰减
brightness = 1.0f - (distance / maxDistance) * intensity;

// 2. 二次衰减（推荐，更自然）
brightness = 1.0f - Math.pow(distance / maxDistance, 2) * intensity;

// 3. 指数衰减
brightness = (float)Math.exp(-distance / maxDistance * intensity * 3);
```

#### 实现代码

```java
public class VignetteFilter {
    
    /**
     * 应用暗角效果
     * @param source 原始图片
     * @param intensity 强度 0.0-1.0
     * @param radius 暗角半径 0.5-1.5（相对于图片尺寸）
     * @return 添加暗角后的图片
     */
    public static Bitmap apply(Bitmap source, float intensity, float radius) {
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
                    // 使用二次衰减
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
        
        Bitmap result = Bitmap.createBitmap(width, height, source.getConfig());
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }
    
    /**
     * 默认半径版本
     */
    public static Bitmap apply(Bitmap source, float intensity) {
        return apply(source, intensity, 0.7f);
    }
}
```

### 💡 实现要点

1. **自然过渡**：使用二次或指数衰减
2. **保留细节**：最暗处不要完全变黑
3. **可调半径**：支持调整暗角范围
4. **性能优化**：可预计算距离图

### 🎯 参数说明

| 参数 | 范围 | 说明 |
|------|------|------|
| 强度 | 0-100% | 暗角深度 |
| 半径 | 0.5-1.5 | 暗角扩散范围 |

**推荐设置**：
- 人像：强度60%，半径0.7
- 风景：强度40%，半径0.8
- 产品：强度70%，半径0.6

---

## 5. 光晕

### 📖 功能说明

**作用**：在图片中心添加柔和的发光效果，营造梦幻、温暖的氛围。

**使用场景**：
- 浪漫氛围照片
- 逆光效果模拟
- 梦幻风格
- 柔和人像

**效果**：
- 中心区域提亮
- 光线向外扩散
- 整体柔和梦幻
- 类似镜头光晕

### 🔬 算法原理

#### 高斯模糊 + 加法混合

```
光晕原理：
1. 提取图片高光部分
2. 对高光进行高斯模糊
3. 叠加回原图（加法混合）
4. 中心区域额外提亮
```

#### 实现步骤

```
原图 → 提取高光 → 高斯模糊 → 叠加 → 光晕效果
              ↓
         中心提亮遮罩
```

#### 实现代码

```java
public class GlowFilter {
    
    /**
     * 应用光晕效果
     * @param source 原始图片
     * @param intensity 强度 0.0-1.0
     * @return 添加光晕后的图片
     */
    public static Bitmap apply(Bitmap source, float intensity) {
        int width = source.getWidth();
        int height = source.getHeight();
        
        // 1. 创建中心提亮遮罩
        float[] mask = createGlowMask(width, height);
        
        // 2. 提取并模糊高光
        Bitmap blurred = extractAndBlurHighlights(source, intensity);
        
        // 3. 混合
        int[] srcPixels = new int[width * height];
        int[] blurPixels = new int[width * height];
        source.getPixels(srcPixels, 0, width, 0, 0, width, height);
        blurred.getPixels(blurPixels, 0, width, 0, 0, width, height);
        
        int[] resultPixels = new int[width * height];
        
        for (int i = 0; i < srcPixels.length; i++) {
            int srcPixel = srcPixels[i];
            int blurPixel = blurPixels[i];
            
            int srcR = (srcPixel >> 16) & 0xFF;
            int srcG = (srcPixel >> 8) & 0xFF;
            int srcB = srcPixel & 0xFF;
            
            int blurR = (blurPixel >> 16) & 0xFF;
            int blurG = (blurPixel >> 8) & 0xFF;
            int blurB = blurPixel & 0xFF;
            
            // 加法混合（叠加光晕）
            float maskValue = mask[i] * intensity;
            int r = (int)Math.min(255, srcR + blurR * maskValue);
            int g = (int)Math.min(255, srcG + blurG * maskValue);
            int b = (int)Math.min(255, srcB + blurB * maskValue);
            
            resultPixels[i] = (0xFF << 24) | (r << 16) | (g << 8) | b;
        }
        
        Bitmap result = Bitmap.createBitmap(width, height, source.getConfig());
        result.setPixels(resultPixels, 0, width, 0, 0, width, height);
        
        blurred.recycle();
        return result;
    }
    
    /**
     * 创建光晕遮罩（中心亮，边缘暗）
     */
    private static float[] createGlowMask(int width, int height) {
        float[] mask = new float[width * height];
        float centerX = width / 2.0f;
        float centerY = height / 2.0f;
        float maxDist = (float)Math.sqrt(centerX * centerX + centerY * centerY);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float dx = x - centerX;
                float dy = y - centerY;
                float dist = (float)Math.sqrt(dx * dx + dy * dy);
                
                // 高斯分布
                mask[y * width + x] = (float)Math.exp(-dist * dist / 
                                                     (2 * maxDist * maxDist * 0.3));
            }
        }
        
        return mask;
    }
    
    /**
     * 提取高光并模糊
     */
    private static Bitmap extractAndBlurHighlights(Bitmap source, float intensity) {
        int width = source.getWidth();
        int height = source.getHeight();
        
        int[] pixels = new int[width * height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);
        
        // 提取高光（亮度 > 180）
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            
            int brightness = (int)(0.299 * r + 0.587 * g + 0.114 * b);
            
            if (brightness > 180) {
                // 保留高光
                float factor = (brightness - 180) / 75.0f;
                r = (int)(r * factor);
                g = (int)(g * factor);
                b = (int)(b * factor);
            } else {
                // 移除非高光
                r = g = b = 0;
            }
            
            pixels[i] = (0xFF << 24) | (r << 16) | (g << 8) | b;
        }
        
        Bitmap highlights = Bitmap.createBitmap(width, height, source.getConfig());
        highlights.setPixels(pixels, 0, width, 0, 0, width, height);
        
        // 高斯模糊
        int blurRadius = (int)(Math.min(width, height) * 0.05f * intensity);
        blurRadius = Math.max(5, Math.min(25, blurRadius));
        
        Bitmap blurred = gaussianBlur(highlights, blurRadius);
        highlights.recycle();
        
        return blurred;
    }
    
    /**
     * 高斯模糊（简化版）
     */
    private static Bitmap gaussianBlur(Bitmap source, int radius) {
        // 可以使用RenderScript加速
        // 这里使用简化的box blur
        return boxBlur(source, radius);
    }
    
    /**
     * Box Blur（快速模糊）
     */
    private static Bitmap boxBlur(Bitmap source, int radius) {
        int width = source.getWidth();
        int height = source.getHeight();
        
        int[] pixels = new int[width * height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);
        
        // 水平模糊
        int[] temp = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = 0, g = 0, b = 0, count = 0;
                
                for (int kx = -radius; kx <= radius; kx++) {
                    int px = Math.max(0, Math.min(width - 1, x + kx));
                    int pixel = pixels[y * width + px];
                    r += (pixel >> 16) & 0xFF;
                    g += (pixel >> 8) & 0xFF;
                    b += pixel & 0xFF;
                    count++;
                }
                
                temp[y * width + x] = (0xFF << 24) | 
                                     ((r / count) << 16) | 
                                     ((g / count) << 8) | 
                                     (b / count);
            }
        }
        
        // 垂直模糊
        int[] result = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = 0, g = 0, b = 0, count = 0;
                
                for (int ky = -radius; ky <= radius; ky++) {
                    int py = Math.max(0, Math.min(height - 1, y + ky));
                    int pixel = temp[py * width + x];
                    r += (pixel >> 16) & 0xFF;
                    g += (pixel >> 8) & 0xFF;
                    b += pixel & 0xFF;
                    count++;
                }
                
                result[y * width + x] = (0xFF << 24) | 
                                       ((r / count) << 16) | 
                                       ((g / count) << 8) | 
                                       (b / count);
            }
        }
        
        Bitmap blurred = Bitmap.createBitmap(width, height, source.getConfig());
        blurred.setPixels(result, 0, width, 0, 0, width, height);
        return blurred;
    }
}
```

### 💡 实现要点

1. **提取高光**：只对明亮区域产生光晕
2. **适度模糊**：模糊半径根据图片尺寸自适应
3. **加法混合**：避免覆盖原图细节
4. **性能优化**：使用RenderScript加速模糊

### 🎯 参数说明

| 参数 | 效果 | 适用场景 |
|------|------|---------|
| 0-30% | 轻微发光 | 日常照片 |
| 30-60% | 明显光晕 | 逆光、梦幻 |
| 60-100% | 强烈光效 | 艺术效果 |

---

## 6. 柔焦

### 📖 功能说明

**作用**：使背景或边缘区域模糊，突出中心主体，模拟大光圈效果。

**使用场景**：
- 人像摄影（突出人物）
- 产品摄影（聚焦产品）
- 模拟景深效果
- 去除背景干扰

**效果**：
- 中心清晰
- 四周逐渐模糊
- 类似大光圈虚化
- 增加空间感

### 🔬 算法原理

#### 径向模糊

```
柔焦原理：
1. 以图片中心为焦点
2. 计算每个像素到中心的距离
3. 距离越远，模糊程度越高
4. 使用可变半径高斯模糊
```

#### 实现代码

```java
public class SoftFocusFilter {
    
    /**
     * 应用柔焦效果
     * @param source 原始图片
     * @param intensity 强度 0.0-1.0
     * @param focusSize 焦点大小 0.3-0.8
     * @return 柔焦后的图片
     */
    public static Bitmap apply(Bitmap source, float intensity, float focusSize) {
        int width = source.getWidth();
        int height = source.getHeight();
        
        // 1. 创建多层模糊图
        Bitmap[] blurLevels = new Bitmap[5];
        blurLevels[0] = source;  // 原图（无模糊）
        
        for (int i = 1; i < 5; i++) {
            int radius = i * 5;  // 5, 10, 15, 20
            blurLevels[i] = gaussianBlur(source, radius);
        }
        
        // 2. 创建模糊遮罩
        float[] blurMask = createBlurMask(width, height, focusSize);
        
        // 3. 混合不同模糊层
        int[] pixels = new int[width * height];
        
        for (int i = 0; i < pixels.length; i++) {
            float blurAmount = blurMask[i] * intensity;
            
            // 选择模糊层
            int level = (int)(blurAmount * 4);
            float blend = (blurAmount * 4) - level;
            
            level = Math.min(3, level);
            
            // 获取两层像素
            int[] pixels1 = new int[1];
            int[] pixels2 = new int[1];
            int x = i % width;
            int y = i / width;
            
            blurLevels[level].getPixels(pixels1, 0, 1, x, y, 1, 1);
            blurLevels[level + 1].getPixels(pixels2, 0, 1, x, y, 1, 1);
            
            // 混合
            int p1 = pixels1[0];
            int p2 = pixels2[0];
            
            int r = (int)(((p1 >> 16) & 0xFF) * (1 - blend) + 
                         ((p2 >> 16) & 0xFF) * blend);
            int g = (int)(((p1 >> 8) & 0xFF) * (1 - blend) + 
                         ((p2 >> 8) & 0xFF) * blend);
            int b = (int)((p1 & 0xFF) * (1 - blend) + 
                         (p2 & 0xFF) * blend);
            
            pixels[i] = (0xFF << 24) | (r << 16) | (g << 8) | b;
        }
        
        // 4. 创建结果
        Bitmap result = Bitmap.createBitmap(width, height, source.getConfig());
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        
        // 5. 释放模糊层
        for (int i = 1; i < 5; i++) {
            blurLevels[i].recycle();
        }
        
        return result;
    }
    
    /**
     * 创建模糊遮罩（中心0，边缘1）
     */
    private static float[] createBlurMask(int width, int height, float focusSize) {
        float[] mask = new float[width * height];
        float centerX = width / 2.0f;
        float centerY = height / 2.0f;
        float maxDist = (float)Math.sqrt(centerX * centerX + centerY * centerY);
        float focusRadius = maxDist * focusSize;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float dx = x - centerX;
                float dy = y - centerY;
                float dist = (float)Math.sqrt(dx * dx + dy * dy);
                
                if (dist <= focusRadius) {
                    mask[y * width + x] = 0;  // 焦点内：清晰
                } else {
                    // 焦点外：根据距离模糊
                    float normalizedDist = (dist - focusRadius) / 
                                          (maxDist - focusRadius);
                    mask[y * width + x] = normalizedDist;
                }
            }
        }
        
        return mask;
    }
    
    /**
     * 高斯模糊（同光晕滤镜）
     */
    private static Bitmap gaussianBlur(Bitmap source, int radius) {
        // 实现见光晕滤镜部分
        return boxBlur(source, radius);
    }
    
    /**
     * 默认焦点大小版本
     */
    public static Bitmap apply(Bitmap source, float intensity) {
        return apply(source, intensity, 0.5f);
    }
}
```

### 💡 实现要点

1. **多层模糊**：预先创建不同模糊程度的图层
2. **平滑过渡**：使用插值混合不同模糊层
3. **可调焦点**：支持调整清晰区域大小
4. **性能优化**：模糊操作使用RenderScript

### 🎯 参数说明

| 参数 | 范围 | 说明 |
|------|------|------|
| 强度 | 0-100% | 模糊程度 |
| 焦点大小 | 0.3-0.8 | 清晰区域大小 |

**推荐设置**：
- 人像：强度70%，焦点0.5
- 产品：强度80%，焦点0.4
- 风景：强度50%，焦点0.6

---

## UI设计

### 布局结构

```xml
┌─────────────────────────────────────┐
│  美化                          ✕   │  ← 标题栏
├─────────────────────────────────────┤
│                                     │
│  [⚡自动增强]  [🔍锐化]  [🌫️去雾]    │  ← 功能按钮（第一行）
│                                     │
│  [🎭暗角]     [✨光晕]  [🌸柔焦]    │  ← 功能按钮（第二行）
│                                     │
├─────────────────────────────────────┤
│ 强度                          80%  │  ← 强度标签
│ ━━━━━━━━━━●━━━━━━━━━━━━━━━━━━━━━  │  ← 滑块
├─────────────────────────────────────┤
│  [ 重置 ]         [ 应用 ]         │  ← 操作按钮
└─────────────────────────────────────┘
```

### 布局文件

```xml
<!-- panel_beautify.xml -->
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/beautify_panel"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="#1A1A1A"
    android:visibility="gone">
    
    <!-- 标题栏 -->
    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="56dp"
        android:paddingHorizontal="16dp">
        
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="美化"
            android:textColor="@color/white"
            android:textSize="18sp"
            android:textStyle="bold"
            android:layout_centerVertical="true"/>
        
        <TextView
            android:id="@+id/btn_beautify_close"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:text="✕"
            android:textColor="@color/white"
            android:textSize="20sp"
            android:gravity="center"
            android:layout_alignParentEnd="true"
            android:layout_centerVertical="true"
            android:clickable="true"
            android:background="?attr/selectableItemBackgroundBorderless"/>
    </RelativeLayout>
    
    <!-- 功能按钮网格 -->
    <GridLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:columnCount="3"
        android:rowCount="2"
        android:padding="16dp">
        
        <!-- 自动增强 -->
        <LinearLayout
            android:id="@+id/btn_auto_enhance"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_columnWeight="1"
            android:orientation="vertical"
            android:gravity="center"
            android:padding="12dp"
            android:clickable="true"
            android:background="@drawable/beautify_button_bg">
            
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="⚡"
                android:textSize="32sp"/>
            
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="自动增强"
                android:textColor="@color/white"
                android:textSize="12sp"
                android:layout_marginTop="4dp"/>
        </LinearLayout>
        
        <!-- 锐化 -->
        <LinearLayout
            android:id="@+id/btn_sharpen"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_columnWeight="1"
            android:orientation="vertical"
            android:gravity="center"
            android:padding="12dp"
            android:clickable="true"
            android:background="@drawable/beautify_button_bg">
            
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="🔍"
                android:textSize="32sp"/>
            
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="锐化"
                android:textColor="@color/white"
                android:textSize="12sp"
                android:layout_marginTop="4dp"/>
        </LinearLayout>
        
        <!-- 去雾 -->
        <LinearLayout
            android:id="@+id/btn_dehaze"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_columnWeight="1"
            android:orientation="vertical"
            android:gravity="center"
            android:padding="12dp"
            android:clickable="true"
            android:background="@drawable/beautify_button_bg">
            
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="🌫️"
                android:textSize="32sp"/>
            
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="去雾"
                android:textColor="@color/white"
                android:textSize="12sp"
                android:layout_marginTop="4dp"/>
        </LinearLayout>
        
        <!-- 暗角 -->
        <LinearLayout
            android:id="@+id/btn_vignette"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_columnWeight="1"
            android:orientation="vertical"
            android:gravity="center"
            android:padding="12dp"
            android:clickable="true"
            android:background="@drawable/beautify_button_bg">
            
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="🎭"
                android:textSize="32sp"/>
            
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="暗角"
                android:textColor="@color/white"
                android:textSize="12sp"
                android:layout_marginTop="4dp"/>
        </LinearLayout>
        
        <!-- 光晕 -->
        <LinearLayout
            android:id="@+id/btn_glow"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_columnWeight="1"
            android:orientation="vertical"
            android:gravity="center"
            android:padding="12dp"
            android:clickable="true"
            android:background="@drawable/beautify_button_bg">
            
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="✨"
                android:textSize="32sp"/>
            
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="光晕"
                android:textColor="@color/white"
                android:textSize="12sp"
                android:layout_marginTop="4dp"/>
        </LinearLayout>
        
        <!-- 柔焦 -->
        <LinearLayout
            android:id="@+id/btn_soft_focus"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_columnWeight="1"
            android:orientation="vertical"
            android:gravity="center"
            android:padding="12dp"
            android:clickable="true"
            android:background="@drawable/beautify_button_bg">
            
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="🌸"
                android:textSize="32sp"/>
            
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="柔焦"
                android:textColor="@color/white"
                android:textSize="12sp"
                android:layout_marginTop="4dp"/>
        </LinearLayout>
    </GridLayout>
    
    <!-- 强度调节 -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:paddingHorizontal="20dp"
        android:paddingVertical="12dp"
        android:background="#252525">
        
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginBottom="8dp">
            
            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="强度"
                android:textColor="@color/white"
                android:textSize="14sp"/>
            
            <TextView
                android:id="@+id/text_beautify_intensity"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="80%"
                android:textColor="#FFD700"
                android:textSize="16sp"
                android:textStyle="bold"/>
        </LinearLayout>
        
        <SeekBar
            android:id="@+id/seekbar_beautify_intensity"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="100"
            android:progress="80"
            android:progressTint="#FFD700"
            android:thumbTint="#FFD700"/>
    </LinearLayout>
    
    <!-- 底部按钮 -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="16dp">
        
        <Button
            android:id="@+id/btn_beautify_reset"
            android:layout_width="0dp"
            android:layout_height="48dp"
            android:layout_weight="1"
            android:text="重置"
            android:textColor="@color/white"
            android:textSize="16sp"
            android:background="@drawable/rounded_button_dark_bg"
            android:layout_marginEnd="8dp"/>
        
        <Button
            android:id="@+id/btn_beautify_apply"
            android:layout_width="0dp"
            android:layout_height="48dp"
            android:layout_weight="1"
            android:text="应用"
            android:textColor="@color/black"
            android:textSize="16sp"
            android:background="#FFD700"
            android:layout_marginStart="8dp"/>
    </LinearLayout>
    
</LinearLayout>
```

### 按钮背景

```xml
<!-- drawable/beautify_button_bg.xml -->
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_pressed="true">
        <shape>
            <solid android:color="#FFD700"/>
            <corners android:radius="8dp"/>
        </shape>
    </item>
    <item android:state_selected="true">
        <shape>
            <solid android:color="#3A3A3A"/>
            <stroke android:width="2dp" android:color="#FFD700"/>
            <corners android:radius="8dp"/>
        </shape>
    </item>
    <item>
        <shape>
            <solid android:color="#2A2A2A"/>
            <corners android:radius="8dp"/>
        </shape>
    </item>
</selector>
```

---

## 实现架构

### 包结构

```
app/src/main/java/com/example/photoshop_demo/beautify/
├── BeautifyEffect.java          # 枚举：6种美化效果
├── AutoEnhanceFilter.java       # 自动增强
├── SharpenFilter.java           # 锐化
├── DehazeFilter.java            # 去雾
├── VignetteFilter.java          # 暗角
├── GlowFilter.java              # 光晕
├── SoftFocusFilter.java         # 柔焦
└── BeautifyManager.java         # 管理器（统一接口）
```

### BeautifyEffect枚举

```java
public enum BeautifyEffect {
    AUTO_ENHANCE("⚡", "自动增强"),
    SHARPEN("🔍", "锐化"),
    DEHAZE("🌫️", "去雾"),
    VIGNETTE("🎭", "暗角"),
    GLOW("✨", "光晕"),
    SOFT_FOCUS("🌸", "柔焦");
    
    private final String icon;
    private final String name;
    
    BeautifyEffect(String icon, String name) {
        this.icon = icon;
        this.name = name;
    }
    
    public String getIcon() { return icon; }
    public String getName() { return name; }
}
```

### BeautifyManager

```java
public class BeautifyManager {
    
    public static Bitmap applyEffect(Bitmap source, BeautifyEffect effect, 
                                     float intensity) {
        switch (effect) {
            case AUTO_ENHANCE:
                return AutoEnhanceFilter.apply(source, intensity);
            case SHARPEN:
                return SharpenFilter.apply(source, intensity);
            case DEHAZE:
                return DehazeFilter.apply(source, intensity);
            case VIGNETTE:
                return VignetteFilter.apply(source, intensity);
            case GLOW:
                return GlowFilter.apply(source, intensity);
            case SOFT_FOCUS:
                return SoftFocusFilter.apply(source, intensity);
            default:
                return source;
        }
    }
}
```

---

## 性能优化

### 1. 使用RenderScript

```java
// 高斯模糊加速
public static Bitmap fastBlur(Context context, Bitmap source, int radius) {
    RenderScript rs = RenderScript.create(context);
    
    Allocation input = Allocation.createFromBitmap(rs, source);
    Allocation output = Allocation.createTyped(rs, input.getType());
    
    ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(
        rs, Element.U8_4(rs));
    script.setRadius(radius);
    script.setInput(input);
    script.forEach(output);
    
    output.copyTo(source);
    
    input.destroy();
    output.destroy();
    script.destroy();
    rs.destroy();
    
    return source;
}
```

### 2. 多线程处理

```java
// 分块处理大图
ExecutorService executor = Executors.newFixedThreadPool(4);
List<Future<?>> futures = new ArrayList<>();

for (int i = 0; i < 4; i++) {
    final int blockIndex = i;
    futures.add(executor.submit(() -> {
        // 处理第blockIndex块
    }));
}

// 等待完成
for (Future<?> future : futures) {
    future.get();
}
```

### 3. 缓存优化

```java
// 缓存中间结果
private Map<String, Bitmap> cache = new LruCache<>(10 * 1024 * 1024);

public Bitmap applyWithCache(Bitmap source, BeautifyEffect effect, 
                             float intensity) {
    String key = effect.name() + "_" + intensity;
    Bitmap cached = cache.get(key);
    if (cached != null) return cached;
    
    Bitmap result = applyEffect(source, effect, intensity);
    cache.put(key, result);
    return result;
}
```

### 4. 降采样预览

```java
// 预览时使用小图
Bitmap preview = Bitmap.createScaledBitmap(
    source, 
    source.getWidth() / 4, 
    source.getHeight() / 4, 
    true
);

// 应用效果到小图
Bitmap previewResult = applyEffect(preview, effect, intensity);

// 实际应用时使用原图
Bitmap finalResult = applyEffect(source, effect, intensity);
```

---

## 总结

### 6个功能对比

| 功能 | 复杂度 | 性能 | 效果 | 推荐度 |
|------|--------|------|------|--------|
| 自动增强 | ⭐⭐ | 快 | 明显 | ⭐⭐⭐⭐⭐ |
| 锐化 | ⭐⭐ | 中 | 明显 | ⭐⭐⭐⭐ |
| 去雾 | ⭐⭐⭐ | 中 | 明显 | ⭐⭐⭐⭐ |
| 暗角 | ⭐ | 快 | 明显 | ⭐⭐⭐⭐ |
| 光晕 | ⭐⭐⭐ | 慢 | 艺术 | ⭐⭐⭐ |
| 柔焦 | ⭐⭐⭐ | 慢 | 明显 | ⭐⭐⭐⭐ |

### 实现优先级

**第一批**（必须）：
1. 自动增强 - 最常用
2. 锐化 - 简单实用
3. 暗角 - 简单有效

**第二批**（推荐）：
4. 去雾 - 特定场景很有用
5. 柔焦 - 人像必备

**第三批**（可选）：
6. 光晕 - 艺术效果

### 预估工作量

- **代码量**：~1000行
- **时间**：2-3小时
- **难点**：高斯模糊性能优化

---

**准备好开始实现了吗？** 🚀

