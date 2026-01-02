package com.example.photoshop_demo.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * 密码工具类
 * 提供密码哈希、验证、强度检查等功能
 * 
 * 注意：由于BCrypt库可能有兼容性问题，这里使用PBKDF2算法实现
 */
public class PasswordHelper {
    
    private static final int ITERATIONS = 10000; // PBKDF2迭代次数
    private static final int SALT_LENGTH = 32;   // 盐值长度
    private static final int HASH_LENGTH = 64;   // 哈希长度
    
    /**
     * 密码强度等级
     */
    public enum PasswordStrength {
        WEAK(0, "弱"),
        MEDIUM(1, "中等"),
        STRONG(2, "强"),
        VERY_STRONG(3, "很强");
        
        private final int level;
        private final String description;
        
        PasswordStrength(int level, String description) {
            this.level = level;
            this.description = description;
        }
        
        public int getLevel() {
            return level;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 生成密码哈希（带盐值）
     * 格式：salt:hash
     */
    public static String hashPassword(String password) {
        try {
            // 生成随机盐值
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // 生成哈希
            byte[] hash = pbkdf2(password, salt);
            
            // 返回格式：盐值:哈希值
            return bytesToHex(salt) + ":" + bytesToHex(hash);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 验证密码
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // 分离盐值和哈希
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                return false;
            }
            
            byte[] salt = hexToBytes(parts[0]);
            byte[] expectedHash = hexToBytes(parts[1]);
            
            // 使用相同盐值生成哈希
            byte[] actualHash = pbkdf2(password, salt);
            
            // 比较哈希值
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * PBKDF2算法实现
     */
    private static byte[] pbkdf2(String password, byte[] salt) throws NoSuchAlgorithmException {
        try {
            javax.crypto.SecretKeyFactory factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(
                password.toCharArray(), 
                salt, 
                ITERATIONS, 
                HASH_LENGTH * 8
            );
            return factory.generateSecret(spec).getEncoded();
        } catch (java.security.spec.InvalidKeySpecException e) {
            throw new NoSuchAlgorithmException(e);
        }
    }
    
    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    /**
     * 十六进制字符串转字节数组
     */
    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
    
    /**
     * 检查密码强度
     */
    public static PasswordStrength checkPasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return PasswordStrength.WEAK;
        }
        
        int score = 0;
        
        // 长度检查
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        
        // 包含小写字母
        if (Pattern.compile("[a-z]").matcher(password).find()) score++;
        
        // 包含大写字母
        if (Pattern.compile("[A-Z]").matcher(password).find()) score++;
        
        // 包含数字
        if (Pattern.compile("[0-9]").matcher(password).find()) score++;
        
        // 包含特殊字符
        if (Pattern.compile("[^a-zA-Z0-9]").matcher(password).find()) score++;
        
        // 根据得分返回强度
        if (score <= 2) return PasswordStrength.WEAK;
        if (score <= 4) return PasswordStrength.MEDIUM;
        if (score <= 5) return PasswordStrength.STRONG;
        return PasswordStrength.VERY_STRONG;
    }
    
    /**
     * 验证密码格式（最小要求）
     */
    public static boolean isPasswordValid(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        // 至少包含字母和数字
        boolean hasLetter = Pattern.compile("[a-zA-Z]").matcher(password).find();
        boolean hasDigit = Pattern.compile("[0-9]").matcher(password).find();
        
        return hasLetter && hasDigit;
    }
    
    /**
     * 获取密码强度百分比（用于UI显示）
     */
    public static int getPasswordStrengthPercentage(String password) {
        PasswordStrength strength = checkPasswordStrength(password);
        switch (strength) {
            case WEAK: return 25;
            case MEDIUM: return 50;
            case STRONG: return 75;
            case VERY_STRONG: return 100;
            default: return 0;
        }
    }
}

