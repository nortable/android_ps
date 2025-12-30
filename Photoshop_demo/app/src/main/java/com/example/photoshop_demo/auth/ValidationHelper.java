package com.example.photoshop_demo.auth;

import java.util.regex.Pattern;

/**
 * 输入验证工具类
 * 提供用户名、邮箱、密码等格式验证
 */
public class ValidationHelper {
    
    // 用户名正则：3-20位，字母数字下划线
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    
    // 邮箱正则
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    // 常见弱密码列表
    private static final String[] COMMON_PASSWORDS = {
        "123456", "password", "12345678", "qwerty", "123456789",
        "12345", "1234", "111111", "1234567", "dragon",
        "123123", "baseball", "abc123", "football", "monkey"
    };
    
    /**
     * 验证用户名格式
     */
    public static boolean isUsernameValid(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }
    
    /**
     * 验证邮箱格式
     */
    public static boolean isEmailValid(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * 验证密码格式（基础要求）
     */
    public static boolean isPasswordValid(String password) {
        if (password == null || password.length() < 6 || password.length() > 30) {
            return false;
        }
        return true;
    }
    
    /**
     * 检查是否为常见弱密码
     */
    public static boolean isCommonPassword(String password) {
        if (password == null) return false;
        
        String lowerPassword = password.toLowerCase();
        for (String common : COMMON_PASSWORDS) {
            if (lowerPassword.equals(common)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 验证密码强度（严格要求）
     */
    public static Result<Void> validatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return Result.error(Result.ErrorCode.WEAK_PASSWORD, "密码不能为空");
        }
        
        if (password.length() < 6) {
            return Result.error(Result.ErrorCode.WEAK_PASSWORD, "密码至少需要6位");
        }
        
        if (password.length() > 30) {
            return Result.error(Result.ErrorCode.WEAK_PASSWORD, "密码不能超过30位");
        }
        
        if (isCommonPassword(password)) {
            return Result.error(Result.ErrorCode.WEAK_PASSWORD, "密码过于简单，请使用更复杂的密码");
        }
        
        // 检查是否包含字母和数字
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        
        if (!hasLetter || !hasDigit) {
            return Result.error(Result.ErrorCode.WEAK_PASSWORD, "密码必须包含字母和数字");
        }
        
        return Result.success(null);
    }
    
    /**
     * 获取用户名验证错误提示
     */
    public static String getUsernameError(String username) {
        if (username == null || username.isEmpty()) {
            return "用户名不能为空";
        }
        if (username.length() < 3) {
            return "用户名至少需要3位";
        }
        if (username.length() > 20) {
            return "用户名不能超过20位";
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return "用户名只能包含字母、数字和下划线";
        }
        return null; // 验证通过
    }
    
    /**
     * 获取邮箱验证错误提示
     */
    public static String getEmailError(String email) {
        if (email == null || email.isEmpty()) {
            return null; // 邮箱是可选的
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "邮箱格式不正确";
        }
        return null; // 验证通过
    }
}

