package com.example.photoshop_demo;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.photoshop_demo.filter.*;
import com.example.photoshop_demo.beautify.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 编辑页Activity - 完整版
 * 实现图片裁切、亮度、对比度、饱和度、旋转等功能
 */
public class EditActivity extends AppCompatActivity {

    // 图片状态
    private Bitmap originalBitmap;   // 原始图片
    private Bitmap currentBitmap;    // 当前编辑的图片
    
    // 调整参数（当前值）
    private int currentBrightness = 0;     // -100 to +100
    private float currentContrast = 1.0f;  // 0.5 to 2.0
    private float currentSaturation = 1.0f; // 0.0 to 2.0
    
    // UI组件
    private ImageView imageView;
    private CropOverlayView cropOverlay;
    
    // 面板
    private ViewGroup adjustPanel;
    private ViewGroup cropPanel;
    private ViewGroup rotatePanel;
    private ViewGroup filterPanel;
    private ViewGroup beautifyPanel;
    
    // 调整面板控件
    private SeekBar seekBarBrightness;
    private SeekBar seekBarContrast;
    private SeekBar seekBarSaturation;
    private TextView textBrightnessValue;
    private TextView textContrastValue;
    private TextView textSaturationValue;
    
    // 项目信息
    private String projectId;
    private String imageUriString;
    private boolean isNewProject;
    private ProjectManager projectManager;
    
    // 撤销/重做管理
    private EditHistory editHistory;
    
    // 滤镜相关
    private RecyclerView filterRecyclerView;
    private LutFilterAdapter filterAdapter;
    private SeekBar seekBarFilterIntensity;
    private TextView textFilterIntensity;
    private LutFilter currentFilter;
    private float currentFilterIntensity = 0.8f;
    
    // 美化相关
    private SeekBar seekBarBeautifyIntensity;
    private TextView textBeautifyIntensity;
    private TextView textCurrentEffect;
    private BeautifyEffect currentBeautifyEffect = BeautifyEffect.AUTO_ENHANCE;
    private float currentBeautifyIntensity = 0.8f;
    
    // 防抖Handler
    private Handler previewHandler = new Handler(Looper.getMainLooper());
    private Runnable previewRunnable;
    private Handler filterHandler = new Handler(Looper.getMainLooper());
    private Runnable filterPreviewRunnable;
    private Handler beautifyHandler = new Handler(Looper.getMainLooper());
    private Runnable beautifyPreviewRunnable;
    
    // 当前模式
    private enum EditMode {
        NONE, ADJUST, CROP, ROTATE, FILTER, BEAUTIFY
    }
    private EditMode currentMode = EditMode.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // 初始化
        initViews();
        initHistory();
        getIntentData();
        loadImage();
        setupTopToolbar();
        setupBottomToolbar();
        setupAdjustPanel();
        setupCropPanel();
        setupRotatePanel();
        setupFilterPanel();
        setupBeautifyPanel();
        
        // 预加载LUT
        LutFilterManager.getInstance(this).preloadLuts();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        imageView = findViewById(R.id.image_view);
        cropOverlay = findViewById(R.id.crop_overlay);
        
        adjustPanel = findViewById(R.id.adjust_panel);
        cropPanel = findViewById(R.id.crop_panel);
        rotatePanel = findViewById(R.id.rotate_panel);
        filterPanel = findViewById(R.id.filter_panel);
        beautifyPanel = findViewById(R.id.beautify_panel);
        
        // 调整面板控件
        seekBarBrightness = findViewById(R.id.seekbar_brightness);
        seekBarContrast = findViewById(R.id.seekbar_contrast);
        seekBarSaturation = findViewById(R.id.seekbar_saturation);
        textBrightnessValue = findViewById(R.id.text_brightness_value);
        textContrastValue = findViewById(R.id.text_contrast_value);
        textSaturationValue = findViewById(R.id.text_saturation_value);
        
        projectManager = new ProjectManager(this);
    }
    
    /**
     * 初始化历史管理
     */
    private void initHistory() {
        editHistory = new EditHistory();
    }

    /**
     * 获取Intent传递的数据
     */
    private void getIntentData() {
        projectId = getIntent().getStringExtra("project_id");
        imageUriString = getIntent().getStringExtra("image_uri");
        isNewProject = getIntent().getBooleanExtra("is_new_project", true);
    }

    /**
     * 加载图片
     */
    private void loadImage() {
        if (imageUriString != null) {
            try {
                Uri imageUri = Uri.parse(imageUriString);
                currentBitmap = MediaStore.Images.Media.getBitmap(
                    getContentResolver(), imageUri);
                originalBitmap = currentBitmap.copy(
                    currentBitmap.getConfig() != null ? currentBitmap.getConfig() : Bitmap.Config.ARGB_8888, 
                    true);
                imageView.setImageBitmap(currentBitmap);
                
                // 保存初始状态到历史
                editHistory.pushState(currentBitmap);
                
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "加载图片失败", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "未找到图片", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 设置顶部工具栏
     */
    private void setupTopToolbar() {
        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 撤销按钮
        findViewById(R.id.btn_undo).setOnClickListener(v -> undo());

        // 更多选项
        findViewById(R.id.btn_more).setOnClickListener(v -> 
            Toast.makeText(this, "更多选项", Toast.LENGTH_SHORT).show());

        // AI创意按钮
        findViewById(R.id.btn_ai_create).setOnClickListener(v -> 
            Toast.makeText(this, "AI创意功能开发中", Toast.LENGTH_SHORT).show());

        // 导出按钮
        findViewById(R.id.btn_export).setOnClickListener(v -> exportToGallery());
    }

    /**
     * 设置底部工具栏
     */
    private void setupBottomToolbar() {
        // 裁切按钮
        findViewById(R.id.btn_tool_crop).setOnClickListener(v -> showCropPanel());
        
        // 调整按钮
        findViewById(R.id.btn_tool_adjust).setOnClickListener(v -> showAdjustPanel());
        
        // 旋转按钮
        findViewById(R.id.btn_tool_rotate).setOnClickListener(v -> showRotatePanel());
        
        // 滤镜按钮
        findViewById(R.id.btn_tool_filter).setOnClickListener(v -> showFilterPanel());
        
        // 美化按钮
        findViewById(R.id.btn_tool_beautify).setOnClickListener(v -> showBeautifyPanel());
    }

    /**
     * 设置调整面板
     */
    private void setupAdjustPanel() {
        // 亮度滑块（-100 到 +100）
        seekBarBrightness.setMax(200);
        seekBarBrightness.setProgress(100);
        seekBarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentBrightness = progress - 100;
                textBrightnessValue.setText(String.format("%+d", currentBrightness));
                previewAdjustmentsDebounced();
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // 对比度滑块（0.5 到 2.0）
        seekBarContrast.setMax(200);
        seekBarContrast.setProgress(100);
        seekBarContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentContrast = progress / 100.0f;
                if (currentContrast < 0.5f) currentContrast = 0.5f;
                textContrastValue.setText(String.format("%.2f", currentContrast));
                previewAdjustmentsDebounced();
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // 饱和度滑块（0.0 到 2.0）
        seekBarSaturation.setMax(200);
        seekBarSaturation.setProgress(100);
        seekBarSaturation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentSaturation = progress / 100.0f;
                textSaturationValue.setText(String.format("%.2f", currentSaturation));
                previewAdjustmentsDebounced();
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // 重置按钮
        findViewById(R.id.btn_adjust_reset).setOnClickListener(v -> resetAdjustments());
        
        // 应用按钮
        findViewById(R.id.btn_adjust_apply).setOnClickListener(v -> applyAdjustments());
    }

    /**
     * 设置裁切面板
     */
    private void setupCropPanel() {
        // 取消按钮
        findViewById(R.id.btn_crop_cancel).setOnClickListener(v -> hideCropPanel());
        
        // 完成按钮
        findViewById(R.id.btn_crop_done).setOnClickListener(v -> applyCrop());
        
        // 裁切比例按钮
        findViewById(R.id.btn_crop_free).setOnClickListener(v -> {
            // 自由裁切，不限制比例
            Toast.makeText(this, "自由裁切", Toast.LENGTH_SHORT).show();
        });
        
        findViewById(R.id.btn_crop_1_1).setOnClickListener(v -> {
            setCropRatio(1.0f / 1.0f);  // 1:1 正方形
            Toast.makeText(this, "1:1 比例", Toast.LENGTH_SHORT).show();
        });
        
        findViewById(R.id.btn_crop_4_3).setOnClickListener(v -> {
            setCropRatio(4.0f / 3.0f);  // 4:3
            Toast.makeText(this, "4:3 比例", Toast.LENGTH_SHORT).show();
        });
        
        findViewById(R.id.btn_crop_16_9).setOnClickListener(v -> {
            setCropRatio(16.0f / 9.0f);  // 16:9
            Toast.makeText(this, "16:9 比例", Toast.LENGTH_SHORT).show();
        });
        
        findViewById(R.id.btn_crop_3_2).setOnClickListener(v -> {
            setCropRatio(3.0f / 2.0f);  // 3:2
            Toast.makeText(this, "3:2 比例", Toast.LENGTH_SHORT).show();
        });
    }
    
    /**
     * 设置裁切比例
     * @param ratio 宽高比 (width/height)
     */
    private void setCropRatio(float ratio) {
        if (cropOverlay.getVisibility() != View.VISIBLE) {
            return;
        }
        
        // 获取当前裁切框
        RectF currentRect = cropOverlay.getCropRect();
        
        // 获取cropOverlay的尺寸
        int overlayWidth = cropOverlay.getWidth();
        int overlayHeight = cropOverlay.getHeight();
        
        // 计算中心点
        float centerX = currentRect.centerX();
        float centerY = currentRect.centerY();
        
        // 计算新的裁切框尺寸（保持在View范围内）
        float newWidth, newHeight;
        
        // 先尝试用当前宽度计算高度
        newWidth = currentRect.width();
        newHeight = newWidth / ratio;
        
        // 如果高度超出范围，用高度计算宽度
        if (newHeight > overlayHeight * 0.9f) {
            newHeight = overlayHeight * 0.9f;
            newWidth = newHeight * ratio;
        }
        
        // 如果宽度超出范围，重新计算
        if (newWidth > overlayWidth * 0.9f) {
            newWidth = overlayWidth * 0.9f;
            newHeight = newWidth / ratio;
        }
        
        // 计算新的裁切框位置（以中心点为基准）
        float left = centerX - newWidth / 2;
        float top = centerY - newHeight / 2;
        float right = centerX + newWidth / 2;
        float bottom = centerY + newHeight / 2;
        
        // 确保不超出边界
        if (left < 0) {
            left = 0;
            right = newWidth;
        }
        if (top < 0) {
            top = 0;
            bottom = newHeight;
        }
        if (right > overlayWidth) {
            right = overlayWidth;
            left = overlayWidth - newWidth;
        }
        if (bottom > overlayHeight) {
            bottom = overlayHeight;
            top = overlayHeight - newHeight;
        }
        
        // 设置新的裁切矩形
        RectF newRect = new RectF(left, top, right, bottom);
        cropOverlay.setCropRect(newRect);
    }

    /**
     * 设置旋转面板
     */
    private void setupRotatePanel() {
        // 左转90度
        findViewById(R.id.btn_rotate_left).setOnClickListener(v -> rotateImage(270));
        
        // 右转90度
        findViewById(R.id.btn_rotate_right).setOnClickListener(v -> rotateImage(90));
        
        // 水平翻转
        findViewById(R.id.btn_flip_horizontal).setOnClickListener(v -> flipImage(true));
        
        // 垂直翻转
        findViewById(R.id.btn_flip_vertical).setOnClickListener(v -> flipImage(false));
        
        // 完成按钮
        findViewById(R.id.btn_rotate_close).setOnClickListener(v -> hideRotatePanel());
    }

    /**
     * 显示调整面板
     */
    private void showAdjustPanel() {
        hideAllPanels();
        currentMode = EditMode.ADJUST;
        adjustPanel.setVisibility(View.VISIBLE);
    }

    /**
     * 显示裁切面板
     */
    private void showCropPanel() {
        hideAllPanels();
        currentMode = EditMode.CROP;
        cropPanel.setVisibility(View.VISIBLE);
        cropOverlay.setVisibility(View.VISIBLE);
        
        // 初始化裁切区域
        cropOverlay.post(() -> {
            int width = cropOverlay.getWidth();
            int height = cropOverlay.getHeight();
            float margin = Math.min(width, height) * 0.1f;
            RectF initialRect = new RectF(margin, margin, width - margin, height - margin);
            cropOverlay.setCropRect(initialRect);
        });
    }

    /**
     * 显示旋转面板
     */
    private void showRotatePanel() {
        hideAllPanels();
        currentMode = EditMode.ROTATE;
        rotatePanel.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏所有面板
     */
    private void hideAllPanels() {
        adjustPanel.setVisibility(View.GONE);
        cropPanel.setVisibility(View.GONE);
        rotatePanel.setVisibility(View.GONE);
        filterPanel.setVisibility(View.GONE);
        beautifyPanel.setVisibility(View.GONE);
        cropOverlay.setVisibility(View.GONE);
        
        // 清除预览效果
        imageView.setColorFilter(null);
        imageView.setImageBitmap(currentBitmap);
        
        currentMode = EditMode.NONE;
    }

    /**
     * 隐藏裁切面板
     */
    private void hideCropPanel() {
        cropPanel.setVisibility(View.GONE);
        cropOverlay.setVisibility(View.GONE);
        currentMode = EditMode.NONE;
    }

    /**
     * 隐藏旋转面板
     */
    private void hideRotatePanel() {
        rotatePanel.setVisibility(View.GONE);
        currentMode = EditMode.NONE;
    }

    /**
     * 实时预览调整效果（防抖版本）
     */
    private void previewAdjustmentsDebounced() {
        if (previewRunnable != null) {
            previewHandler.removeCallbacks(previewRunnable);
        }
        
        previewRunnable = this::previewAdjustments;
        previewHandler.postDelayed(previewRunnable, 50);
    }

    /**
     * 实时预览调整效果
     * 使用ColorFilter，不创建新Bitmap，节省内存
     */
    private void previewAdjustments() {
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
        // 检查是否有调整
        if (currentBrightness == 0 && currentContrast == 1.0f && currentSaturation == 1.0f) {
            Toast.makeText(this, "未做任何调整", Toast.LENGTH_SHORT).show();
            hideAllPanels();
            return;
        }
        
        Toast.makeText(this, "正在处理...", Toast.LENGTH_SHORT).show();
        
        // 在后台线程处理
        new Thread(() -> {
            Bitmap adjusted = ImageProcessor.adjustAll(
                currentBitmap,
                currentBrightness,
                currentContrast,
                currentSaturation
            );
            
            runOnUiThread(() -> {
                // 保存当前状态到历史
                editHistory.pushState(currentBitmap);
                
                // 释放旧Bitmap
                if (currentBitmap != originalBitmap && !currentBitmap.isRecycled()) {
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
                hideAllPanels();
                
                Toast.makeText(this, "调整已应用", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    /**
     * 应用裁切
     */
    private void applyCrop() {
        Toast.makeText(this, "正在裁切...", Toast.LENGTH_SHORT).show();
        
        new Thread(() -> {
            // 获取裁切矩形
            RectF cropRect = cropOverlay.getCropRect();
            
            // 将屏幕坐标转换为图片坐标
            RectF imageCropRect = convertToImageCoordinates(cropRect);
            
            // 执行裁切
            Bitmap cropped = ImageProcessor.crop(
                currentBitmap,
                (int)imageCropRect.left,
                (int)imageCropRect.top,
                (int)imageCropRect.width(),
                (int)imageCropRect.height()
            );
            
            runOnUiThread(() -> {
                // 保存当前状态到历史
                editHistory.pushState(currentBitmap);
                
                // 释放旧Bitmap
                if (currentBitmap != originalBitmap && !currentBitmap.isRecycled()) {
                    currentBitmap.recycle();
                }
                
                // 更新当前Bitmap
                currentBitmap = cropped;
                imageView.setImageBitmap(currentBitmap);
                
                // 隐藏裁切面板
                hideCropPanel();
                
                Toast.makeText(this, "裁切完成", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    /**
     * 将屏幕坐标转换为图片坐标
     */
    private RectF convertToImageCoordinates(RectF screenRect) {
        // 获取ImageView的实际显示尺寸和位置
        int viewWidth = imageView.getWidth();
        int viewHeight = imageView.getHeight();
        
        // 获取Bitmap尺寸
        int bitmapWidth = currentBitmap.getWidth();
        int bitmapHeight = currentBitmap.getHeight();
        
        // 计算缩放比例（fitCenter模式）
        float scaleX = (float)bitmapWidth / viewWidth;
        float scaleY = (float)bitmapHeight / viewHeight;
        float scale = Math.max(scaleX, scaleY);
        
        // 计算实际显示尺寸
        float displayWidth = bitmapWidth / scale;
        float displayHeight = bitmapHeight / scale;
        
        // 计算偏移（居中显示）
        float offsetX = (viewWidth - displayWidth) / 2;
        float offsetY = (viewHeight - displayHeight) / 2;
        
        // 转换坐标
        float left = (screenRect.left - offsetX) * scale;
        float top = (screenRect.top - offsetY) * scale;
        float right = (screenRect.right - offsetX) * scale;
        float bottom = (screenRect.bottom - offsetY) * scale;
        
        // 限制在图片范围内
        left = Math.max(0, Math.min(left, bitmapWidth));
        top = Math.max(0, Math.min(top, bitmapHeight));
        right = Math.max(0, Math.min(right, bitmapWidth));
        bottom = Math.max(0, Math.min(bottom, bitmapHeight));
        
        return new RectF(left, top, right, bottom);
    }

    /**
     * 旋转图片
     * @param degrees 旋转角度（90, 180, 270）
     */
    private void rotateImage(int degrees) {
        Toast.makeText(this, "正在旋转...", Toast.LENGTH_SHORT).show();
        
        new Thread(() -> {
            Bitmap rotated;
            if (degrees == 90) {
                rotated = ImageProcessor.rotate90(currentBitmap);
            } else if (degrees == 270) {
                rotated = ImageProcessor.rotate270(currentBitmap);
            } else {
                rotated = ImageProcessor.rotate180(currentBitmap);
            }
            
            runOnUiThread(() -> {
                // 保存当前状态到历史
                editHistory.pushState(currentBitmap);
                
                // 释放旧Bitmap
                if (currentBitmap != originalBitmap && !currentBitmap.isRecycled()) {
                    currentBitmap.recycle();
                }
                
                // 更新当前Bitmap
                currentBitmap = rotated;
                imageView.setImageBitmap(currentBitmap);
                
                Toast.makeText(this, "旋转完成", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    /**
     * 翻转图片
     * @param horizontal true=水平翻转, false=垂直翻转
     */
    private void flipImage(boolean horizontal) {
        Toast.makeText(this, "正在翻转...", Toast.LENGTH_SHORT).show();
        
        new Thread(() -> {
            Bitmap flipped = horizontal ? 
                ImageProcessor.flipHorizontal(currentBitmap) : 
                ImageProcessor.flipVertical(currentBitmap);
            
            runOnUiThread(() -> {
                // 保存当前状态到历史
                editHistory.pushState(currentBitmap);
                
                // 释放旧Bitmap
                if (currentBitmap != originalBitmap && !currentBitmap.isRecycled()) {
                    currentBitmap.recycle();
                }
                
                // 更新当前Bitmap
                currentBitmap = flipped;
                imageView.setImageBitmap(currentBitmap);
                
                Toast.makeText(this, "翻转完成", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    /**
     * 撤销
     */
    private void undo() {
        if (!editHistory.canUndo()) {
            Toast.makeText(this, "无法撤销", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Bitmap previous = editHistory.undo(currentBitmap);
        if (previous != null) {
            if (currentBitmap != originalBitmap && !currentBitmap.isRecycled()) {
                currentBitmap.recycle();
            }
            currentBitmap = previous;
            imageView.setImageBitmap(currentBitmap);
            Toast.makeText(this, "已撤销", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 导出到相册
     */
    private void exportToGallery() {
        if (currentBitmap == null) {
            Toast.makeText(this, "没有可导出的图片", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "正在保存...", Toast.LENGTH_SHORT).show();
        
        new Thread(() -> {
            try {
                String savedPath = saveImageToGallery(currentBitmap);
                updateProjectInfo(savedPath);
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "已保存到相册", Toast.LENGTH_LONG).show();
                    finish();
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> 
                    Toast.makeText(this, "保存失败: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /**
     * 保存图片到相册
     */
    private String saveImageToGallery(Bitmap bitmap) throws IOException {
        String fileName = "Edited_" + System.currentTimeMillis() + ".jpg";
        
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, 
                Environment.DIRECTORY_PICTURES + "/Photoshop_demo");
        }

        Uri imageUri = getContentResolver().insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (imageUri != null) {
            try (OutputStream outputStream = 
                    getContentResolver().openOutputStream(imageUri)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);
                if (outputStream != null) {
                    outputStream.flush();
                }
            }
            return imageUri.toString();
        } else {
            throw new IOException("无法创建文件");
        }
    }

    /**
     * 更新项目信息
     */
    private void updateProjectInfo(String savedPath) {
        if (projectId != null) {
            EditProject project = projectManager.getProject(projectId);
            if (project != null) {
                project.setEditedImagePath(savedPath);
                projectManager.updateProject(project);
            }
        }
    }

    /**
     * 设置滤镜面板
     */
    private void setupFilterPanel() {
        filterRecyclerView = findViewById(R.id.filter_recycler_view);
        seekBarFilterIntensity = findViewById(R.id.seekbar_filter_intensity);
        textFilterIntensity = findViewById(R.id.text_filter_intensity);
        
        // 设置RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false);
        filterRecyclerView.setLayoutManager(layoutManager);
        
        // 创建预览缩略图（使用缩小的图片）
        Bitmap preview = Bitmap.createScaledBitmap(currentBitmap, 200, 200, true);
        
        // 设置适配器
        List<LutFilter> filters = LutFilterManager.getInstance(this).getAllFilters();
        filterAdapter = new LutFilterAdapter(filters, preview);
        filterAdapter.setOnFilterSelectedListener((filter, position) -> {
            currentFilter = filter;
            seekBarFilterIntensity.setProgress(filter.getDefaultIntensity());
            previewFilterDebounced();
        });
        filterRecyclerView.setAdapter(filterAdapter);
        
        // 强度滑块
        seekBarFilterIntensity.setOnSeekBarChangeListener(
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    currentFilterIntensity = progress / 100.0f;
                    textFilterIntensity.setText(progress + "%");
                    previewFilterDebounced();
                }
                
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        
        // 关闭按钮
        findViewById(R.id.btn_filter_close).setOnClickListener(v -> hideFilterPanel());
        
        // 重置按钮
        findViewById(R.id.btn_filter_reset).setOnClickListener(v -> resetFilter());
        
        // 应用按钮
        findViewById(R.id.btn_filter_apply).setOnClickListener(v -> applyFilterToImage());
    }
    
    /**
     * 显示滤镜面板
     */
    private void showFilterPanel() {
        hideAllPanels();
        currentMode = EditMode.FILTER;
        filterPanel.setVisibility(View.VISIBLE);
        
        // 默认选中原图
        currentFilter = LutFilterManager.getInstance(this).getAllFilters().get(0);
    }
    
    /**
     * 隐藏滤镜面板
     */
    private void hideFilterPanel() {
        filterPanel.setVisibility(View.GONE);
        imageView.setImageBitmap(currentBitmap);  // 恢复原图显示
        currentMode = EditMode.NONE;
    }
    
    /**
     * 预览滤镜（防抖）
     */
    private void previewFilterDebounced() {
        if (filterPreviewRunnable != null) {
            filterHandler.removeCallbacks(filterPreviewRunnable);
        }
        filterPreviewRunnable = this::previewFilter;
        filterHandler.postDelayed(filterPreviewRunnable, 50);
    }
    
    /**
     * 预览滤镜效果
     */
    private void previewFilter() {
        if (currentFilter == null) return;
        
        // 原图滤镜直接显示原图
        if (currentFilter.getId().equals("identity")) {
            imageView.setImageBitmap(currentBitmap);
            return;
        }
        
        // 在后台线程生成预览
        new Thread(() -> {
            Bitmap preview = LutFilterManager.getInstance(this)
                .applyFilter(currentBitmap, currentFilter, currentFilterIntensity);
            
            runOnUiThread(() -> {
                imageView.setImageBitmap(preview);
            });
        }).start();
    }
    
    /**
     * 重置滤镜
     */
    private void resetFilter() {
        currentFilter = LutFilterManager.getInstance(this).getAllFilters().get(0);
        currentFilterIntensity = 0.8f;
        seekBarFilterIntensity.setProgress(80);
        filterAdapter.resetSelection();
        imageView.setImageBitmap(currentBitmap);
        Toast.makeText(this, "已重置", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 应用滤镜到实际Bitmap
     */
    private void applyFilterToImage() {
        if (currentFilter == null || currentFilter.getId().equals("identity")) {
            Toast.makeText(this, "未选择滤镜", Toast.LENGTH_SHORT).show();
            hideFilterPanel();
            return;
        }
        
        Toast.makeText(this, "正在处理...", Toast.LENGTH_SHORT).show();
        
        new Thread(() -> {
            Bitmap filtered = LutFilterManager.getInstance(this)
                .applyFilter(currentBitmap, currentFilter, currentFilterIntensity);
            
            runOnUiThread(() -> {
                // 保存到历史
                editHistory.pushState(currentBitmap);
                
                // 释放旧Bitmap
                if (currentBitmap != originalBitmap && !currentBitmap.isRecycled()) {
                    currentBitmap.recycle();
                }
                
                // 更新当前Bitmap
                currentBitmap = filtered;
                imageView.setImageBitmap(currentBitmap);
                
                hideFilterPanel();
                Toast.makeText(this, "滤镜已应用", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    /**
     * 设置美化面板
     */
    private void setupBeautifyPanel() {
        seekBarBeautifyIntensity = findViewById(R.id.seekbar_beautify_intensity);
        textBeautifyIntensity = findViewById(R.id.text_beautify_intensity);
        textCurrentEffect = findViewById(R.id.text_current_effect);
        
        // 3个美化效果按钮
        View btnAutoEnhance = findViewById(R.id.btn_auto_enhance);
        View btnSharpen = findViewById(R.id.btn_sharpen);
        View btnVignette = findViewById(R.id.btn_vignette);
        
        // 设置点击事件
        btnAutoEnhance.setOnClickListener(v -> selectBeautifyEffect(BeautifyEffect.AUTO_ENHANCE, btnAutoEnhance, btnSharpen, btnVignette));
        btnSharpen.setOnClickListener(v -> selectBeautifyEffect(BeautifyEffect.SHARPEN, btnSharpen, btnAutoEnhance, btnVignette));
        btnVignette.setOnClickListener(v -> selectBeautifyEffect(BeautifyEffect.VIGNETTE, btnVignette, btnAutoEnhance, btnSharpen));
        
        // 强度滑块
        seekBarBeautifyIntensity.setOnSeekBarChangeListener(
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    currentBeautifyIntensity = progress / 100.0f;
                    textBeautifyIntensity.setText(progress + "%");
                    previewBeautifyDebounced();
                }
                
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        
        // 关闭按钮
        findViewById(R.id.btn_beautify_close).setOnClickListener(v -> hideBeautifyPanel());
        
        // 重置按钮
        findViewById(R.id.btn_beautify_reset).setOnClickListener(v -> resetBeautify());
        
        // 应用按钮
        findViewById(R.id.btn_beautify_apply).setOnClickListener(v -> applyBeautifyToImage());
    }
    
    /**
     * 显示美化面板
     */
    private void showBeautifyPanel() {
        hideAllPanels();
        currentMode = EditMode.BEAUTIFY;
        beautifyPanel.setVisibility(View.VISIBLE);
        
        // 默认选中自动增强
        currentBeautifyEffect = BeautifyEffect.AUTO_ENHANCE;
        currentBeautifyIntensity = BeautifyEffect.AUTO_ENHANCE.getDefaultIntensity();
        
        // 更新UI
        View btnAutoEnhance = findViewById(R.id.btn_auto_enhance);
        View btnSharpen = findViewById(R.id.btn_sharpen);
        View btnVignette = findViewById(R.id.btn_vignette);
        
        btnAutoEnhance.setSelected(true);
        btnSharpen.setSelected(false);
        btnVignette.setSelected(false);
        
        seekBarBeautifyIntensity.setProgress(currentBeautifyEffect.getDefaultIntensityPercent());
        textCurrentEffect.setText("当前效果：" + currentBeautifyEffect.getName());
        
        // 自动预览
        previewBeautify();
    }
    
    /**
     * 隐藏美化面板
     */
    private void hideBeautifyPanel() {
        beautifyPanel.setVisibility(View.GONE);
        imageView.setImageBitmap(currentBitmap);
        currentMode = EditMode.NONE;
    }
    
    /**
     * 选择美化效果
     */
    private void selectBeautifyEffect(BeautifyEffect effect, View selectedBtn, View... otherBtns) {
        currentBeautifyEffect = effect;
        currentBeautifyIntensity = effect.getDefaultIntensity();
        
        // 更新按钮选中状态
        selectedBtn.setSelected(true);
        for (View btn : otherBtns) {
            btn.setSelected(false);
        }
        
        // 更新滑块和文本
        seekBarBeautifyIntensity.setProgress(effect.getDefaultIntensityPercent());
        textCurrentEffect.setText("当前效果：" + effect.getName());
        
        // 预览效果
        previewBeautify();
    }
    
    /**
     * 预览美化效果（防抖）
     */
    private void previewBeautifyDebounced() {
        if (beautifyPreviewRunnable != null) {
            beautifyHandler.removeCallbacks(beautifyPreviewRunnable);
        }
        beautifyPreviewRunnable = this::previewBeautify;
        beautifyHandler.postDelayed(beautifyPreviewRunnable, 50);
    }
    
    /**
     * 预览美化效果
     */
    private void previewBeautify() {
        if (currentBeautifyEffect == null) return;
        
        // 在后台线程生成预览
        new Thread(() -> {
            Bitmap preview = BeautifyManager.applyEffect(
                currentBitmap, currentBeautifyEffect, currentBeautifyIntensity);
            
            runOnUiThread(() -> {
                imageView.setImageBitmap(preview);
            });
        }).start();
    }
    
    /**
     * 重置美化
     */
    private void resetBeautify() {
        currentBeautifyEffect = BeautifyEffect.AUTO_ENHANCE;
        currentBeautifyIntensity = BeautifyEffect.AUTO_ENHANCE.getDefaultIntensity();
        
        // 重置UI
        View btnAutoEnhance = findViewById(R.id.btn_auto_enhance);
        View btnSharpen = findViewById(R.id.btn_sharpen);
        View btnVignette = findViewById(R.id.btn_vignette);
        
        btnAutoEnhance.setSelected(true);
        btnSharpen.setSelected(false);
        btnVignette.setSelected(false);
        
        seekBarBeautifyIntensity.setProgress(80);
        textCurrentEffect.setText("当前效果：自动增强");
        
        imageView.setImageBitmap(currentBitmap);
        Toast.makeText(this, "已重置", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 应用美化到实际Bitmap
     */
    private void applyBeautifyToImage() {
        if (currentBeautifyEffect == null) {
            Toast.makeText(this, "请选择美化效果", Toast.LENGTH_SHORT).show();
            hideBeautifyPanel();
            return;
        }
        
        Toast.makeText(this, "正在处理...", Toast.LENGTH_SHORT).show();
        
        new Thread(() -> {
            Bitmap beautified = BeautifyManager.applyEffect(
                currentBitmap, currentBeautifyEffect, currentBeautifyIntensity);
            
            runOnUiThread(() -> {
                // 保存到历史
                editHistory.pushState(currentBitmap);
                
                // 释放旧Bitmap
                if (currentBitmap != originalBitmap && !currentBitmap.isRecycled()) {
                    currentBitmap.recycle();
                }
                
                // 更新当前Bitmap
                currentBitmap = beautified;
                imageView.setImageBitmap(currentBitmap);
                
                hideBeautifyPanel();
                Toast.makeText(this, "美化已应用", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 释放Bitmap内存
        if (currentBitmap != null && !currentBitmap.isRecycled()) {
            currentBitmap.recycle();
        }
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
        }
        
        // 清空历史
        if (editHistory != null) {
            editHistory.clear();
        }
        
        // 释放滤镜适配器缓存
        if (filterAdapter != null) {
            filterAdapter.releaseCache();
        }
        
        // 移除Handler回调
        if (previewHandler != null && previewRunnable != null) {
            previewHandler.removeCallbacks(previewRunnable);
        }
        if (filterHandler != null && filterPreviewRunnable != null) {
            filterHandler.removeCallbacks(filterPreviewRunnable);
        }
        if (beautifyHandler != null && beautifyPreviewRunnable != null) {
            beautifyHandler.removeCallbacks(beautifyPreviewRunnable);
        }
    }
}
