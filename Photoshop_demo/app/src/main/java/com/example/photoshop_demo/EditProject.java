package com.example.photoshop_demo;

import java.io.Serializable;

/**
 * 编辑项目数据模型
 * 原因：需要保存每个项目的信息，包括原图和编辑后的图片路径
 */
public class EditProject implements Serializable {
    private String projectId;           // 项目唯一ID
    private String originalImageUri;    // 原始图片URI
    private String editedImagePath;     // 编辑后的图片路径
    private long createTime;            // 创建时间
    private long lastEditTime;          // 最后编辑时间

    public EditProject(String projectId, String originalImageUri) {
        this.projectId = projectId;
        this.originalImageUri = originalImageUri;
        this.createTime = System.currentTimeMillis();
        this.lastEditTime = this.createTime;
    }

    // Getters and Setters
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getOriginalImageUri() {
        return originalImageUri;
    }

    public void setOriginalImageUri(String originalImageUri) {
        this.originalImageUri = originalImageUri;
    }

    public String getEditedImagePath() {
        return editedImagePath;
    }

    public void setEditedImagePath(String editedImagePath) {
        this.editedImagePath = editedImagePath;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getLastEditTime() {
        return lastEditTime;
    }

    public void setLastEditTime(long lastEditTime) {
        this.lastEditTime = lastEditTime;
    }
}



