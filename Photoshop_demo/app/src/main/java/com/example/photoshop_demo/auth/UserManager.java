package com.example.photoshop_demo.auth;

import android.content.Context;
import java.util.UUID;

/**
 * 用户管理器
 * 处理用户注册、登录、信息更新等业务逻辑
 */
public class UserManager {
    
    private Context context;
    private UserDatabase database;
    
    public UserManager(Context context) {
        this.context = context.getApplicationContext();
        this.database = UserDatabase.getInstance(this.context);
    }
    
    /**
     * 用户注册
     */
    public Result<User> register(String username, String password, String email) {
        // 1. 验证用户名
        String usernameError = ValidationHelper.getUsernameError(username);
        if (usernameError != null) {
            return Result.error(Result.ErrorCode.INVALID_USERNAME, usernameError);
        }
        
        // 2. 检查用户名是否已存在
        if (database.isUsernameExists(username)) {
            return Result.error(Result.ErrorCode.USER_EXISTS);
        }
        
        // 3. 验证密码强度
        Result<Void> passwordValidation = ValidationHelper.validatePasswordStrength(password);
        if (!passwordValidation.isSuccess()) {
            return Result.error(passwordValidation.getErrorCode(), passwordValidation.getErrorMessage());
        }
        
        // 4. 验证邮箱（如果提供）
        if (email != null && !email.isEmpty()) {
            String emailError = ValidationHelper.getEmailError(email);
            if (emailError != null) {
                return Result.error(Result.ErrorCode.INVALID_EMAIL, emailError);
            }
        }
        
        // 5. 生成密码哈希
        String passwordHash = PasswordHelper.hashPassword(password);
        if (passwordHash == null) {
            return Result.error(Result.ErrorCode.UNKNOWN_ERROR, "密码加密失败");
        }
        
        // 6. 创建用户对象
        String userId = UUID.randomUUID().toString();
        User user = new User(userId, username, passwordHash);
        user.setEmail(email);
        user.setCreatedAt(System.currentTimeMillis());
        user.setLastLoginAt(System.currentTimeMillis());
        
        // 7. 保存到数据库
        long result = database.insertUser(user);
        if (result == -1) {
            return Result.error(Result.ErrorCode.DATABASE_ERROR, "注册失败，请稍后重试");
        }
        
        // 8. 返回成功
        return Result.success(user);
    }
    
    /**
     * 用户登录
     */
    public Result<User> login(String username, String password) {
        // 1. 验证输入
        if (username == null || username.isEmpty()) {
            return Result.error(Result.ErrorCode.INVALID_USERNAME, "请输入用户名");
        }
        if (password == null || password.isEmpty()) {
            return Result.error(Result.ErrorCode.INVALID_PASSWORD, "请输入密码");
        }
        
        // 2. 查询用户
        User user = database.getUserByUsername(username);
        if (user == null) {
            return Result.error(Result.ErrorCode.USER_NOT_FOUND, "用户不存在");
        }
        
        // 3. 验证密码
        if (!PasswordHelper.verifyPassword(password, user.getPasswordHash())) {
            return Result.error(Result.ErrorCode.INVALID_PASSWORD, "密码错误");
        }
        
        // 4. 检查账号状态
        if (!user.isActive()) {
            return Result.error(Result.ErrorCode.UNKNOWN_ERROR, "账号已被禁用");
        }
        
        // 5. 更新最后登录时间
        long currentTime = System.currentTimeMillis();
        user.setLastLoginAt(currentTime);
        database.updateLastLoginTime(user.getUserId(), currentTime);
        
        // 6. 返回成功
        return Result.success(user);
    }
    
    /**
     * 检查用户名是否存在
     */
    public boolean isUsernameExists(String username) {
        return database.isUsernameExists(username);
    }
    
    /**
     * 根据用户ID获取用户信息
     */
    public User getUserById(String userId) {
        return database.getUserById(userId);
    }
    
    /**
     * 根据用户名获取用户信息
     */
    public User getUserByUsername(String username) {
        return database.getUserByUsername(username);
    }
    
    /**
     * 更新用户信息
     */
    public Result<User> updateUser(User user) {
        int rows = database.updateUser(user);
        if (rows > 0) {
            return Result.success(user);
        } else {
            return Result.error(Result.ErrorCode.DATABASE_ERROR, "更新失败");
        }
    }
    
    /**
     * 修改密码
     */
    public Result<Void> changePassword(String userId, String oldPassword, String newPassword) {
        // 1. 获取用户
        User user = database.getUserById(userId);
        if (user == null) {
            return Result.error(Result.ErrorCode.USER_NOT_FOUND);
        }
        
        // 2. 验证旧密码
        if (!PasswordHelper.verifyPassword(oldPassword, user.getPasswordHash())) {
            return Result.error(Result.ErrorCode.INVALID_PASSWORD, "原密码错误");
        }
        
        // 3. 验证新密码强度
        Result<Void> validation = ValidationHelper.validatePasswordStrength(newPassword);
        if (!validation.isSuccess()) {
            return validation;
        }
        
        // 4. 生成新密码哈希
        String newPasswordHash = PasswordHelper.hashPassword(newPassword);
        if (newPasswordHash == null) {
            return Result.error(Result.ErrorCode.UNKNOWN_ERROR, "密码加密失败");
        }
        
        // 5. 更新密码
        user.setPasswordHash(newPasswordHash);
        int rows = database.updateUser(user);
        
        if (rows > 0) {
            return Result.success(null);
        } else {
            return Result.error(Result.ErrorCode.DATABASE_ERROR, "密码修改失败");
        }
    }
    
    /**
     * 重置密码（忘记密码）
     */
    public Result<Void> resetPassword(String username, String newPassword) {
        // 1. 查询用户
        User user = database.getUserByUsername(username);
        if (user == null) {
            return Result.error(Result.ErrorCode.USER_NOT_FOUND);
        }
        
        // 2. 验证新密码强度
        Result<Void> validation = ValidationHelper.validatePasswordStrength(newPassword);
        if (!validation.isSuccess()) {
            return validation;
        }
        
        // 3. 生成新密码哈希
        String newPasswordHash = PasswordHelper.hashPassword(newPassword);
        if (newPasswordHash == null) {
            return Result.error(Result.ErrorCode.UNKNOWN_ERROR, "密码加密失败");
        }
        
        // 4. 更新密码
        user.setPasswordHash(newPasswordHash);
        int rows = database.updateUser(user);
        
        if (rows > 0) {
            return Result.success(null);
        } else {
            return Result.error(Result.ErrorCode.DATABASE_ERROR, "密码重置失败");
        }
    }
}

