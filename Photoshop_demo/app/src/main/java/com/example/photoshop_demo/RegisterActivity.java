package com.example.photoshop_demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.photoshop_demo.auth.*;

/**
 * 注册页面
 */
public class RegisterActivity extends AppCompatActivity {
    
    private ImageView buttonBack;
    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextPasswordConfirm;
    private TextView textUsernameStatus;
    private ProgressBar progressBarPasswordStrength;
    private TextView textPasswordStrength;
    private CheckBox checkBoxAgree;
    private Button buttonRegister;
    private TextView textViewLogin;
    
    private UserManager userManager;
    private SessionManager sessionManager;
    private Handler handler = new Handler();
    private Runnable usernameCheckRunnable;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        // 初始化管理器
        userManager = new UserManager(this);
        sessionManager = new SessionManager(this);
        
        // 初始化视图
        initViews();
        
        // 设置监听器
        setupListeners();
    }
    
    private void initViews() {
        buttonBack = findViewById(R.id.buttonBack);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPasswordConfirm = findViewById(R.id.editTextPasswordConfirm);
        textUsernameStatus = findViewById(R.id.textUsernameStatus);
        progressBarPasswordStrength = findViewById(R.id.progressBarPasswordStrength);
        textPasswordStrength = findViewById(R.id.textPasswordStrength);
        checkBoxAgree = findViewById(R.id.checkBoxAgree);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);
    }
    
    private void setupListeners() {
        // 返回按钮
        buttonBack.setOnClickListener(v -> finish());
        
        // 用户名实时检查
        editTextUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 取消之前的检查任务
                if (usernameCheckRunnable != null) {
                    handler.removeCallbacks(usernameCheckRunnable);
                }
                
                // 延迟500ms检查用户名
                usernameCheckRunnable = () -> checkUsernameAvailability(s.toString());
                handler.postDelayed(usernameCheckRunnable, 500);
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // 密码强度实时检查
        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrength(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // 注册按钮
        buttonRegister.setOnClickListener(v -> handleRegister());
        
        // 登录链接
        textViewLogin.setOnClickListener(v -> finish());
    }
    
    private void checkUsernameAvailability(String username) {
        if (TextUtils.isEmpty(username)) {
            textUsernameStatus.setVisibility(View.GONE);
            return;
        }
        
        // 验证格式
        String error = ValidationHelper.getUsernameError(username);
        if (error != null) {
            textUsernameStatus.setText("✗ " + error);
            textUsernameStatus.setTextColor(0xFFFF5252);
            textUsernameStatus.setVisibility(View.VISIBLE);
            return;
        }
        
        // 检查是否已存在
        if (userManager.isUsernameExists(username)) {
            textUsernameStatus.setText("✗ 用户名已存在");
            textUsernameStatus.setTextColor(0xFFFF5252);
            textUsernameStatus.setVisibility(View.VISIBLE);
        } else {
            textUsernameStatus.setText("✓ 用户名可用");
            textUsernameStatus.setTextColor(0xFF4CAF50);
            textUsernameStatus.setVisibility(View.VISIBLE);
        }
    }
    
    private void updatePasswordStrength(String password) {
        if (TextUtils.isEmpty(password)) {
            progressBarPasswordStrength.setProgress(0);
            textPasswordStrength.setText("弱");
            textPasswordStrength.setTextColor(0xFFFF5252);
            return;
        }
        
        PasswordHelper.PasswordStrength strength = PasswordHelper.checkPasswordStrength(password);
        int percentage = PasswordHelper.getPasswordStrengthPercentage(password);
        
        progressBarPasswordStrength.setProgress(percentage);
        textPasswordStrength.setText(strength.getDescription());
        
        // 根据强度设置颜色
        int color;
        switch (strength) {
            case WEAK:
                color = 0xFFFF5252;
                break;
            case MEDIUM:
                color = 0xFFFFC107;
                break;
            case STRONG:
                color = 0xFF4CAF50;
                break;
            case VERY_STRONG:
                color = 0xFF2196F3;
                break;
            default:
                color = 0xFF999999;
        }
        textPasswordStrength.setTextColor(color);
        progressBarPasswordStrength.setProgressTintList(
            android.content.res.ColorStateList.valueOf(color)
        );
    }
    
    private void handleRegister() {
        // 获取输入
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String passwordConfirm = editTextPasswordConfirm.getText().toString().trim();
        boolean agreed = checkBoxAgree.isChecked();
        
        // 验证输入
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("请输入用户名");
            editTextUsername.requestFocus();
            return;
        }
        
        String usernameError = ValidationHelper.getUsernameError(username);
        if (usernameError != null) {
            editTextUsername.setError(usernameError);
            editTextUsername.requestFocus();
            return;
        }
        
        if (userManager.isUsernameExists(username)) {
            editTextUsername.setError("用户名已存在");
            editTextUsername.requestFocus();
            return;
        }
        
        if (!TextUtils.isEmpty(email)) {
            String emailError = ValidationHelper.getEmailError(email);
            if (emailError != null) {
                editTextEmail.setError(emailError);
                editTextEmail.requestFocus();
                return;
            }
        }
        
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("请输入密码");
            editTextPassword.requestFocus();
            return;
        }
        
        Result<Void> passwordValidation = ValidationHelper.validatePasswordStrength(password);
        if (!passwordValidation.isSuccess()) {
            editTextPassword.setError(passwordValidation.getErrorMessage());
            editTextPassword.requestFocus();
            return;
        }
        
        if (!password.equals(passwordConfirm)) {
            editTextPasswordConfirm.setError("两次密码不一致");
            editTextPasswordConfirm.requestFocus();
            return;
        }
        
        if (!agreed) {
            Toast.makeText(this, "请阅读并同意用户协议和隐私政策", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 禁用按钮
        buttonRegister.setEnabled(false);
        buttonRegister.setText("注册中...");
        
        // 执行注册
        String finalEmail = TextUtils.isEmpty(email) ? null : email;
        Result<User> result = userManager.register(username, password, finalEmail);
        
        if (result.isSuccess()) {
            // 注册成功
            User user = result.getData();
            sessionManager.createSession(user, true);
            
            Toast.makeText(this, "注册成功，欢迎加入！", Toast.LENGTH_SHORT).show();
            
            // 跳转到首页
            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            // 注册失败
            Toast.makeText(this, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
            
            // 恢复按钮状态
            buttonRegister.setEnabled(true);
            buttonRegister.setText("注 册");
        }
    }
}

