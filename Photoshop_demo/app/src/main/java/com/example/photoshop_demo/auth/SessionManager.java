package com.example.photoshop_demo.auth;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 会话管理器
 * 处理用户登录状态、会话保持、自动登录等
 */
public class SessionManager {
    
    private static final String PREF_NAME = "user_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_SESSION_TOKEN = "session_token";
    private static final String KEY_SESSION_CREATED_AT = "session_created_at";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_AUTO_LOGIN = "auto_login_enabled";
    
    private Context context;
    private SharedPreferences prefs;
    private UserManager userManager;
    
    public SessionManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.userManager = new UserManager(context);
    }
    
    /**
     * 创建会话（登录成功后调用）
     */
    public void createSession(User user, boolean rememberMe) {
        SharedPreferences.Editor editor = prefs.edit();
        
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_SESSION_TOKEN, generateSessionToken(user));
        editor.putLong(KEY_SESSION_CREATED_AT, System.currentTimeMillis());
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.putBoolean(KEY_AUTO_LOGIN, rememberMe);
        
        editor.apply();
    }
    
    /**
     * 检查是否已登录
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * 获取当前用户
     */
    public User getCurrentUser() {
        if (!isLoggedIn()) {
            return null;
        }
        
        String userId = prefs.getString(KEY_USER_ID, null);
        if (userId == null) {
            return null;
        }
        
        // 从数据库获取最新用户信息
        return userManager.getUserById(userId);
    }
    
    /**
     * 获取当前用户ID
     */
    public String getCurrentUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }
    
    /**
     * 获取当前用户名
     */
    public String getCurrentUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }
    
    /**
     * 注销登录
     */
    public void logout() {
        SharedPreferences.Editor editor = prefs.edit();
        
        // 清除所有会话信息
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_SESSION_TOKEN);
        editor.remove(KEY_SESSION_CREATED_AT);
        
        // 保留自动登录设置（用户下次可以选择）
        // editor.remove(KEY_REMEMBER_ME);
        // editor.remove(KEY_AUTO_LOGIN);
        
        editor.apply();
    }
    
    /**
     * 恢复会话（应用启动时检查自动登录）
     */
    public User restoreSession() {
        if (!isLoggedIn()) {
            return null;
        }
        
        // 检查是否启用自动登录
        boolean autoLogin = prefs.getBoolean(KEY_AUTO_LOGIN, false);
        if (!autoLogin) {
            return null;
        }
        
        // 检查会话是否过期（30天）
        long sessionCreatedAt = prefs.getLong(KEY_SESSION_CREATED_AT, 0);
        long currentTime = System.currentTimeMillis();
        long sessionAge = currentTime - sessionCreatedAt;
        long maxSessionAge = 30L * 24 * 60 * 60 * 1000; // 30天
        
        if (sessionAge > maxSessionAge) {
            // 会话已过期，清除登录状态
            logout();
            return null;
        }
        
        // 返回当前用户
        return getCurrentUser();
    }
    
    /**
     * 更新会话信息（用户信息变更后）
     */
    public void updateSession(User user) {
        if (!isLoggedIn()) {
            return;
        }
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.apply();
    }
    
    /**
     * 检查是否启用了"记住我"
     */
    public boolean isRememberMeEnabled() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }
    
    /**
     * 设置自动登录
     */
    public void setAutoLoginEnabled(boolean enabled) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_AUTO_LOGIN, enabled);
        editor.apply();
    }
    
    /**
     * 检查是否启用了自动登录
     */
    public boolean isAutoLoginEnabled() {
        return prefs.getBoolean(KEY_AUTO_LOGIN, false);
    }
    
    /**
     * 生成会话令牌（简单实现）
     */
    private String generateSessionToken(User user) {
        return user.getUserId() + "_" + System.currentTimeMillis();
    }
    
    /**
     * 刷新会话（延长会话时间）
     */
    public void refreshSession() {
        if (!isLoggedIn()) {
            return;
        }
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_SESSION_CREATED_AT, System.currentTimeMillis());
        editor.apply();
    }
}

