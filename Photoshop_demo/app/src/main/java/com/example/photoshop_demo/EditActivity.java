package com.example.photoshop_demo;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 编辑页Activity
 * 原因：加载图片、进行编辑、保存到相册
 */
public class EditActivity extends AppCompatActivity {

    private ImageView imageView;
    private Bitmap currentBitmap;  // 当前正在编辑的图片
    private Bitmap originalBitmap; // 原始图片（用于撤销）
    
    private String projectId;
    private String imageUriString;
    private boolean isNewProject;
    
    private ProjectManager projectManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // 初始化视图
        imageView = findViewById(R.id.image_view);
        
        // 初始化项目管理器
        projectManager = new ProjectManager(this);

        // 获取Intent传递的数据
        getIntentData();
        
        // 加载图片
        loadImage();

        // 设置按钮点击事件
        setupButtons();
    }

    /**
     * 获取Intent传递的数据
     * 原因：需要知道是新项目还是重新编辑
     */
    private void getIntentData() {
        projectId = getIntent().getStringExtra("project_id");
        imageUriString = getIntent().getStringExtra("image_uri");
        isNewProject = getIntent().getBooleanExtra("is_new_project", true);
        
        // 如果有已编辑的图片路径，优先加载
        String editedImagePath = getIntent().getStringExtra("edited_image_path");
        if (editedImagePath != null && !editedImagePath.isEmpty()) {
            // TODO: 加载已编辑的图片
        }
    }

    /**
     * 加载图片
     * 原因：从URI加载图片到ImageView
     */
    private void loadImage() {
        if (imageUriString != null) {
            try {
                Uri imageUri = Uri.parse(imageUriString);
                
                // 使用MediaStore加载图片
                currentBitmap = MediaStore.Images.Media.getBitmap(
                    getContentResolver(), imageUri);
                
                // 保存原始图片副本（用于撤销）
                originalBitmap = currentBitmap.copy(currentBitmap.getConfig(), true);
                
                // 显示图片
                imageView.setImageBitmap(currentBitmap);
                
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
     * 设置按钮点击事件
     */
    private void setupButtons() {
        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> {
            finish();
        });

        // 撤销按钮
        findViewById(R.id.btn_undo).setOnClickListener(v -> {
            undoEdit();
        });

        // 更多选项按钮
        findViewById(R.id.btn_more).setOnClickListener(v -> {
            Toast.makeText(this, "更多选项", Toast.LENGTH_SHORT).show();
        });

        // AI创意按钮
        findViewById(R.id.btn_ai_create).setOnClickListener(v -> {
            Toast.makeText(this, "AI创意功能开发中", Toast.LENGTH_SHORT).show();
        });

        // 导出按钮 - 保存到相册
        findViewById(R.id.btn_export).setOnClickListener(v -> {
            exportToGallery();
        });

        // 底部工具栏按钮
        findViewById(R.id.btn_tool_color_adjust).setOnClickListener(v -> {
            Toast.makeText(this, "AI色彩调节功能开发中", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_tool_ai_color).setOnClickListener(v -> {
            applyGrayscaleFilter();  // 示例：应用黑白滤镜
        });

        findViewById(R.id.btn_tool_filter).setOnClickListener(v -> {
            Toast.makeText(this, "滤镜功能开发中", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_tool_balance).setOnClickListener(v -> {
            Toast.makeText(this, "白平衡功能开发中", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_tool_shadow).setOnClickListener(v -> {
            Toast.makeText(this, "影调功能开发中", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * 撤销编辑
     * 原因：恢复到原始图片
     */
    private void undoEdit() {
        if (originalBitmap != null) {
            currentBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
            imageView.setImageBitmap(currentBitmap);
            Toast.makeText(this, "已撤销", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 应用黑白滤镜（示例）
     * 原因：展示图片编辑功能
     */
    private void applyGrayscaleFilter() {
        if (currentBitmap == null) return;

        Bitmap newBitmap = currentBitmap.copy(currentBitmap.getConfig(), true);
        
        for (int x = 0; x < newBitmap.getWidth(); x++) {
            for (int y = 0; y < newBitmap.getHeight(); y++) {
                int pixel = newBitmap.getPixel(x, y);
                
                int alpha = (pixel >> 24) & 0xff;
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                
                // 计算灰度值
                int gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
                
                // 设置新的像素值
                int newPixel = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
                newBitmap.setPixel(x, y, newPixel);
            }
        }
        
        currentBitmap = newBitmap;
        imageView.setImageBitmap(currentBitmap);
        Toast.makeText(this, "已应用黑白滤镜", Toast.LENGTH_SHORT).show();
    }

    /**
     * 导出到相册
     * 原因：保存编辑后的图片，让用户可以在相册中看到
     */
    private void exportToGallery() {
        if (currentBitmap == null) {
            Toast.makeText(this, "没有可导出的图片", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                String savedPath = saveImageToGallery(currentBitmap);
                
                // 更新项目信息
                updateProjectInfo(savedPath);
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "已保存到相册", Toast.LENGTH_LONG).show();
                    finish(); // 返回首页
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "保存失败: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * 保存图片到相册（兼容Android 10+）
     * 原因：使用MediaStore API保存到系统相册
     */
    private String saveImageToGallery(Bitmap bitmap) throws IOException {
        // 生成文件名
        String fileName = "Edited_" + System.currentTimeMillis() + ".jpg";
        
        // 准备ContentValues
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        
        // Android 10+ 使用相对路径
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, 
                Environment.DIRECTORY_PICTURES + "/Photoshop_demo");
        }

        // 插入到MediaStore
        Uri imageUri = getContentResolver().insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (imageUri != null) {
            try (OutputStream outputStream = 
                    getContentResolver().openOutputStream(imageUri)) {
                // 保存图片（质量95）
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);
                outputStream.flush();
            }
            
            // 返回保存的路径（实际上是URI字符串）
            return imageUri.toString();
        } else {
            throw new IOException("无法创建文件");
        }
    }

    /**
     * 更新项目信息
     * 原因：保存编辑后的图片路径到项目中
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
    }
}
