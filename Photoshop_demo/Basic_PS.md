# 第三阶段教学：图片基本编辑功能实现

## 📚 目录
1. [功能概述](#1-功能概述)
2. [图片处理原理](#2-图片处理原理)
3. [裁切功能实现](#3-裁切功能实现)
4. [亮度调整实现](#4-亮度调整实现)
5. [对比度调整实现](#5-对比度调整实现)
6. [饱和度调整实现](#6-饱和度调整实现)
7. [UI设计方案](#7-ui设计方案)
8. [完整实现步骤](#8-完整实现步骤)
9. [性能优化](#9-性能优化)
10. [最佳实践](#10-最佳实践)

---

## 1. 功能概述

### 1.1 要实现的功能列表

| 功能 | 说明 | 难度 | 优先级 |
|------|------|------|--------|
| **裁切** | 选择区域裁剪图片 | ⭐⭐⭐ | 高 |
| **亮度** | 调整图片明暗 | ⭐⭐ | 高 |
| **对比度** | 调整明暗对比 | ⭐⭐ | 高 |
| **饱和度** | 调整色彩鲜艳度 | ⭐⭐ | 中 |
| **旋转** | 90度旋转/翻转 | ⭐ | 中 |
| **色温** | 调整冷暖色调 | ⭐⭐ | 低 |

### 1.2 用户交互设计

```
编辑页面布局：
┌──────────────────────────┐
│ ← ↶ ⋯ ⭐AI 【导出】      │ 顶部工具栏
├──────────────────────────┤
│                          │
│      [图片显示区域]       │ 中央显示区
│                          │
├──────────────────────────┤
│ 亮度 [---------●---] +50 │ 调整滑块
│ 对比 [-----------●-] +30 │
├──────────────────────────┤
│ [裁切][旋转][滤镜][调整] │ 功能标签
└──────────────────────────┘
```

---

## 2. 图片处理原理

### 2.1 Bitmap基础知识

#### 什么是Bitmap？
```
Bitmap是Android中表示位图的类
位图 = 像素矩阵

例如：100x100的图片 = 10,000个像素点
每个像素存储ARGB值（透明度、红、绿、蓝）
```

#### 内存计算
```java
内存占用 = 宽度 × 高度 × 4字节（ARGB_8888）

例如：
- 1080x1920的图片 = 1080 × 1920 × 4 = 8,294,400字节 ≈ 8MB
- 4K图片(3840x2160) = 3840 × 2160 × 4 = 33,177,600字节 ≈ 32MB
```

**重要提示**：处理大图片需要注意内存管理！

### 2.2 像素操作原理

#### ARGB颜色模型
```
每个像素是一个32位整数：
AAAAAAAA RRRRRRRR GGGGGGGG BBBBBBBB
│        │        │        │
│        │        │        └─ Blue (0-255)
│        │        └────────── Green (0-255)
│        └─────────────────── Red (0-255)
└──────────────────────────── Alpha/透明度 (0-255)

提取颜色分量：
int pixel = bitmap.getPixel(x, y);
int alpha = (pixel >> 24) & 0xFF;  // 右移24位，取最高8位
int red   = (pixel >> 16) & 0xFF;  // 右移16位，取8位
int green = (pixel >> 8)  & 0xFF;  // 右移8位，取8位
int blue  = pixel & 0xFF;          // 取最低8位
```

#### 合成新像素
```java
int newPixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
bitmap.setPixel(x, y, newPixel);
```

---

## 3. 裁切功能实现

### 3.1 裁切原理

裁切就是从原图中提取一个矩形区域。

```
原图 (800x600)                  裁切后 (400x300)
┌────────────────┐             ┌──────────┐
│                │             │          │
│   ┌──────────┐ │             │          │
│   │ 裁切区域 │ │  ========>  │          │
│   │          │ │             │          │
│   └──────────┘ │             └──────────┘
│                │
└────────────────┘
```

### 3.2 实现方法

#### 方法1：使用Bitmap.createBitmap()（推荐）

```java
/**
 * 裁切图片
 * @param source 原始图片
 * @param x 起始X坐标
 * @param y 起始Y坐标
 * @param width 裁切宽度
 * @param height 裁切高度
 * @return 裁切后的图片
 */
public Bitmap cropImage(Bitmap source, int x, int y, int width, int height) {
    // 检查参数合法性
    if (x < 0) x = 0;
    if (y < 0) y = 0;
    if (x + width > source.getWidth()) {
        width = source.getWidth() - x;
    }
    if (y + height > source.getHeight()) {
        height = source.getHeight() - y;
    }
    
    // 裁切（不会修改原图）
    return Bitmap.createBitmap(source, x, y, width, height);
}

// 使用示例
Bitmap cropped = cropImage(currentBitmap, 100, 100, 400, 300);
imageView.setImageBitmap(cropped);
```

**为什么推荐这个方法？**
- ✅ 简单、快速
- ✅ 系统优化
- ✅ 不修改原图

#### 方法2：使用Canvas绘制

```java
public Bitmap cropImageWithCanvas(Bitmap source, Rect srcRect) {
    // 创建目标Bitmap
    Bitmap cropped = Bitmap.createBitmap(
        srcRect.width(), 
        srcRect.height(), 
        Bitmap.Config.ARGB_8888
    );
    
    // 创建Canvas
    Canvas canvas = new Canvas(cropped);
    
    // 定义目标矩形（从0,0开始）
    Rect dstRect = new Rect(0, 0, srcRect.width(), srcRect.height());
    
    // 绘制
    canvas.drawBitmap(source, srcRect, dstRect, null);
    
    return cropped;
}

// 使用示例
Rect cropRect = new Rect(100, 100, 500, 400);  // left, top, right, bottom
Bitmap cropped = cropImageWithCanvas(currentBitmap, cropRect);
```

### 3.3 交互式裁切UI

#### 需要实现的组件

1. **CropOverlayView** - 裁切遮罩层
```java
public class CropOverlayView extends View {
    private RectF cropRect;  // 裁切矩形
    private Paint cropPaint;  // 绘制画笔
    private Paint dimPaint;   // 半透明背景
    
    // 四个角的触摸检测
    private static final int CORNER_NONE = 0;
    private static final int CORNER_TOP_LEFT = 1;
    private static final int CORNER_TOP_RIGHT = 2;
    private static final int CORNER_BOTTOM_LEFT = 3;
    private static final int CORNER_BOTTOM_RIGHT = 4;
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 1. 绘制半透明背景（裁切区域外）
        canvas.drawColor(0x99000000);  // 60%透明黑色
        
        // 2. 绘制裁切区域（清除透明背景）
        canvas.drawRect(cropRect, clearPaint);
        
        // 3. 绘制裁切框
        canvas.drawRect(cropRect, cropPaint);
        
        // 4. 绘制九宫格线
        drawGrid(canvas);
        
        // 5. 绘制四个角的控制点
        drawCorners(canvas);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 检测触摸的是哪个角
                detectCorner(event.getX(), event.getY());
                break;
                
            case MotionEvent.ACTION_MOVE:
                // 拖动调整裁切区域
                adjustCropRect(event.getX(), event.getY());
                invalidate();  // 重绘
                break;
                
            case MotionEvent.ACTION_UP:
                // 完成调整
                break;
        }
        return true;
    }
}
```

#### 在EditActivity中使用

```java
public class EditActivity extends AppCompatActivity {
    private ImageView imageView;
    private CropOverlayView cropOverlay;
    private boolean isCropMode = false;
    
    private void enterCropMode() {
        isCropMode = true;
        
        // 显示裁切遮罩
        cropOverlay.setVisibility(View.VISIBLE);
        
        // 初始化裁切区域（默认80%的图片区域）
        int imgWidth = imageView.getWidth();
        int imgHeight = imageView.getHeight();
        int margin = (int)(imgWidth * 0.1);
        
        RectF initialRect = new RectF(
            margin, 
            margin, 
            imgWidth - margin, 
            imgHeight - margin
        );
        cropOverlay.setCropRect(initialRect);
        
        // 更改底部工具栏：显示"取消"和"完成"按钮
        showCropControls();
    }
    
    private void applyCrop() {
        // 获取裁切矩形
        RectF cropRect = cropOverlay.getCropRect();
        
        // 将屏幕坐标转换为图片坐标
        RectF imageCropRect = convertToImageCoordinates(cropRect);
        
        // 执行裁切
        Bitmap cropped = Bitmap.createBitmap(
            currentBitmap,
            (int)imageCropRect.left,
            (int)imageCropRect.top,
            (int)imageCropRect.width(),
            (int)imageCropRect.height()
        );
        
        // 更新显示
        currentBitmap = cropped;
        imageView.setImageBitmap(cropped);
        
        // 退出裁切模式
        exitCropMode();
    }
}
```

### 3.4 坐标转换

**关键问题**：ImageView的尺寸 ≠ Bitmap的尺寸！

```java
/**
 * 将屏幕坐标转换为图片坐标
 * 原因：ImageView可能缩放了图片
 */
private RectF convertToImageCoordinates(RectF screenRect) {
    // 获取ImageView的显示矩阵
    Matrix matrix = imageView.getImageMatrix();
    
    // 获取Bitmap尺寸
    int bitmapWidth = currentBitmap.getWidth();
    int bitmapHeight = currentBitmap.getHeight();
    
    // 获取ImageView尺寸
    int viewWidth = imageView.getWidth();
    int viewHeight = imageView.getHeight();
    
    // 计算缩放比例
    float scaleX = (float)bitmapWidth / viewWidth;
    float scaleY = (float)bitmapHeight / viewHeight;
    
    // 转换坐标
    return new RectF(
        screenRect.left * scaleX,
        screenRect.top * scaleY,
        screenRect.right * scaleX,
        screenRect.bottom * scaleY
    );
}
```

---

## 4. 亮度调整实现

### 4.1 亮度原理

**亮度**：图片的明暗程度

调整方法：对每个像素的RGB值加上或减去一个值

```
原始像素: R=100, G=150, B=200
亮度+50:  R=150, G=200, B=250
亮度-50:  R=50,  G=100, B=150

注意：需要限制在0-255范围内
```

### 4.2 实现代码

#### 方法1：逐像素处理（精确但较慢）

```java
/**
 * 调整亮度
 * @param source 原始图片
 * @param brightness 亮度值 (-255 到 +255)
 * @return 调整后的图片
 */
public Bitmap adjustBrightness(Bitmap source, int brightness) {
    // 创建新的Bitmap
    Bitmap result = source.copy(source.getConfig(), true);
    
    int width = result.getWidth();
    int height = result.getHeight();
    
    // 遍历所有像素
    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            // 获取原始像素
            int pixel = result.getPixel(x, y);
            
            // 提取ARGB分量
            int alpha = (pixel >> 24) & 0xFF;
            int red   = (pixel >> 16) & 0xFF;
            int green = (pixel >> 8)  & 0xFF;
            int blue  = pixel & 0xFF;
            
            // 调整亮度（限制范围0-255）
            red   = clamp(red + brightness);
            green = clamp(green + brightness);
            blue  = clamp(blue + brightness);
            
            // 合成新像素
            int newPixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
            result.setPixel(x, y, newPixel);
        }
    }
    
    return result;
}

/**
 * 将值限制在0-255范围内
 */
private int clamp(int value) {
    if (value < 0) return 0;
    if (value > 255) return 255;
    return value;
}

// 使用示例
Bitmap brightened = adjustBrightness(currentBitmap, 50);  // 增加50亮度
```

#### 方法2：使用ColorMatrix（推荐，更快）

```java
/**
 * 使用ColorMatrix调整亮度
 * 优点：GPU加速，速度快
 */
public Bitmap adjustBrightnessWithColorMatrix(Bitmap source, int brightness) {
    // 创建ColorMatrix
    ColorMatrix colorMatrix = new ColorMatrix();
    
    // 设置亮度矩阵
    colorMatrix.set(new float[] {
        1, 0, 0, 0, brightness,  // Red
        0, 1, 0, 0, brightness,  // Green
        0, 0, 1, 0, brightness,  // Blue
        0, 0, 0, 1, 0            // Alpha
    });
    
    // 创建Paint并应用ColorMatrix
    Paint paint = new Paint();
    paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
    
    // 创建新Bitmap并绘制
    Bitmap result = Bitmap.createBitmap(
        source.getWidth(), 
        source.getHeight(), 
        source.getConfig()
    );
    Canvas canvas = new Canvas(result);
    canvas.drawBitmap(source, 0, 0, paint);
    
    return result;
}

// 使用示例
Bitmap brightened = adjustBrightnessWithColorMatrix(currentBitmap, 30);
```

#### 方法3：实时预览（不创建新Bitmap）

```java
/**
 * ImageView实时预览效果
 * 优点：不消耗额外内存，只改变显示效果
 */
public void previewBrightness(ImageView imageView, int brightness) {
    ColorMatrix colorMatrix = new ColorMatrix();
    colorMatrix.set(new float[] {
        1, 0, 0, 0, brightness,
        0, 1, 0, 0, brightness,
        0, 0, 1, 0, brightness,
        0, 0, 0, 1, 0
    });
    
    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
    imageView.setColorFilter(filter);
}

// 使用示例
// 在SeekBar的监听器中调用
seekBarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int brightness = progress - 100;  // 范围：-100 到 +100
        previewBrightness(imageView, brightness);
        textBrightness.setText(String.format("%+d", brightness));
    }
    
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // 用户松手后，应用真实效果
        int brightness = seekBar.getProgress() - 100;
        currentBitmap = adjustBrightnessWithColorMatrix(currentBitmap, brightness);
        imageView.setColorFilter(null);  // 清除预览滤镜
        imageView.setImageBitmap(currentBitmap);
    }
});
```

### 4.3 亮度UI设计

```xml
<!-- 亮度调整控件 -->
<LinearLayout
    android:id="@+id/brightness_control"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="16dp"
    android:visibility="gone">
    
    <!-- 减号 -->
    <TextView
        android:id="@+id/btn_brightness_minus"
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:text="-"
        android:gravity="center"
        android:textSize="20sp"
        android:textColor="@color/white"
        android:background="@drawable/circle_button_bg"/>
    
    <!-- 滑块 -->
    <SeekBar
        android:id="@+id/seekbar_brightness"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:layout_gravity="center_vertical"
        android:layout_marginHorizontal="16dp"
        android:max="200"
        android:progress="100"/>
    
    <!-- 加号 -->
    <TextView
        android:id="@+id/btn_brightness_plus"
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:text="+"
        android:gravity="center"
        android:textSize="20sp"
        android:textColor="@color/white"
        android:background="@drawable/circle_button_bg"/>
    
    <!-- 数值显示 -->
    <TextView
        android:id="@+id/text_brightness_value"
        android:layout_width="60dp"
        android:layout_height="wrap_content"
        android:text="0"
        android:gravity="center"
        android:textColor="@color/white"
        android:textSize="16sp"
        android:layout_marginStart="8dp"/>
        
</LinearLayout>
```

---

## 5. 对比度调整实现

### 5.1 对比度原理

**对比度**：图片明暗区域的差异程度

调整方法：将RGB值乘以一个系数

```
对比度 = 1.0（原始）
对比度 > 1.0（增强对比）：亮的更亮，暗的更暗
对比度 < 1.0（减弱对比）：趋向中间灰色

计算公式：
newValue = (oldValue - 128) × contrast + 128

128是中间灰度值
```

### 5.2 实现代码

```java
/**
 * 调整对比度
 * @param source 原始图片
 * @param contrast 对比度 (0.0 - 2.0)
 *                 1.0 = 原始
 *                 < 1.0 = 减弱对比
 *                 > 1.0 = 增强对比
 */
public Bitmap adjustContrast(Bitmap source, float contrast) {
    Bitmap result = source.copy(source.getConfig(), true);
    
    int width = result.getWidth();
    int height = result.getHeight();
    
    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            int pixel = result.getPixel(x, y);
            
            int alpha = (pixel >> 24) & 0xFF;
            int red   = (pixel >> 16) & 0xFF;
            int green = (pixel >> 8)  & 0xFF;
            int blue  = pixel & 0xFF;
            
            // 应用对比度公式
            red   = clamp((int)((red - 128) * contrast + 128));
            green = clamp((int)((green - 128) * contrast + 128));
            blue  = clamp((int)((blue - 128) * contrast + 128));
            
            int newPixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
            result.setPixel(x, y, newPixel);
        }
    }
    
    return result;
}
```

#### 使用ColorMatrix实现（更快）

```java
/**
 * 使用ColorMatrix调整对比度
 */
public Bitmap adjustContrastWithColorMatrix(Bitmap source, float contrast) {
    ColorMatrix colorMatrix = new ColorMatrix();
    
    float offset = (1.0f - contrast) * 128;
    
    colorMatrix.set(new float[] {
        contrast, 0, 0, 0, offset,
        0, contrast, 0, 0, offset,
        0, 0, contrast, 0, offset,
        0, 0, 0, 1, 0
    });
    
    Paint paint = new Paint();
    paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
    
    Bitmap result = Bitmap.createBitmap(
        source.getWidth(), 
        source.getHeight(), 
        source.getConfig()
    );
    Canvas canvas = new Canvas(result);
    canvas.drawBitmap(source, 0, 0, paint);
    
    return result;
}

// 使用示例
Bitmap highContrast = adjustContrastWithColorMatrix(currentBitmap, 1.5f);  // 增强50%
Bitmap lowContrast = adjustContrastWithColorMatrix(currentBitmap, 0.7f);   // 减弱30%
```

---

## 6. 饱和度调整实现

### 6.1 饱和度原理

**饱和度**：颜色的鲜艳程度

```
饱和度 = 0：完全灰色（黑白图片）
饱和度 = 1：原始颜色
饱和度 > 1：更加鲜艳
```

### 6.2 实现代码

```java
/**
 * 调整饱和度
 * @param source 原始图片
 * @param saturation 饱和度 (0.0 - 2.0)
 *                   0 = 黑白
 *                   1 = 原始
 *                   2 = 超饱和
 */
public Bitmap adjustSaturation(Bitmap source, float saturation) {
    ColorMatrix colorMatrix = new ColorMatrix();
    colorMatrix.setSaturation(saturation);  // Android提供的便捷方法
    
    Paint paint = new Paint();
    paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
    
    Bitmap result = Bitmap.createBitmap(
        source.getWidth(), 
        source.getHeight(), 
        source.getConfig()
    );
    Canvas canvas = new Canvas(result);
    canvas.drawBitmap(source, 0, 0, paint);
    
    return result;
}

// 使用示例
Bitmap bw = adjustSaturation(currentBitmap, 0);      // 黑白
Bitmap vivid = adjustSaturation(currentBitmap, 1.5f); // 鲜艳
```

---

## 7. UI设计方案

### 7.1 编辑模式切换

```
底部工具栏设计：

默认模式：
┌────────────────────────────────────┐
│ [裁切] [调整] [滤镜] [美化] [文字] │
└────────────────────────────────────┘

点击"调整"后：
┌────────────────────────────────────┐
│  亮度  [---------●-------]  +20    │
│  对比  [----------●------]  +15    │
│  饱和  [-----------●-----]  +10    │
│                                    │
│  [重置]          [完成]            │
└────────────────────────────────────┘
```

### 7.2 布局XML示例

```xml
<!-- activity_edit.xml 底部添加 -->

<!-- 编辑工具面板容器 -->
<FrameLayout
    android:id="@+id/edit_panel_container"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_above="@id/bottom_toolbar"
    android:background="@color/dark_background"
    android:visibility="gone">
    
    <!-- 调整面板 -->
    <LinearLayout
        android:id="@+id/adjust_panel"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        
        <!-- 亮度 -->
        <include layout="@layout/adjust_slider_brightness"/>
        
        <!-- 对比度 -->
        <include layout="@layout/adjust_slider_contrast"/>
        
        <!-- 饱和度 -->
        <include layout="@layout/adjust_slider_saturation"/>
        
        <!-- 控制按钮 -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginTop="16dp">
            
            <Button
                android:id="@+id/btn_reset"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="重置"
                android:layout_marginEnd="8dp"/>
            
            <Button
                android:id="@+id/btn_apply"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="完成"
                android:backgroundTint="@color/yellow"
                android:textColor="@color/black"
                android:layout_marginStart="8dp"/>
        </LinearLayout>
        
    </LinearLayout>
    
    <!-- 裁切面板（后续添加） -->
    
    <!-- 滤镜面板（后续添加） -->
    
</FrameLayout>
```

### 7.3 可复用的滑块组件

```xml
<!-- adjust_slider_brightness.xml -->
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:layout_marginBottom="12dp">
    
    <!-- 标题和数值 -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">
        
        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="亮度"
            android:textColor="@color/white"
            android:textSize="14sp"/>
        
        <TextView
            android:id="@+id/text_brightness_value"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="0"
            android:textColor="@color/yellow"
            android:textSize="14sp"
            android:minWidth="40dp"
            android:gravity="end"/>
    </LinearLayout>
    
    <!-- 滑块 -->
    <SeekBar
        android:id="@+id/seekbar_brightness"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:max="200"
        android:progress="100"
        android:layout_marginTop="8dp"/>
        
</LinearLayout>
```

---

## 8. 完整实现步骤

### 8.1 第一步：创建ImageProcessor工具类

```java
/**
 * 图片处理工具类
 * 集中管理所有图片编辑算法
 */
public class ImageProcessor {
    
    /**
     * 裁切图片
     */
    public static Bitmap crop(Bitmap source, int x, int y, int width, int height) {
        // 参数验证
        x = Math.max(0, x);
        y = Math.max(0, y);
        width = Math.min(width, source.getWidth() - x);
        height = Math.min(height, source.getHeight() - y);
        
        return Bitmap.createBitmap(source, x, y, width, height);
    }
    
    /**
     * 调整亮度
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
     */
    public static Bitmap adjustSaturation(Bitmap source, float saturation) {
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(saturation);
        return applyColorMatrix(source, cm);
    }
    
    /**
     * 组合调整（亮度+对比度+饱和度）
     * 优化：一次处理，避免多次创建Bitmap
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
     * 应用ColorMatrix
     */
    private static Bitmap applyColorMatrix(Bitmap source, ColorMatrix cm) {
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        
        Bitmap result = Bitmap.createBitmap(
            source.getWidth(), 
            source.getHeight(), 
            source.getConfig()
        );
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(source, 0, 0, paint);
        
        return result;
    }
    
    /**
     * 90度旋转
     */
    public static Bitmap rotate90(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(source, 0, 0, 
            source.getWidth(), source.getHeight(), matrix, true);
    }
    
    /**
     * 水平翻转
     */
    public static Bitmap flipHorizontal(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1, source.getWidth() / 2f, source.getHeight() / 2f);
        return Bitmap.createBitmap(source, 0, 0, 
            source.getWidth(), source.getHeight(), matrix, true);
    }
    
    /**
     * 垂直翻转
     */
    public static Bitmap flipVertical(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.postScale(1, -1, source.getWidth() / 2f, source.getHeight() / 2f);
        return Bitmap.createBitmap(source, 0, 0, 
            source.getWidth(), source.getHeight(), matrix, true);
    }
}
```

### 8.2 第二步：在EditActivity中集成

```java
public class EditActivity extends AppCompatActivity {
    
    // 图片状态
    private Bitmap originalBitmap;   // 原始图片
    private Bitmap currentBitmap;    // 当前编辑的图片
    private Bitmap previewBitmap;    // 预览用的临时图片
    
    // 调整参数（当前值）
    private int currentBrightness = 0;     // -100 to +100
    private float currentContrast = 1.0f;  // 0.5 to 2.0
    private float currentSaturation = 1.0f; // 0.0 to 2.0
    
    // UI组件
    private ImageView imageView;
    private ViewGroup adjustPanel;
    private SeekBar seekBarBrightness;
    private SeekBar seekBarContrast;
    private SeekBar seekBarSaturation;
    private TextView textBrightnessValue;
    private TextView textContrastValue;
    private TextView textSaturationValue;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        
        initViews();
        loadImage();
        setupAdjustPanel();
    }
    
    private void initViews() {
        imageView = findViewById(R.id.image_view);
        adjustPanel = findViewById(R.id.adjust_panel);
        
        seekBarBrightness = findViewById(R.id.seekbar_brightness);
        seekBarContrast = findViewById(R.id.seekbar_contrast);
        seekBarSaturation = findViewById(R.id.seekbar_saturation);
        
        textBrightnessValue = findViewById(R.id.text_brightness_value);
        textContrastValue = findViewById(R.id.text_contrast_value);
        textSaturationValue = findViewById(R.id.text_saturation_value);
    }
    
    private void setupAdjustPanel() {
        // 亮度滑块（-100 到 +100）
        seekBarBrightness.setMax(200);
        seekBarBrightness.setProgress(100);  // 中间值 = 0
        seekBarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentBrightness = progress - 100;
                textBrightnessValue.setText(String.format("%+d", currentBrightness));
                previewAdjustments();
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // 对比度滑块（0.5 到 2.0）
        seekBarContrast.setMax(150);
        seekBarContrast.setProgress(100);  // 中间值 = 1.0
        seekBarContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentContrast = progress / 100.0f;  // 转换为 0.0 - 1.5
                if (currentContrast < 0.5f) currentContrast = 0.5f;
                textContrastValue.setText(String.format("%.2f", currentContrast));
                previewAdjustments();
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // 饱和度滑块（0.0 到 2.0）
        seekBarSaturation.setMax(200);
        seekBarSaturation.setProgress(100);  // 中间值 = 1.0
        seekBarSaturation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentSaturation = progress / 100.0f;
                textSaturationValue.setText(String.format("%.2f", currentSaturation));
                previewAdjustments();
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // 重置按钮
        findViewById(R.id.btn_reset).setOnClickListener(v -> resetAdjustments());
        
        // 完成按钮
        findViewById(R.id.btn_apply).setOnClickListener(v -> applyAdjustments());
    }
    
    /**
     * 实时预览调整效果
     * 使用ColorFilter，不创建新Bitmap，节省内存
     */
    private void previewAdjustments() {
        // 组合ColorMatrix
        ColorMatrix cmBrightness = new ColorMatrix();
        cmBrightness.set(new float[] {
            1, 0, 0, 0, currentBrightness,
            0, 1, 0, 0, currentBrightness,
            0, 0, 1, 0, currentBrightness,
            0, 0, 0, 1, 0
        });
        
        float offset = (1.0f - currentContrast) * 128;
        ColorMatrix cmContrast = new ColorMatrix();
        cmContrast.set(new float[] {
            currentContrast, 0, 0, 0, offset,
            0, currentContrast, 0, 0, offset,
            0, 0, currentContrast, 0, offset,
            0, 0, 0, 1, 0
        });
        
        ColorMatrix cmSaturation = new ColorMatrix();
        cmSaturation.setSaturation(currentSaturation);
        
        // 组合矩阵
        ColorMatrix combined = new ColorMatrix();
        combined.postConcat(cmBrightness);
        combined.postConcat(cmContrast);
        combined.postConcat(cmSaturation);
        
        // 应用到ImageView（只改变显示，不改变Bitmap）
        imageView.setColorFilter(new ColorMatrixColorFilter(combined));
    }
    
    /**
     * 重置所有调整
     */
    private void resetAdjustments() {
        currentBrightness = 0;
        currentContrast = 1.0f;
        currentSaturation = 1.0f;
        
        seekBarBrightness.setProgress(100);
        seekBarContrast.setProgress(100);
        seekBarSaturation.setProgress(100);
        
        imageView.setColorFilter(null);
    }
    
    /**
     * 应用调整到实际Bitmap
     */
    private void applyAdjustments() {
        // 显示进度提示
        Toast.makeText(this, "正在处理...", Toast.LENGTH_SHORT).show();
        
        // 在后台线程处理
        new Thread(() -> {
            // 应用所有调整
            Bitmap adjusted = ImageProcessor.adjustAll(
                currentBitmap,
                currentBrightness,
                currentContrast,
                currentSaturation
            );
            
            // 回到主线程更新UI
            runOnUiThread(() -> {
                // 释放旧Bitmap
                if (currentBitmap != originalBitmap) {
                    currentBitmap.recycle();
                }
                
                // 更新当前Bitmap
                currentBitmap = adjusted;
                
                // 清除ColorFilter并显示新Bitmap
                imageView.setColorFilter(null);
                imageView.setImageBitmap(currentBitmap);
                
                // 重置参数
                resetAdjustments();
                
                // 隐藏调整面板
                adjustPanel.setVisibility(View.GONE);
                
                Toast.makeText(this, "调整已应用", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
    
    /**
     * 显示调整面板
     */
    private void showAdjustPanel() {
        adjustPanel.setVisibility(View.VISIBLE);
    }
}
```

### 8.3 第三步：添加底部工具栏切换

```java
// 在EditActivity中添加底部按钮点击事件

private void setupBottomToolbar() {
    // 裁切按钮
    findViewById(R.id.btn_tool_crop).setOnClickListener(v -> {
        // 显示裁切面板（下一步实现）
        Toast.makeText(this, "裁切功能", Toast.LENGTH_SHORT).show();
    });
    
    // 调整按钮
    findViewById(R.id.btn_tool_adjust).setOnClickListener(v -> {
        showAdjustPanel();
    });
    
    // 滤镜按钮
    findViewById(R.id.btn_tool_filter).setOnClickListener(v -> {
        Toast.makeText(this, "滤镜功能", Toast.LENGTH_SHORT).show();
    });
    
    // 旋转按钮
    findViewById(R.id.btn_tool_rotate).setOnClickListener(v -> {
        rotateImage();
    });
}

/**
 * 旋转图片
 */
private void rotateImage() {
    new Thread(() -> {
        Bitmap rotated = ImageProcessor.rotate90(currentBitmap);
        runOnUiThread(() -> {
            if (currentBitmap != originalBitmap) {
                currentBitmap.recycle();
            }
            currentBitmap = rotated;
            imageView.setImageBitmap(currentBitmap);
        });
    }).start();
}
```

---

## 9. 性能优化

### 9.1 内存优化

#### 问题：处理大图片容易OOM

```java
/**
 * 按需缩放加载图片
 * 原因：避免加载过大的图片导致内存溢出
 */
public static Bitmap decodeSampledBitmap(Uri uri, Context context, 
                                         int reqWidth, int reqHeight) {
    try {
        // 第一步：只解析尺寸，不加载像素数据
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        
        InputStream is = context.getContentResolver().openInputStream(uri);
        BitmapFactory.decodeStream(is, null, options);
        is.close();
        
        // 计算缩放比例
        int sampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        
        // 第二步：按缩放比例加载
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        
        is = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
        is.close();
        
        return bitmap;
        
    } catch (IOException e) {
        e.printStackTrace();
        return null;
    }
}

/**
 * 计算缩放比例
 */
private static int calculateInSampleSize(BitmapFactory.Options options,
                                         int reqWidth, int reqHeight) {
    int width = options.outWidth;
    int height = options.outHeight;
    int inSampleSize = 1;
    
    if (width > reqWidth || height > reqHeight) {
        int halfWidth = width / 2;
        int halfHeight = height / 2;
        
        while ((halfWidth / inSampleSize) >= reqWidth &&
               (halfHeight / inSampleSize) >= reqHeight) {
            inSampleSize *= 2;
        }
    }
    
    return inSampleSize;
}

// 使用示例
// 在EditActivity中加载图片时使用
Uri imageUri = Uri.parse(imageUriString);
currentBitmap = decodeSampledBitmap(imageUri, this, 1080, 1920);
```

### 9.2 处理速度优化

#### 使用RenderScript加速（可选）

```java
/**
 * 使用RenderScript加速图片处理
 * 原理：GPU并行计算，比CPU快10-100倍
 */
public class ImageProcessorRS {
    private RenderScript rs;
    
    public ImageProcessorRS(Context context) {
        rs = RenderScript.create(context);
    }
    
    /**
     * 快速模糊（示例）
     */
    public Bitmap blur(Bitmap input, float radius) {
        Allocation inputAlloc = Allocation.createFromBitmap(rs, input);
        Allocation outputAlloc = Allocation.createTyped(rs, inputAlloc.getType());
        
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(
            rs, Element.U8_4(rs));
        blurScript.setRadius(radius);
        blurScript.setInput(inputAlloc);
        blurScript.forEach(outputAlloc);
        
        Bitmap output = Bitmap.createBitmap(
            input.getWidth(), input.getHeight(), input.getConfig());
        outputAlloc.copyTo(output);
        
        return output;
    }
    
    public void cleanup() {
        rs.destroy();
    }
}
```

### 9.3 UI响应优化

```java
/**
 * 使用Handler防抖，避免频繁处理
 */
private Handler adjustHandler = new Handler();
private Runnable adjustRunnable;

private void previewAdjustmentsDebounced() {
    // 取消之前的任务
    if (adjustRunnable != null) {
        adjustHandler.removeCallbacks(adjustRunnable);
    }
    
    // 延迟100ms执行
    adjustRunnable = () -> previewAdjustments();
    adjustHandler.postDelayed(adjustRunnable, 100);
}

// 在SeekBar监听器中使用
@Override
public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    currentBrightness = progress - 100;
    textBrightnessValue.setText(String.format("%+d", currentBrightness));
    previewAdjustmentsDebounced();  // 使用防抖版本
}
```

---

## 10. 最佳实践

### 10.1 撤销/重做功能

```java
/**
 * 编辑历史管理
 */
public class EditHistory {
    private Stack<Bitmap> undoStack = new Stack<>();
    private Stack<Bitmap> redoStack = new Stack<>();
    private int maxSize = 10;  // 最多保存10步
    
    /**
     * 保存当前状态
     */
    public void pushState(Bitmap bitmap) {
        // 复制Bitmap
        Bitmap copy = bitmap.copy(bitmap.getConfig(), true);
        undoStack.push(copy);
        
        // 限制栈大小
        if (undoStack.size() > maxSize) {
            Bitmap oldest = undoStack.remove(0);
            oldest.recycle();
        }
        
        // 清空重做栈
        clearRedoStack();
    }
    
    /**
     * 撤销
     */
    public Bitmap undo(Bitmap current) {
        if (undoStack.isEmpty()) return null;
        
        // 当前状态压入重做栈
        Bitmap copy = current.copy(current.getConfig(), true);
        redoStack.push(copy);
        
        // 从撤销栈弹出
        return undoStack.pop();
    }
    
    /**
     * 重做
     */
    public Bitmap redo(Bitmap current) {
        if (redoStack.isEmpty()) return null;
        
        // 当前状态压入撤销栈
        Bitmap copy = current.copy(current.getConfig(), true);
        undoStack.push(copy);
        
        // 从重做栈弹出
        return redoStack.pop();
    }
    
    /**
     * 清空所有历史
     */
    public void clear() {
        clearUndoStack();
        clearRedoStack();
    }
    
    private void clearUndoStack() {
        while (!undoStack.isEmpty()) {
            undoStack.pop().recycle();
        }
    }
    
    private void clearRedoStack() {
        while (!redoStack.isEmpty()) {
            redoStack.pop().recycle();
        }
    }
}

// 在EditActivity中使用
private EditHistory editHistory = new EditHistory();

private void applyAdjustments() {
    // 保存当前状态到历史
    editHistory.pushState(currentBitmap);
    
    // 应用调整...
}

private void undo() {
    Bitmap previous = editHistory.undo(currentBitmap);
    if (previous != null) {
        currentBitmap.recycle();
        currentBitmap = previous;
        imageView.setImageBitmap(currentBitmap);
    } else {
        Toast.makeText(this, "无法撤销", Toast.LENGTH_SHORT).show();
    }
}
```

### 10.2 保存调整参数

```java
/**
 * 保存调整参数到项目
 * 原因：用户可以重新打开项目继续调整
 */
public class AdjustParams implements Serializable {
    public int brightness = 0;
    public float contrast = 1.0f;
    public float saturation = 1.0f;
    
    public String toJson() {
        return new Gson().toJson(this);
    }
    
    public static AdjustParams fromJson(String json) {
        return new Gson().fromJson(json, AdjustParams.class);
    }
}

// 在EditProject中添加字段
private String adjustParamsJson;

// 保存参数
AdjustParams params = new AdjustParams();
params.brightness = currentBrightness;
params.contrast = currentContrast;
params.saturation = currentSaturation;
project.setAdjustParamsJson(params.toJson());
projectManager.updateProject(project);

// 加载参数
if (project.getAdjustParamsJson() != null) {
    AdjustParams params = AdjustParams.fromJson(project.getAdjustParamsJson());
    currentBrightness = params.brightness;
    currentContrast = params.contrast;
    currentSaturation = params.saturation;
    updateSeekBars();
}
```

### 10.3 错误处理

```java
/**
 * 安全的图片处理包装
 */
public class SafeImageProcessor {
    
    public static Bitmap processWithFallback(Bitmap source, 
                                             ImageProcessFunction function) {
        try {
            return function.process(source);
        } catch (OutOfMemoryError e) {
            // 内存不足，释放缓存后重试
            System.gc();
            try {
                return function.process(source);
            } catch (Exception e2) {
                Log.e("ImageProcessor", "处理失败", e2);
                return source;  // 返回原图
            }
        } catch (Exception e) {
            Log.e("ImageProcessor", "处理失败", e);
            return source;
        }
    }
    
    @FunctionalInterface
    public interface ImageProcessFunction {
        Bitmap process(Bitmap source) throws Exception;
    }
}

// 使用示例
Bitmap result = SafeImageProcessor.processWithFallback(currentBitmap, 
    bitmap -> ImageProcessor.adjustBrightness(bitmap, 50));
```

---

## 11. 测试建议

### 11.1 测试用例

| 测试项 | 测试步骤 | 预期结果 |
|-------|---------|---------|
| 亮度调整 | 移动滑块到+50 | 图片变亮 |
| 对比度调整 | 移动滑块到1.5 | 对比增强 |
| 饱和度调整 | 移动滑块到0 | 图片变黑白 |
| 重置功能 | 点击重置按钮 | 所有参数归零 |
| 应用调整 | 点击完成按钮 | 效果永久应用 |
| 撤销功能 | 应用后点击撤销 | 恢复上一步 |
| 内存测试 | 处理4K图片 | 不崩溃 |
| 旋转测试 | 连续旋转4次 | 恢复原状 |

### 11.2 性能基准

```
测试设备：普通Android设备
图片大小：1080x1920

期望性能：
- 预览响应：< 16ms（60fps）
- 应用调整：< 500ms
- 裁切处理：< 200ms
- 旋转处理：< 300ms
```

---

## 12. 总结

### 12.1 实现清单

- [ ] 创建ImageProcessor工具类
- [ ] 实现亮度调整算法
- [ ] 实现对比度调整算法
- [ ] 实现饱和度调整算法
- [ ] 实现裁切功能
- [ ] 实现旋转/翻转功能
- [ ] 设计调整面板UI
- [ ] 实现实时预览
- [ ] 实现撤销/重做
- [ ] 添加内存优化
- [ ] 添加性能优化
- [ ] 完整测试

### 12.2 关键知识点

1. **Bitmap像素操作**：ARGB颜色模型、像素遍历
2. **ColorMatrix**：高效的颜色变换，GPU加速
3. **内存管理**：Bitmap回收、按需加载、缩放
4. **UI响应**：实时预览、防抖、后台处理
5. **坐标转换**：屏幕坐标↔图片坐标

### 12.3 进阶方向

- 使用RenderScript GPU加速
- 实现更多滤镜（怀旧、冷暖色调等）
- 添加曲线调整（Curves）
- 实现图层功能
- 添加AI增强（超分辨率、智能美颜等）

---

## 📚 参考资源

- [Android官方文档 - Bitmap](https://developer.android.com/reference/android/graphics/Bitmap)
- [Android官方文档 - ColorMatrix](https://developer.android.com/reference/android/graphics/ColorMatrix)
- [图像处理算法教程](https://en.wikipedia.org/wiki/Digital_image_processing)

---

**准备好开始实现了吗？按照这个教程一步步来，你就能实现完整的图片编辑功能！** 🎨

有任何问题，随时参考这个文档，或者询问我！

