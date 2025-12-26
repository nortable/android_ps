# 项目滑动删除功能实现 Pipeline

## 📋 需求分析

### 用户痛点
- ❌ 项目列表冗余，有很多重复/旧的项目记录
- ❌ 无法删除不需要的项目
- ❌ 列表越来越长，影响使用体验

### 功能需求
1. ✅ **滑动删除**：左滑或右滑项目显示删除按钮
2. ✅ **删除项目记录**：从列表和SharedPreferences中移除
3. ✅ **保留已保存的照片**：删除项目记录时，相册中的照片不删除
4. ✅ **视觉反馈**：删除动画 + Toast提示
5. ✅ **撤销功能**（可选）：3秒内可以撤销删除

---

## 🎯 实现方案

### 方案选择：ItemTouchHelper

**优势**：
- Android官方推荐
- 与RecyclerView原生集成
- 支持滑动和拖拽
- 自带动画效果
- 实现简单

**效果**：
```
项目列表项
    ↓
用户左滑/右滑
    ↓
显示红色删除背景
    ↓
滑动到一定距离
    ↓
触发删除
    ↓
从列表中移除（带动画）
    ↓
显示Toast："已删除项目"
    ↓
（可选）3秒内可点击"撤销"恢复
```

---

## 📁 文件结构

```
实现涉及的文件：

新增：
├── SwipeToDeleteCallback.java          [核心] ItemTouchHelper回调
└── item_project_swipe_bg.xml          [UI] 滑动背景（红色+删除图标）

修改：
├── HomeActivity.java                   添加ItemTouchHelper
├── ProjectAdapter.java                 添加删除方法
├── ProjectManager.java                 确保删除逻辑正确
└── item_project.xml                   （可能需要调整）
```

---

## 🔧 实现步骤

### Step 1: 创建 SwipeToDeleteCallback（1小时）

**文件**：`app/src/main/java/com/example/photoshop_demo/SwipeToDeleteCallback.java`

**职责**：
- 定义滑动方向（左滑或右滑）
- 绘制滑动背景（红色+删除图标）
- 触发删除回调

**核心代码结构**：
```java
public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    
    private ProjectAdapter adapter;
    private Paint paint;
    private Drawable deleteIcon;
    
    public SwipeToDeleteCallback(ProjectAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
        // 初始化画笔和图标
    }
    
    @Override
    public boolean onMove(...) {
        return false; // 不支持拖拽
    }
    
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        // 获取位置
        int position = viewHolder.getAdapterPosition();
        // 调用Adapter删除
        adapter.deleteProject(position);
    }
    
    @Override
    public void onChildDraw(...) {
        // 绘制红色背景
        // 绘制删除图标
        // 根据滑动距离调整透明度
    }
}
```

**关键点**：
1. **滑动方向**：`ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT`（支持左右滑动）
2. **背景绘制**：Canvas绘制红色矩形 + 删除图标
3. **透明度渐变**：根据滑动距离调整alpha值（更好的视觉效果）

---

### Step 2: 修改 ProjectAdapter（30分钟）

**文件**：`app/src/main/java/com/example/photoshop_demo/ProjectAdapter.java`

**新增内容**：

#### 2.1 删除接口回调
```java
public interface OnProjectDeleteListener {
    void onProjectDelete(EditProject project, int position);
}

private OnProjectDeleteListener deleteListener;

public void setOnProjectDeleteListener(OnProjectDeleteListener listener) {
    this.deleteListener = listener;
}
```

#### 2.2 删除方法
```java
public void deleteProject(int position) {
    if (position >= 0 && position < projects.size()) {
        EditProject project = projects.get(position);
        
        // 从列表移除
        projects.remove(position);
        notifyItemRemoved(position);
        
        // 通知HomeActivity处理删除逻辑
        if (deleteListener != null) {
            deleteListener.onProjectDelete(project, position);
        }
    }
}
```

#### 2.3 撤销恢复方法（可选）
```java
public void restoreProject(EditProject project, int position) {
    projects.add(position, project);
    notifyItemInserted(position);
}
```

**改动点**：
- ✅ 添加删除回调接口
- ✅ 添加删除方法（从列表移除 + 通知UI）
- ✅ 添加恢复方法（撤销功能用）

---

### Step 3: 修改 HomeActivity（30分钟）

**文件**：`app/src/main/java/com/example/photoshop_demo/HomeActivity.java`

**新增内容**：

#### 3.1 在 setupRecyclerView() 中添加 ItemTouchHelper
```java
private void setupRecyclerView() {
    projectsRecycler = findViewById(R.id.projects_recycler);
    projectsRecycler.setLayoutManager(new LinearLayoutManager(this));
    
    projectAdapter = new ProjectAdapter(this, projectManager.getAllProjects());
    
    // 设置点击监听
    projectAdapter.setOnProjectClickListener(project -> {
        reopenProject(project);
    });
    
    // ✅ 新增：设置删除监听
    projectAdapter.setOnProjectDeleteListener((project, position) -> {
        handleProjectDelete(project, position);
    });
    
    projectsRecycler.setAdapter(projectAdapter);
    
    // ✅ 新增：附加滑动删除
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
        new SwipeToDeleteCallback(projectAdapter));
    itemTouchHelper.attachToRecyclerView(projectsRecycler);
}
```

#### 3.2 处理删除逻辑
```java
private void handleProjectDelete(EditProject project, int position) {
    // 从ProjectManager删除记录
    projectManager.deleteProject(project.getProjectId());
    
    // 显示Toast
    Toast.makeText(this, "已删除项目", Toast.LENGTH_SHORT).show();
    
    // 可选：撤销功能
    // showUndoSnackbar(project, position);
}
```

#### 3.3 撤销功能（可选）
```java
private void showUndoSnackbar(EditProject project, int position) {
    Snackbar.make(projectsRecycler, "项目已删除", Snackbar.LENGTH_LONG)
        .setAction("撤销", v -> {
            // 恢复项目
            projectAdapter.restoreProject(project, position);
            projectManager.restoreProject(project);
            Toast.makeText(this, "已恢复项目", Toast.LENGTH_SHORT).show();
        })
        .show();
}
```

**改动点**：
- ✅ 创建ItemTouchHelper并附加到RecyclerView
- ✅ 实现删除处理逻辑
- ✅ 添加撤销功能（可选）

---

### Step 4: 确保删除逻辑正确（15分钟）

**文件**：`app/src/main/java/com/example/photoshop_demo/ProjectManager.java`

**检查现有的 deleteProject 方法**：
```java
public void deleteProject(String projectId) {
    List<EditProject> projects = getAllProjects();
    for (int i = 0; i < projects.size(); i++) {
        if (projects.get(i).getProjectId().equals(projectId)) {
            // ✅ 只删除记录，不删除文件
            projects.remove(i);
            saveProjects(projects);
            return;
        }
    }
}
```

**重要**：
- ✅ **只删除SharedPreferences中的记录**
- ✅ **不删除editedImagePath指向的文件**（用户的相册照片）
- ❌ **不删除原始图片**（可能是相册中的照片）

如果需要删除临时文件（可选）：
```java
public void deleteProject(String projectId, boolean deleteFiles) {
    List<EditProject> projects = getAllProjects();
    for (int i = 0; i < projects.size(); i++) {
        EditProject project = projects.get(i);
        if (project.getProjectId().equals(projectId)) {
            // 可选：删除临时缓存文件
            if (deleteFiles) {
                deleteTempFiles(project);
            }
            
            projects.remove(i);
            saveProjects(projects);
            return;
        }
    }
}

private void deleteTempFiles(EditProject project) {
    // 只删除app私有目录的临时文件
    // 不删除相册中的已保存文件
}
```

---

### Step 5: 添加删除图标资源（15分钟）

#### 5.1 下载/创建删除图标

**方式1：使用Android Material Icons**
```xml
<!-- res/drawable/ic_delete.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M6,19c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7H6v12zM19,4h-3.5l-1,-1h-5l-1,1H5v2h14V4z"/>
</vector>
```

#### 5.2 创建滑动背景drawable（可选）
```xml
<!-- res/drawable/swipe_delete_background.xml -->
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#FF0000"/>
    <corners android:radius="8dp"/>
</shape>
```

---

### Step 6: 可选优化（30分钟）

#### 6.1 添加Snackbar（撤销功能）

**依赖**：
```gradle
implementation 'com.google.android.material:material:1.9.0'
```

**使用**：
```java
import com.google.android.material.snackbar.Snackbar;

Snackbar.make(view, "项目已删除", Snackbar.LENGTH_LONG)
    .setAction("撤销", v -> restoreProject())
    .show();
```

#### 6.2 添加删除确认对话框（防误删）

```java
private void confirmDelete(EditProject project, int position) {
    new AlertDialog.Builder(this)
        .setTitle("删除项目")
        .setMessage("确定要删除这个项目吗？\n已保存的照片将保留在相册中。")
        .setPositiveButton("删除", (dialog, which) -> {
            projectAdapter.deleteProject(position);
        })
        .setNegativeButton("取消", null)
        .show();
}
```

#### 6.3 优化删除动画

在 `SwipeToDeleteCallback` 中添加：
```java
@Override
public void onChildDraw(Canvas c, RecyclerView recyclerView, 
                       RecyclerView.ViewHolder viewHolder,
                       float dX, float dY, int actionState, 
                       boolean isCurrentlyActive) {
    
    // 根据滑动距离调整透明度
    float alpha = Math.min(1.0f, Math.abs(dX) / viewHolder.itemView.getWidth());
    
    // 绘制背景时应用透明度
    paint.setAlpha((int)(alpha * 255));
    
    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, 
                     actionState, isCurrentlyActive);
}
```

---

## 📊 实现时间表

| 步骤 | 任务 | 预计时间 |
|------|------|---------|
| 1 | 创建SwipeToDeleteCallback | 1小时 |
| 2 | 修改ProjectAdapter | 30分钟 |
| 3 | 修改HomeActivity | 30分钟 |
| 4 | 检查ProjectManager逻辑 | 15分钟 |
| 5 | 添加删除图标资源 | 15分钟 |
| 6 | 可选优化（撤销/确认） | 30分钟 |
| **总计** | | **约3小时** |

---

## 🎨 视觉效果

### 默认状态
```
┌─────────────────────────────────┐
│ 🖼️ 编辑项目 #17667              │
│    2小时前              已保存    │
└─────────────────────────────────┘
```

### 左滑状态
```
┌─────────────────────────────────┐
│ 🖼️ 编辑项目 #17667    🗑️ 删除  │
│    2小时前              已保存    │
└─────────────────────────────────┘
     ← 滑动方向
```

### 删除动画
```
┌─────────────────────────────────┐
│ 🖼️ 编辑项目 #17667              │  ← 淡出
└─────────────────────────────────┘  ← 高度收缩

Toast: "已删除项目"
```

---

## ✅ 测试清单

### 基础功能
- [ ] 左滑显示删除背景
- [ ] 右滑显示删除背景
- [ ] 滑动到阈值触发删除
- [ ] 删除后项目从列表消失
- [ ] 删除后SharedPreferences更新
- [ ] 删除后Toast显示

### 数据完整性
- [ ] 删除项目记录成功
- [ ] 相册中的已保存照片仍然存在
- [ ] 其他项目不受影响

### 边界情况
- [ ] 删除第一个项目
- [ ] 删除最后一个项目
- [ ] 删除所有项目（列表为空）
- [ ] 快速连续滑动删除

### 可选功能
- [ ] 撤销功能正常工作
- [ ] 撤销后项目恢复到原位置
- [ ] Snackbar 3秒后自动消失

---

## 🔐 安全考虑

### 数据安全
1. **只删除记录**
   - ✅ SharedPreferences中的项目记录
   - ❌ **不删除**相册中的已保存照片
   
2. **临时文件处理**
   - 可以删除app私有目录的临时缓存
   - 不影响用户的相册内容

3. **误删保护**
   - 添加撤销功能（推荐）
   - 或添加删除确认对话框

### 代码健壮性
```java
// 添加空值检查
if (position >= 0 && position < projects.size()) {
    // 删除逻辑
}

// 添加异常处理
try {
    projectManager.deleteProject(projectId);
} catch (Exception e) {
    Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
}
```

---

## 📝 实现顺序建议

### 阶段1：核心功能（必需，2小时）
1. ✅ 创建 SwipeToDeleteCallback
2. ✅ 修改 ProjectAdapter（添加删除方法）
3. ✅ 修改 HomeActivity（集成ItemTouchHelper）
4. ✅ 测试基本删除功能

### 阶段2：视觉优化（推荐，30分钟）
5. ✅ 添加删除图标
6. ✅ 优化滑动背景绘制
7. ✅ 添加透明度渐变效果

### 阶段3：用户体验（可选，30分钟）
8. ⭐ 添加撤销功能（强烈推荐）
9. ⭐ 或添加删除确认对话框
10. ✅ 完善Toast提示

---

## 🎯 成功标准

### 功能完整性
- ✅ 可以左滑或右滑删除项目
- ✅ 删除后项目从列表消失
- ✅ 删除后SharedPreferences更新
- ✅ 已保存的照片仍在相册中

### 用户体验
- ✅ 删除动画流畅
- ✅ 视觉反馈清晰（红色背景+删除图标）
- ✅ Toast提示明确
- ✅ 支持撤销（可选但推荐）

### 代码质量
- ✅ 无Linter错误
- ✅ 异常处理完善
- ✅ 代码注释清晰

---

## 🚀 后续优化方向

1. **批量删除**
   - 长按进入多选模式
   - 勾选多个项目一次性删除

2. **自动清理**
   - 定期清理N天前的项目
   - 或超过M个项目时自动删除最旧的

3. **云同步**（高级）
   - 删除时同步到云端
   - 支持跨设备同步

---

**准备开始实现？回复"开始实现"我将按顺序完成所有步骤！**

---

*Pipeline创建日期：2025-12-26*  
*预计实现时间：2-3小时*  
*优先级：高*

