package com.example.photoshop_demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 裁切遮罩层View
 * 实现交互式裁切功能
 */
public class CropOverlayView extends View {
    
    private RectF cropRect;  // 裁切矩形
    private Paint cropPaint;  // 裁切框画笔
    private Paint dimPaint;   // 半透明背景画笔
    private Paint clearPaint; // 清除画笔
    private Paint gridPaint;  // 九宫格线画笔
    private Paint cornerPaint; // 角点画笔
    
    // 触摸状态
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_MOVE = 1;
    private static final int TOUCH_TOP_LEFT = 2;
    private static final int TOUCH_TOP_RIGHT = 3;
    private static final int TOUCH_BOTTOM_LEFT = 4;
    private static final int TOUCH_BOTTOM_RIGHT = 5;
    
    private int touchMode = TOUCH_NONE;
    private float lastX, lastY;
    
    // 配置参数
    private static final float CORNER_SIZE = 60f;  // 角点触摸区域大小
    private static final float MIN_CROP_SIZE = 100f; // 最小裁切尺寸
    private static final float CORNER_LINE_WIDTH = 4f; // 角点线宽
    private static final float CORNER_LINE_LENGTH = 30f; // 角点线长度
    
    public CropOverlayView(Context context) {
        super(context);
        init();
    }
    
    public CropOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public CropOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    /**
     * 初始化画笔
     */
    private void init() {
        // 关闭硬件加速，使PorterDuff模式正常工作
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        
        // 裁切框画笔（白色边框）
        cropPaint = new Paint();
        cropPaint.setColor(Color.WHITE);
        cropPaint.setStyle(Paint.Style.STROKE);
        cropPaint.setStrokeWidth(3f);
        cropPaint.setAntiAlias(true);
        
        // 半透明背景画笔（60%黑色）
        dimPaint = new Paint();
        dimPaint.setColor(0x99000000);
        dimPaint.setStyle(Paint.Style.FILL);
        
        // 清除画笔（用于清除裁切区域的半透明背景）
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        clearPaint.setAntiAlias(true);
        
        // 九宫格线画笔
        gridPaint = new Paint();
        gridPaint.setColor(0x88FFFFFF); // 半透明白色
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setAntiAlias(true);
        
        // 角点画笔
        cornerPaint = new Paint();
        cornerPaint.setColor(Color.WHITE);
        cornerPaint.setStyle(Paint.Style.STROKE);
        cornerPaint.setStrokeWidth(CORNER_LINE_WIDTH);
        cornerPaint.setStrokeCap(Paint.Cap.ROUND);
        cornerPaint.setAntiAlias(true);
        
        // 初始化裁切矩形（默认居中80%）
        cropRect = new RectF();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // 如果cropRect还没有初始化，设置默认值
        if (cropRect.isEmpty()) {
            float margin = Math.min(w, h) * 0.1f;
            cropRect.set(margin, margin, w - margin, h - margin);
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (cropRect.isEmpty()) {
            return;
        }
        
        // 1. 绘制半透明背景（整个View）
        canvas.drawRect(0, 0, getWidth(), getHeight(), dimPaint);
        
        // 2. 清除裁切区域的半透明背景（显示原图）
        canvas.drawRect(cropRect, clearPaint);
        
        // 3. 绘制裁切框
        canvas.drawRect(cropRect, cropPaint);
        
        // 4. 绘制九宫格线
        drawGrid(canvas);
        
        // 5. 绘制四个角的控制点
        drawCorners(canvas);
    }
    
    /**
     * 绘制九宫格线
     */
    private void drawGrid(Canvas canvas) {
        float width = cropRect.width();
        float height = cropRect.height();
        
        // 绘制两条竖线
        float x1 = cropRect.left + width / 3;
        float x2 = cropRect.left + width * 2 / 3;
        canvas.drawLine(x1, cropRect.top, x1, cropRect.bottom, gridPaint);
        canvas.drawLine(x2, cropRect.top, x2, cropRect.bottom, gridPaint);
        
        // 绘制两条横线
        float y1 = cropRect.top + height / 3;
        float y2 = cropRect.top + height * 2 / 3;
        canvas.drawLine(cropRect.left, y1, cropRect.right, y1, gridPaint);
        canvas.drawLine(cropRect.left, y2, cropRect.right, y2, gridPaint);
    }
    
    /**
     * 绘制四个角的控制点（L形）
     */
    private void drawCorners(Canvas canvas) {
        // 左上角
        canvas.drawLine(cropRect.left, cropRect.top, 
            cropRect.left + CORNER_LINE_LENGTH, cropRect.top, cornerPaint);
        canvas.drawLine(cropRect.left, cropRect.top, 
            cropRect.left, cropRect.top + CORNER_LINE_LENGTH, cornerPaint);
        
        // 右上角
        canvas.drawLine(cropRect.right, cropRect.top, 
            cropRect.right - CORNER_LINE_LENGTH, cropRect.top, cornerPaint);
        canvas.drawLine(cropRect.right, cropRect.top, 
            cropRect.right, cropRect.top + CORNER_LINE_LENGTH, cornerPaint);
        
        // 左下角
        canvas.drawLine(cropRect.left, cropRect.bottom, 
            cropRect.left + CORNER_LINE_LENGTH, cropRect.bottom, cornerPaint);
        canvas.drawLine(cropRect.left, cropRect.bottom, 
            cropRect.left, cropRect.bottom - CORNER_LINE_LENGTH, cornerPaint);
        
        // 右下角
        canvas.drawLine(cropRect.right, cropRect.bottom, 
            cropRect.right - CORNER_LINE_LENGTH, cropRect.bottom, cornerPaint);
        canvas.drawLine(cropRect.right, cropRect.bottom, 
            cropRect.right, cropRect.bottom - CORNER_LINE_LENGTH, cornerPaint);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchMode = detectTouchMode(x, y);
                lastX = x;
                lastY = y;
                return touchMode != TOUCH_NONE;
                
            case MotionEvent.ACTION_MOVE:
                if (touchMode != TOUCH_NONE) {
                    float dx = x - lastX;
                    float dy = y - lastY;
                    adjustCropRect(dx, dy);
                    lastX = x;
                    lastY = y;
                    invalidate();
                    return true;
                }
                break;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touchMode = TOUCH_NONE;
                break;
        }
        
        return super.onTouchEvent(event);
    }
    
    /**
     * 检测触摸模式（点击的是哪个角或者中间区域）
     */
    private int detectTouchMode(float x, float y) {
        // 检测四个角
        if (isInCorner(x, y, cropRect.left, cropRect.top)) {
            return TOUCH_TOP_LEFT;
        }
        if (isInCorner(x, y, cropRect.right, cropRect.top)) {
            return TOUCH_TOP_RIGHT;
        }
        if (isInCorner(x, y, cropRect.left, cropRect.bottom)) {
            return TOUCH_BOTTOM_LEFT;
        }
        if (isInCorner(x, y, cropRect.right, cropRect.bottom)) {
            return TOUCH_BOTTOM_RIGHT;
        }
        
        // 检测是否在裁切区域内（移动整个框）
        if (cropRect.contains(x, y)) {
            return TOUCH_MOVE;
        }
        
        return TOUCH_NONE;
    }
    
    /**
     * 判断点是否在角点区域内
     */
    private boolean isInCorner(float touchX, float touchY, float cornerX, float cornerY) {
        return Math.abs(touchX - cornerX) < CORNER_SIZE && 
               Math.abs(touchY - cornerY) < CORNER_SIZE;
    }
    
    /**
     * 根据触摸模式调整裁切矩形
     */
    private void adjustCropRect(float dx, float dy) {
        RectF newRect = new RectF(cropRect);
        
        switch (touchMode) {
            case TOUCH_MOVE:
                // 移动整个框
                newRect.offset(dx, dy);
                // 限制在View范围内
                if (newRect.left < 0) newRect.offset(-newRect.left, 0);
                if (newRect.top < 0) newRect.offset(0, -newRect.top);
                if (newRect.right > getWidth()) newRect.offset(getWidth() - newRect.right, 0);
                if (newRect.bottom > getHeight()) newRect.offset(0, getHeight() - newRect.bottom);
                break;
                
            case TOUCH_TOP_LEFT:
                newRect.left += dx;
                newRect.top += dy;
                break;
                
            case TOUCH_TOP_RIGHT:
                newRect.right += dx;
                newRect.top += dy;
                break;
                
            case TOUCH_BOTTOM_LEFT:
                newRect.left += dx;
                newRect.bottom += dy;
                break;
                
            case TOUCH_BOTTOM_RIGHT:
                newRect.right += dx;
                newRect.bottom += dy;
                break;
        }
        
        // 验证新矩形是否有效
        if (isValidRect(newRect)) {
            cropRect.set(newRect);
        }
    }
    
    /**
     * 验证矩形是否有效（不能太小，不能超出边界）
     */
    private boolean isValidRect(RectF rect) {
        return rect.width() >= MIN_CROP_SIZE && 
               rect.height() >= MIN_CROP_SIZE &&
               rect.left >= 0 && 
               rect.top >= 0 &&
               rect.right <= getWidth() && 
               rect.bottom <= getHeight();
    }
    
    /**
     * 设置裁切矩形
     */
    public void setCropRect(RectF rect) {
        if (rect != null) {
            cropRect.set(rect);
            invalidate();
        }
    }
    
    /**
     * 获取裁切矩形
     */
    public RectF getCropRect() {
        return new RectF(cropRect);
    }
    
    /**
     * 重置为默认裁切区域（居中80%）
     */
    public void reset() {
        float margin = Math.min(getWidth(), getHeight()) * 0.1f;
        cropRect.set(margin, margin, getWidth() - margin, getHeight() - margin);
        invalidate();
    }
}

