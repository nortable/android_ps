package com.example.photoshop_demo.auth;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

/**
 * 权限守卫
 * 检查用户权限，控制功能访问
 */
public class AuthGuard {
    
    /**
     * 检查滤镜功能权限
     * @return true=有权限，false=无权限
     */
    public static boolean checkFilterPermission(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        return sessionManager.isLoggedIn();
    }
    
    /**
     * 显示登录提示对话框
     * 当用户尝试访问需要登录的功能时调用
     */
    public static void showLoginPrompt(Context context, String featureName) {
        new AlertDialog.Builder(context)
                .setTitle("需要登录")
                .setMessage(featureName + "功能需要登录后才能使用\n\n登录后即可使用所有高级功能")
                .setPositiveButton("立即登录", (dialog, which) -> {
                    navigateToLogin(context);
                })
                .setNegativeButton("稍后", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }
    
    /**
     * 显示登录提示对话框（带回调）
     * 用于登录成功后执行特定操作
     */
    public static void showLoginPrompt(Context context, String featureName, Runnable onLoginSuccess) {
        new AlertDialog.Builder(context)
                .setTitle("需要登录")
                .setMessage(featureName + "功能需要登录后才能使用\n\n登录后即可使用所有高级功能")
                .setPositiveButton("立即登录", (dialog, which) -> {
                    navigateToLogin(context, onLoginSuccess);
                })
                .setNegativeButton("稍后", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }
    
    /**
     * 导航到登录页面
     */
    public static void navigateToLogin(Context context) {
        try {
            Class<?> loginActivityClass = Class.forName("com.example.photoshop_demo.LoginActivity");
            Intent intent = new Intent(context, loginActivityClass);
            context.startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            // 如果LoginActivity还未创建，显示提示
            new AlertDialog.Builder(context)
                    .setTitle("提示")
                    .setMessage("登录功能正在开发中，敬请期待")
                    .setPositiveButton("确定", null)
                    .show();
        }
    }
    
    /**
     * 导航到登录页面（带回调）
     */
    public static void navigateToLogin(Context context, Runnable onLoginSuccess) {
        try {
            Class<?> loginActivityClass = Class.forName("com.example.photoshop_demo.LoginActivity");
            Intent intent = new Intent(context, loginActivityClass);
            // 可以通过Intent传递回调标识，登录成功后处理
            context.startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 检查并提示登录（统一入口）
     * @return true=已登录，false=未登录（已显示提示）
     */
    public static boolean requireLogin(Context context, String featureName) {
        if (checkFilterPermission(context)) {
            return true;
        } else {
            showLoginPrompt(context, featureName);
            return false;
        }
    }
    
    /**
     * 检查并提示登录（带回调）
     * @return true=已登录，false=未登录（已显示提示）
     */
    public static boolean requireLogin(Context context, String featureName, Runnable onLoginSuccess) {
        if (checkFilterPermission(context)) {
            return true;
        } else {
            showLoginPrompt(context, featureName, onLoginSuccess);
            return false;
        }
    }
}

