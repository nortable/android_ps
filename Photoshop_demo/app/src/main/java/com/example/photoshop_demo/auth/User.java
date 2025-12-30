package com.example.photoshop_demo.auth;

import java.io.Serializable;

/**
 * 用户实体类
 * 存储用户基本信息
 */
public class User implements Serializable {
    private String userId;          // 用户ID（UUID）
    private String username;        // 用户名（唯一）
    private String passwordHash;    // 密码哈希
    private String email;           // 邮箱（可选）
    private String avatarPath;      // 头像路径（可选）
    private long createdAt;         // 创建时间（时间戳）
    private long lastLoginAt;       // 最后登录时间
    private boolean isActive;       // 账号状态（true=启用，false=禁用）

    // 构造函数
    public User() {
        this.isActive = true;
    }

    public User(String userId, String username, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(long lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", lastLoginAt=" + lastLoginAt +
                ", isActive=" + isActive +
                '}';
    }
}

