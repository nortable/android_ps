package com.example.photoshop_demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.example.photoshop_demo.filter.LutGeneratorHelper;
import com.example.photoshop_demo.auth.SessionManager;
import com.example.photoshop_demo.auth.User;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.PopupMenu;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 首页Activity
 * 原因：管理项目列表显示、相册访问、权限请求
 */
public class HomeActivity extends AppCompatActivity {

    // 权限请求启动器
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    
    // 图片选择启动器
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Intent> pickMultipleImagesLauncher;
    
    // 相机启动器
    private ActivityResultLauncher<Intent> cameraLauncher;
    private Uri photoUri;  // 拍照后的图片URI
    
    // 项目管理器
    private ProjectManager projectManager;
    // 项目列表适配器
    private ProjectAdapter projectAdapter;
    // RecyclerView
    private RecyclerView projectsRecycler;
    
    // 用户会话管理器
    private SessionManager sessionManager;
    private LinearLayout layoutUserStatus;
    private TextView textUsername;
    
    // 当前操作类型
    private enum ActionType {
        EDIT_IMAGE, TAKE_PHOTO, COLLAGE
    }
    private ActionType currentAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 初始化项目管理器
        projectManager = new ProjectManager(this);
        
        // 初始化会话管理器
        sessionManager = new SessionManager(this);

        // 初始化用户状态UI
        setupUserStatus();

        // 初始化权限和图片选择器
        setupPermissionLauncher();
        setupImagePicker();
        setupCameraLauncher();
        setupMultipleImagePicker();

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
        // 相册权限
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    // 权限已授予，根据操作类型执行相应动作
                    if (currentAction == ActionType.COLLAGE) {
                        openMultipleImagePicker();
                    } else {
                        openGallery();
                    }
                } else {
                    // 权限被拒绝
                    Toast.makeText(this, "需要相册权限才能选择照片", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        // 相机权限
        requestCameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    // 权限已授予，启动相机
                    launchCamera();
                } else {
                    // 权限被拒绝
                    Toast.makeText(this, "需要相机权限才能拍照", 
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
     * 设置多图选择器（用于拼图）
     */
    private void setupMultipleImagePicker() {
        pickMultipleImagesLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    ArrayList<String> imageUris = new ArrayList<>();
                    
                    // 处理多选图片
                    if (data.getClipData() != null) {
                        // 多张图片
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            imageUris.add(uri.toString());
                        }
                    } else if (data.getData() != null) {
                        // 单张图片
                        imageUris.add(data.getData().toString());
                    }
                    
                    // 检查图片数量
                    if (imageUris.size() < 2) {
                        Toast.makeText(this, "请至少选择2张图片", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (imageUris.size() > 9) {
                        Toast.makeText(this, "最多选择9张图片", Toast.LENGTH_SHORT).show();
                        imageUris = new ArrayList<>(imageUris.subList(0, 9));
                    }
                    
                    // 跳转到拼图页面
                    openCollageActivity(imageUris);
                }
            }
        );
    }
    
    /**
     * 设置相机启动器
     */
    private void setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // 拍照成功，跳转到编辑页
                    createAndEditProject(photoUri);
                } else {
                    Toast.makeText(this, "拍照取消", Toast.LENGTH_SHORT).show();
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
        
        // 设置点击监听
        projectAdapter.setOnProjectClickListener(project -> {
            // 点击项目，重新编辑
            reopenProject(project);
        });
        
        // 设置删除监听
        projectAdapter.setOnProjectDeleteListener((project, position) -> {
            handleProjectDelete(project, position);
        });
        
        projectsRecycler.setAdapter(projectAdapter);
        
        // 附加滑动删除功能
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
            new SwipeToDeleteCallback(this, projectAdapter));
        itemTouchHelper.attachToRecyclerView(projectsRecycler);
    }

    /**
     * 设置功能按钮
     */
    private void setupFunctionButtons() {
        // 按钮1: 图片编辑
        findViewById(R.id.btn_edit_image).setOnClickListener(v -> {
            currentAction = ActionType.EDIT_IMAGE;
            checkAndRequestPermission();
        });

        // 按钮2: 拍照
        findViewById(R.id.btn_take_photo).setOnClickListener(v -> {
            currentAction = ActionType.TAKE_PHOTO;
            checkCameraPermissionAndLaunch();
        });

        // 按钮3: 拼图
        findViewById(R.id.btn_collage).setOnClickListener(v -> {
            currentAction = ActionType.COLLAGE;
            checkAndRequestPermission();
        });
        
        // 添加新项目按钮
        findViewById(R.id.btn_add_project).setOnClickListener(v -> {
            currentAction = ActionType.EDIT_IMAGE;
            checkAndRequestPermission();
        });
    }

    /**
     * 设置标签切换
     */
    private void setupTabs() {
        // 只保留项目标签（照片功能已删除）
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
            // 已有权限，根据操作类型执行相应动作
            if (currentAction == ActionType.COLLAGE) {
                openMultipleImagePicker();
            } else {
                openGallery();
            }
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
     * 打开多图选择器（用于拼图）
     */
    private void openMultipleImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pickMultipleImagesLauncher.launch(intent);
    }
    
    /**
     * 检查相机权限并启动相机
     */
    private void checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED) {
            // 已有权限，直接启动相机
            launchCamera();
        } else {
            // 请求相机权限
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }
    
    /**
     * 启动相机
     */
    private void launchCamera() {
        // 创建临时文件
        File photoFile = createImageFile();
        if (photoFile != null) {
            // 使用FileProvider获取URI（Android 7.0+）
            photoUri = FileProvider.getUriForFile(this,
                "com.example.photoshop_demo.fileprovider",
                photoFile);
            
            // 启动相机Intent
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            cameraLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(this, "无法创建图片文件", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 创建图片文件
     */
    private File createImageFile() {
        try {
            // 创建文件名
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
            String imageFileName = "PHOTO_" + timeStamp + ".jpg";
            
            // 在app私有目录创建文件
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            return new File(storageDir, imageFileName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
     * 打开拼图页面
     */
    private void openCollageActivity(ArrayList<String> imageUris) {
        Intent intent = new Intent(this, CollageActivity.class);
        intent.putStringArrayListExtra("image_uris", imageUris);
        startActivity(intent);
    }
    
    /**
     * 处理项目删除
     */
    private void handleProjectDelete(EditProject project, int position) {
        // 从ProjectManager删除记录
        projectManager.deleteProject(project.getProjectId());
        
        // 显示Snackbar with 撤销选项
        Snackbar.make(projectsRecycler, "已删除项目", Snackbar.LENGTH_LONG)
            .setAction("撤销", v -> {
                // 恢复项目
                projectAdapter.restoreProject(project, position);
                projectManager.restoreProject(project);
                Toast.makeText(this, "已恢复项目", Toast.LENGTH_SHORT).show();
            })
            .setActionTextColor(getColor(R.color.yellow))
            .show();
    }
    
    /**
     * 初始化用户状态UI
     */
    private void setupUserStatus() {
        layoutUserStatus = findViewById(R.id.layout_user_status);
        textUsername = findViewById(R.id.text_username);
        
        // 更新用户状态显示
        updateUserStatus();
        
        // 设置点击监听
        layoutUserStatus.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                // 已登录，显示用户菜单
                showUserMenu();
            } else {
                // 未登录，跳转到登录页
                navigateToLogin();
            }
        });
    }
    
    /**
     * 更新用户状态显示
     */
    private void updateUserStatus() {
        if (sessionManager.isLoggedIn()) {
            User currentUser = sessionManager.getCurrentUser();
            if (currentUser != null) {
                textUsername.setText(currentUser.getUsername());
            }
        } else {
            textUsername.setText("登录");
        }
    }
    
    /**
     * 显示用户菜单
     */
    private void showUserMenu() {
        PopupMenu popup = new PopupMenu(this, layoutUserStatus);
        
        // 添加菜单项
        popup.getMenu().add(0, 1, 1, "个人中心");
        popup.getMenu().add(0, 2, 2, "退出登录");
        
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == 1) {
                // 跳转到个人中心
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == 2) {
                // 退出登录
                sessionManager.logout();
                updateUserStatus();
                Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        
        popup.show();
    }
    
    /**
     * 跳转到登录页
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
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
        // 页面恢复时更新用户状态
        updateUserStatus();
    }
}
