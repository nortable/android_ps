package com.example.photoshop_demo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.photoshop_demo.auth.*;

/**
 * 登录页面
 */
public class LoginActivity extends AppCompatActivity {
    
    private EditText editTextUsername;
    private EditText editTextPassword;
    private CheckBox checkBoxRememberMe;
    private Button buttonLogin;
    private TextView textViewRegister;
    private TextView textViewForgotPassword;
    private Button buttonGuestMode;
    
    private UserManager userManager;
    private SessionManager sessionManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // 初始化管理器
        userManager = new UserManager(this);
        sessionManager = new SessionManager(this);
        
        // 检查是否已登录
        if (sessionManager.isLoggedIn()) {
            navigateToHome();
            return;
        }
        
        // 初始化视图
        initViews();
        
        // 设置监听器
        setupListeners();
    }
    
    private void initViews() {
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        checkBoxRememberMe = findViewById(R.id.checkBoxRememberMe);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        buttonGuestMode = findViewById(R.id.buttonGuestMode);
    }
    
    private void setupListeners() {
        // 登录按钮
        buttonLogin.setOnClickListener(v -> handleLogin());
        
        // 注册链接
        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        
        // 忘记密码
        textViewForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "密码重置功能开发中", Toast.LENGTH_SHORT).show();
        });
        
        // 游客模式
        buttonGuestMode.setOnClickListener(v -> {
            navigateToHome();
        });
    }
    
    private void handleLogin() {
        // 获取输入
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        boolean rememberMe = checkBoxRememberMe.isChecked();
        
        // 验证输入
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("请输入用户名");
            editTextUsername.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("请输入密码");
            editTextPassword.requestFocus();
            return;
        }
        
        // 禁用按钮，防止重复点击
        buttonLogin.setEnabled(false);
        buttonLogin.setText("登录中...");
        
        // 执行登录
        Result<User> result = userManager.login(username, password);
        
        if (result.isSuccess()) {
            // 登录成功
            User user = result.getData();
            sessionManager.createSession(user, rememberMe);
            
            Toast.makeText(this, "欢迎回来，" + user.getUsername(), Toast.LENGTH_SHORT).show();
            
            navigateToHome();
        } else {
            // 登录失败
            Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
            
            // 恢复按钮状态
            buttonLogin.setEnabled(true);
            buttonLogin.setText("登 录");
        }
    }
    
    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        // 返回时跳转到首页（允许游客模式）
        navigateToHome();
    }
}

