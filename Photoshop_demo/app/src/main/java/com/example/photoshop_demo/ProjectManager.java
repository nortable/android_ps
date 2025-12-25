package com.example.photoshop_demo;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目管理器
 * 原因：管理所有编辑项目，包括保存、加载、删除等操作
 * 使用SharedPreferences存储项目列表（简单方案，适合少量数据）
 */
public class ProjectManager {
    private static final String PREFS_NAME = "photoshop_projects";
    private static final String KEY_PROJECTS = "projects_list";
    
    private Context context;
    private SharedPreferences prefs;
    private Gson gson;

    public ProjectManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * 获取所有项目
     */
    public List<EditProject> getAllProjects() {
        String json = prefs.getString(KEY_PROJECTS, "[]");
        Type type = new TypeToken<ArrayList<EditProject>>(){}.getType();
        List<EditProject> projects = gson.fromJson(json, type);
        return projects != null ? projects : new ArrayList<>();
    }

    /**
     * 保存项目列表
     */
    private void saveProjects(List<EditProject> projects) {
        String json = gson.toJson(projects);
        prefs.edit().putString(KEY_PROJECTS, json).apply();
    }

    /**
     * 添加新项目
     */
    public EditProject createProject(String originalImageUri) {
        String projectId = "project_" + System.currentTimeMillis();
        EditProject project = new EditProject(projectId, originalImageUri);
        
        List<EditProject> projects = getAllProjects();
        projects.add(0, project);  // 添加到列表开头（最新的在前面）
        saveProjects(projects);
        
        return project;
    }

    /**
     * 更新项目
     */
    public void updateProject(EditProject project) {
        List<EditProject> projects = getAllProjects();
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getProjectId().equals(project.getProjectId())) {
                project.setLastEditTime(System.currentTimeMillis());
                projects.set(i, project);
                saveProjects(projects);
                return;
            }
        }
    }

    /**
     * 根据ID获取项目
     */
    public EditProject getProject(String projectId) {
        List<EditProject> projects = getAllProjects();
        for (EditProject project : projects) {
            if (project.getProjectId().equals(projectId)) {
                return project;
            }
        }
        return null;
    }

    /**
     * 删除项目
     */
    public void deleteProject(String projectId) {
        List<EditProject> projects = getAllProjects();
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getProjectId().equals(projectId)) {
                projects.remove(i);
                saveProjects(projects);
                return;
            }
        }
    }
}

