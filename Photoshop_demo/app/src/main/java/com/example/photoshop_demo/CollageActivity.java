package com.example.photoshop_demo;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.photoshop_demo.collage.CollageEngine;
import com.example.photoshop_demo.collage.CollageTemplate;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 拼图Activity
 */
public class CollageActivity extends AppCompatActivity {
    
    private List<Bitmap> images;
    private CollageTemplate currentTemplate;
    private ImageView previewImageView;
    private SeekBar seekBarSpacing;
    private TextView textSpacingValue;
    
    private int currentSpacing = 4;
    private int currentBackgroundColor = Color.WHITE;
    private Bitmap currentPreview;
    
    // 颜色视图
    private View colorWhite, colorBlack, colorGray, colorRed, colorBlue, colorGold;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collage);
        
        // 获取选中的图片
        loadImagesFromIntent();
        
        // 初始化UI
        initViews();
        
        // 设置监听器
        setupListeners();
        
        // 选择默认模板
        selectDefaultTemplate();
        
        // 生成预览
        updatePreview();
    }
    
    /**
     * 从Intent加载图片
     */
    private void loadImagesFromIntent() {
        ArrayList<String> imageUris = getIntent().getStringArrayListExtra("image_uris");
        
        if (imageUris == null || imageUris.isEmpty()) {
            Toast.makeText(this, "未找到图片", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        images = new ArrayList<>();
        for (String uriString : imageUris) {
            try {
                Uri uri = Uri.parse(uriString);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // 缩小图片以提高性能
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 
                    Math.min(bitmap.getWidth(), 1080),
                    Math.min(bitmap.getHeight(), 1080), 
                    true);
                if (scaled != bitmap) {
                    bitmap.recycle();
                }
                images.add(scaled);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        if (images.isEmpty()) {
            Toast.makeText(this, "加载图片失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    /**
     * 初始化视图
     */
    private void initViews() {
        previewImageView = findViewById(R.id.collage_preview);
        seekBarSpacing = findViewById(R.id.seekbar_spacing);
        textSpacingValue = findViewById(R.id.text_spacing_value);
        
        colorWhite = findViewById(R.id.color_white);
        colorBlack = findViewById(R.id.color_black);
        colorGray = findViewById(R.id.color_gray);
        colorRed = findViewById(R.id.color_red);
        colorBlue = findViewById(R.id.color_blue);
        colorGold = findViewById(R.id.color_gold);
    }
    
    /**
     * 设置监听器
     */
    private void setupListeners() {
        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        // 保存按钮
        findViewById(R.id.btn_save).setOnClickListener(v -> saveCollage());
        
        // 边框宽度滑块
        seekBarSpacing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentSpacing = progress;
                textSpacingValue.setText(progress + "px");
                updatePreview();
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // 颜色选择
        colorWhite.setOnClickListener(v -> selectBackgroundColor(Color.WHITE));
        colorBlack.setOnClickListener(v -> selectBackgroundColor(Color.BLACK));
        colorGray.setOnClickListener(v -> selectBackgroundColor(Color.parseColor("#808080")));
        colorRed.setOnClickListener(v -> selectBackgroundColor(Color.RED));
        colorBlue.setOnClickListener(v -> selectBackgroundColor(Color.BLUE));
        colorGold.setOnClickListener(v -> selectBackgroundColor(Color.parseColor("#FFD700")));
    }
    
    /**
     * 选择默认模板
     */
    private void selectDefaultTemplate() {
        List<CollageTemplate> templates = CollageTemplate.getTemplatesForCount(images.size());
        if (!templates.isEmpty()) {
            currentTemplate = templates.get(0);
        }
    }
    
    /**
     * 更新预览
     */
    private void updatePreview() {
        if (currentTemplate == null || images.isEmpty()) {
            return;
        }
        
        new Thread(() -> {
            // 生成拼图（适中尺寸用于预览）
            Bitmap collage = CollageEngine.createCollage(
                images,
                currentTemplate,
                1080,  // 输出宽度
                1080,  // 输出高度
                currentSpacing,
                currentBackgroundColor
            );
            
            // 释放旧预览
            if (currentPreview != null && !currentPreview.isRecycled()) {
                currentPreview.recycle();
            }
            currentPreview = collage;
            
            runOnUiThread(() -> {
                previewImageView.setImageBitmap(collage);
            });
        }).start();
    }
    
    /**
     * 选择背景色
     */
    private void selectBackgroundColor(int color) {
        currentBackgroundColor = color;
        updatePreview();
    }
    
    /**
     * 保存拼图
     */
    private void saveCollage() {
        if (currentPreview == null) {
            Toast.makeText(this, "正在生成拼图，请稍候...", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Toast.makeText(this, "正在保存...", Toast.LENGTH_SHORT).show();
        
        new Thread(() -> {
            try {
                // 生成高质量拼图（更大尺寸）
                Bitmap finalCollage = CollageEngine.createCollage(
                    images,
                    currentTemplate,
                    2160,  // 高质量输出
                    2160,
                    currentSpacing * 2,  // 按比例放大间距
                    currentBackgroundColor
                );
                
                // 保存到相册
                String savedPath = saveImageToGallery(finalCollage);
                
                // 释放
                finalCollage.recycle();
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "拼图已保存到相册", Toast.LENGTH_LONG).show();
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
        String fileName = "Collage_" + System.currentTimeMillis() + ".jpg";
        
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 释放所有Bitmap
        if (images != null) {
            for (Bitmap bitmap : images) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
            images.clear();
        }
        
        if (currentPreview != null && !currentPreview.isRecycled()) {
            currentPreview.recycle();
        }
    }
}

