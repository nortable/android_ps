package com.example.photoshop_demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.photoshop_demo.filter.LutGeneratorHelper;
import java.util.List;

/**
 * 首页Activity
 * 原因：管理项目列表显示、相册访问、权限请求
 */
public class HomeActivity extends AppCompatActivity {

    // 权限请求启动器
    private ActivityResultLauncher<String> requestPermissionLauncher;
    // 图片选择启动器
    private ActivityResultLauncher<String> pickImageLauncher;
    
    // 项目管理器
    private ProjectManager projectManager;
    // 项目列表适配器
    private ProjectAdapter projectAdapter;
    // RecyclerView
    private RecyclerView projectsRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 初始化项目管理器
        projectManager = new ProjectManager(this);

        // 初始化权限和图片选择器
        setupPermissionLauncher();
        setupImagePicker();

        // 初始化RecyclerView
        setupRecyclerView();

        // 设置功能按钮点击事件
        setupFunctionButtons();
        
        // 设置标签切换
        setupTabs();
        
        // 加载项目列表
        loadProjects();
        
        // 临时：生成LUT文件（仅开发时使用）
        // 注意：运行一次后请注释掉这行代码！
        // LutGeneratorHelper.generateAndSaveAllLuts(this);
    }

    /**
     * 设置权限请求启动器
     * 原因：Android需要在运行时请求敏感权限
     */
    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    // 权限已授予，打开相册
                    openGallery();
                } else {
                    // 权限被拒绝
                    Toast.makeText(this, "需要相册权限才能选择照片", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    /**
     * 设置图片选择器
     * 原因：使用系统提供的图片选择界面
     */
    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    // 选择了图片，创建新项目并跳转到编辑页
                    createAndEditProject(uri);
                }
            }
        );
    }

    /**
     * 设置RecyclerView
     * 原因：显示项目列表
     */
    private void setupRecyclerView() {
        projectsRecycler = findViewById(R.id.projects_recycler);
        projectsRecycler.setLayoutManager(new LinearLayoutManager(this));
        
        projectAdapter = new ProjectAdapter(this, projectManager.getAllProjects());
        projectAdapter.setOnProjectClickListener(project -> {
            // 点击项目，重新编辑
            reopenProject(project);
        });
        
        projectsRecycler.setAdapter(projectAdapter);
    }

    /**
     * 设置功能按钮
     */
    private void setupFunctionButtons() {
        findViewById(R.id.btn_ai_color).setOnClickListener(v -> {
            // AI追色功能 - 也需要选择照片
            checkAndRequestPermission();
        });

        findViewById(R.id.btn_camera).setOnClickListener(v -> {
            // 相机模拟功能
            Toast.makeText(this, "相机功能开发中", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_effects).setOnClickListener(v -> {
            // 特效功能
            Toast.makeText(this, "特效功能开发中", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_collage).setOnClickListener(v -> {
            // 拼图功能 - 选择照片
            checkAndRequestPermission();
        });
        
        // 添加新项目按钮
        findViewById(R.id.btn_add_project).setOnClickListener(v -> {
            checkAndRequestPermission();
        });
    }

    /**
     * 设置标签切换
     */
    private void setupTabs() {
        findViewById(R.id.tab_photos).setOnClickListener(v -> {
            Toast.makeText(this, "照片功能开发中", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.tab_projects).setOnClickListener(v -> {
            // 已经在项目列表页
            loadProjects();
        });
    }

    /**
     * 检查并请求权限
     * 原因：根据Android版本选择正确的权限
     */
    private void checkAndRequestPermission() {
        String permission;
        
        // 根据Android版本选择权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            // Android 12及以下
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        // 检查是否已有权限
        if (ContextCompat.checkSelfPermission(this, permission) 
                == PackageManager.PERMISSION_GRANTED) {
            // 已有权限，直接打开相册
            openGallery();
        } else {
            // 请求权限
            requestPermissionLauncher.launch(permission);
        }
    }

    /**
     * 打开相册选择照片
     */
    private void openGallery() {
        pickImageLauncher.launch("image/*");
    }

    /**
     * 创建新项目并跳转到编辑页
     * 原因：用户选择了照片后，需要创建项目并开始编辑
     */
    private void createAndEditProject(Uri imageUri) {
        // 创建新项目
        EditProject project = projectManager.createProject(imageUri.toString());
        
        // 跳转到编辑页
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("project_id", project.getProjectId());
        intent.putExtra("image_uri", imageUri.toString());
        intent.putExtra("is_new_project", true);
        startActivity(intent);
    }

    /**
     * 重新打开已有项目
     * 原因：用户点击项目列表中的项目，继续编辑
     */
    private void reopenProject(EditProject project) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("project_id", project.getProjectId());
        intent.putExtra("image_uri", project.getOriginalImageUri());
        intent.putExtra("is_new_project", false);
        
        // 如果已有编辑后的图片，也传递过去
        if (project.getEditedImagePath() != null) {
            intent.putExtra("edited_image_path", project.getEditedImagePath());
        }
        
        startActivity(intent);
    }

    /**
     * 加载项目列表
     */
    private void loadProjects() {
        List<EditProject> projects = projectManager.getAllProjects();
        projectAdapter.updateProjects(projects);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从编辑页返回时刷新项目列表
        loadProjects();
    }
}
