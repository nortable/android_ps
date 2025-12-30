package com.example.photoshop_demo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.example.photoshop_demo.auth.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 个人中心页面
 */
public class ProfileActivity extends AppCompatActivity {
    
    private ImageView buttonBack;
    private ImageView imageViewAvatar;
    private TextView textViewUsername;
    private TextView textViewEmail;
    private TextView textViewMemberSince;
    private LinearLayout layoutChangePassword;
    private LinearLayout layoutChangeAvatar;
    private LinearLayout layoutBindEmail;
    private SwitchCompat switchAutoLogin;
    private Button buttonLogout;
    
    private UserManager userManager;
    private SessionManager sessionManager;
    private User currentUser;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        // 初始化管理器
        userManager = new UserManager(this);
        sessionManager = new SessionManager(this);
        
        // 检查登录状态
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }
        
        // 获取当前用户
        currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
            return;
        }
        
        // 初始化视图
        initViews();
        
        // 显示用户信息
        displayUserInfo();
        
        // 设置监听器
        setupListeners();
    }
    
    private void initViews() {
        buttonBack = findViewById(R.id.buttonBack);
        imageViewAvatar = findViewById(R.id.imageViewAvatar);
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewMemberSince = findViewById(R.id.textViewMemberSince);
        layoutChangePassword = findViewById(R.id.layoutChangePassword);
        layoutChangeAvatar = findViewById(R.id.layoutChangeAvatar);
        layoutBindEmail = findViewById(R.id.layoutBindEmail);
        switchAutoLogin = findViewById(R.id.switchAutoLogin);
        buttonLogout = findViewById(R.id.buttonLogout);
    }
    
    private void displayUserInfo() {
        // 显示用户名
        textViewUsername.setText(currentUser.getUsername());
        
        // 显示邮箱
        if (TextUtils.isEmpty(currentUser.getEmail())) {
            textViewEmail.setText("未绑定邮箱");
            textViewEmail.setTextColor(0xFF999999);
        } else {
            textViewEmail.setText(currentUser.getEmail());
            textViewEmail.setTextColor(0xFF666666);
        }
        
        // 显示注册时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = sdf.format(new Date(currentUser.getCreatedAt()));
        textViewMemberSince.setText("注册时间：" + dateStr);
        
        // 设置自动登录开关
        switchAutoLogin.setChecked(sessionManager.isAutoLoginEnabled());
    }
    
    private void setupListeners() {
        // 返回按钮
        buttonBack.setOnClickListener(v -> finish());
        
        // 修改密码
        layoutChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        
        // 修改头像
        layoutChangeAvatar.setOnClickListener(v -> {
            Toast.makeText(this, "头像上传功能开发中", Toast.LENGTH_SHORT).show();
        });
        
        // 绑定邮箱
        layoutBindEmail.setOnClickListener(v -> showBindEmailDialog());
        
        // 自动登录开关
        switchAutoLogin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setAutoLoginEnabled(isChecked);
            Toast.makeText(this, isChecked ? "已开启自动登录" : "已关闭自动登录", 
                         Toast.LENGTH_SHORT).show();
        });
        
        // 退出登录
        buttonLogout.setOnClickListener(v -> showLogoutDialog());
    }
    
    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
        
        // 创建自定义对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改密码");
        
        // 创建输入框布局
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        
        final EditText oldPasswordInput = new EditText(this);
        oldPasswordInput.setHint("请输入原密码");
        oldPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | 
                                     android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(oldPasswordInput);
        
        final EditText newPasswordInput = new EditText(this);
        newPasswordInput.setHint("请输入新密码");
        newPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | 
                                     android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPasswordInput);
        
        final EditText confirmPasswordInput = new EditText(this);
        confirmPasswordInput.setHint("确认新密码");
        confirmPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | 
                                         android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(confirmPasswordInput);
        
        builder.setView(layout);
        
        builder.setPositiveButton("确定", (dialog, which) -> {
            String oldPassword = oldPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();
            
            if (TextUtils.isEmpty(oldPassword)) {
                Toast.makeText(this, "请输入原密码", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (TextUtils.isEmpty(newPassword)) {
                Toast.makeText(this, "请输入新密码", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Result<Void> result = userManager.changePassword(
                currentUser.getUserId(), oldPassword, newPassword
            );
            
            if (result.isSuccess()) {
                Toast.makeText(this, "密码修改成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    
    private void showBindEmailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("绑定邮箱");
        
        final EditText emailInput = new EditText(this);
        emailInput.setHint("请输入邮箱地址");
        emailInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setText(currentUser.getEmail());
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        layout.addView(emailInput);
        
        builder.setView(layout);
        
        builder.setPositiveButton("确定", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();
            
            if (!TextUtils.isEmpty(email)) {
                String error = ValidationHelper.getEmailError(email);
                if (error != null) {
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            currentUser.setEmail(email);
            Result<User> result = userManager.updateUser(currentUser);
            
            if (result.isSuccess()) {
                sessionManager.updateSession(currentUser);
                displayUserInfo();
                Toast.makeText(this, "邮箱绑定成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("退出登录")
            .setMessage("确定要退出登录吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                sessionManager.logout();
                Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
                navigateToLogin();
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

