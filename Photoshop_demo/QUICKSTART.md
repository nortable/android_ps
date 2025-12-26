# 快速开始指南 - 第二阶段功能

## 🎯 实现的功能

### ✅ 核心流程
1. **选择照片** → 从相册选择图片（需要权限）
2. **创建项目** → 自动生成唯一ID
3. **编辑图片** → 加载到编辑页面
4. **应用效果** → 示例：黑白滤镜
5. **导出保存** → 保存到相册`/Pictures/Photoshop_demo/`
6. **重新编辑** → 首页列表显示所有项目

---

## 🚀 立即运行

### 第1步：同步项目
```bash
Android Studio → 点击 "Sync Project with Gradle Files"
```
**原因**：新增了Gson依赖

### 第2步：运行应用
```bash
连接设备 → 点击 Run (▶️)
```

### 第3步：测试功能
1. 点击首页 **"+"** 按钮
2. **允许相册权限**
3. 选择一张照片
4. 在编辑页点击 **"AI追色"**（应用黑白滤镜）
5. 点击 **"导出"** 保存
6. 返回首页，看到项目列表
7. 打开系统相册，查看保存的图片

---

## 📁 新增文件一览

```
✨ 数据层
├── EditProject.java          # 项目数据模型
└── ProjectManager.java       # 项目管理器（CRUD）

✨ UI层
├── ProjectAdapter.java       # 项目列表适配器
└── item_project.xml          # 列表项布局

✨ 业务层
├── HomeActivity.java         # 更新：权限+相册+列表
└── EditActivity.java         # 更新：加载+编辑+保存

✨ 配置
├── AndroidManifest.xml       # 新增：权限声明
└── build.gradle.kts          # 新增：Gson依赖
```

---

## 🔑 关键代码位置

### 权限请求
```java
// HomeActivity.java - Line 45
private void checkAndRequestPermission() {
    // 根据Android版本选择权限
    // Android 13+: READ_MEDIA_IMAGES
    // Android 6-12: READ_EXTERNAL_STORAGE
}
```

### 相册选择
```java
// HomeActivity.java - Line 89
pickImageLauncher.launch("image/*");
```

### 创建项目
```java
// HomeActivity.java - Line 184
EditProject project = projectManager.createProject(imageUri.toString());
```

### 加载图片
```java
// EditActivity.java - Line 75
currentBitmap = MediaStore.Images.Media.getBitmap(
    getContentResolver(), imageUri);
```

### 保存到相册
```java
// EditActivity.java - Line 239
ContentValues values = new ContentValues();
values.put(MediaStore.Images.Media.RELATIVE_PATH, 
    "Pictures/Photoshop_demo");
Uri uri = getContentResolver().insert(...);
```

---

## 📊 数据流程图

```
用户选择照片
    ↓
[HomeActivity]
    ├─ 请求权限
    ├─ 打开相册
    ├─ 创建项目 → ProjectManager.createProject()
    └─ 跳转EditActivity (传递project_id + image_uri)
    
[EditActivity]
    ├─ 加载图片 → MediaStore.getBitmap()
    ├─ 应用滤镜 → applyGrayscaleFilter()
    ├─ 保存相册 → saveImageToGallery()
    └─ 更新项目 → ProjectManager.updateProject()
    
[返回HomeActivity]
    ├─ onResume() 刷新列表
    └─ 显示新项目（状态：已保存）
```

---

## 🎨 按钮功能说明

### HomeActivity（首页）
| 按钮 | 当前功能 |
|------|---------|
| **AI追色** | 打开相册选择照片 ✅ |
| **相机模拟** | 开发中 🚧 |
| **特效** | 开发中 🚧 |
| **拼图** | 打开相册选择照片 ✅ |
| **+** | 打开相册选择照片 ✅ |

### EditActivity（编辑页）
| 按钮 | 当前功能 |
|------|---------|
| **←** | 返回首页 ✅ |
| **↶** | 撤销编辑 ✅ |
| **⋯** | 开发中 🚧 |
| **⭐AI去创意** | 开发中 🚧 |
| **导出** | 保存到相册 ✅ |
| **AI色彩调节** | 开发中 🚧 |
| **AI追色** | 应用黑白滤镜 ✅（示例） |
| **滤镜** | 开发中 🚧 |
| **白平衡** | 开发中 🚧 |
| **影调** | 开发中 🚧 |

---

## 💾 存储位置

### 项目元数据
```
路径: /data/data/com.example.photoshop_demo/shared_prefs/
文件: photoshop_projects.xml
格式: JSON (通过Gson序列化)
卸载: 会删除
```

### 导出的图片
```
路径: /sdcard/Pictures/Photoshop_demo/
文件: Edited_1703419200000.jpg
格式: JPEG (质量95)
卸载: 不会删除 ✅
```

---

## ⚠️ 注意事项

### 1. 首次运行必须授予权限
```
应用请求 → 点击"允许" → 可以访问相册
如果点击"拒绝" → 需要去系统设置手动开启
```

### 2. 测试建议
- ✅ 使用真机测试（模拟器相册可能为空）
- ✅ 选择小图片测试（大图片可能OOM）
- ✅ 检查相册是否有保存的图片

### 3. 内存管理
```java
// EditActivity.onDestroy()
currentBitmap.recycle();  // 手动释放Bitmap内存
```

---

## 🐛 故障排除

| 问题 | 可能原因 | 解决方案 |
|-----|---------|---------|
| 点击无反应 | 权限未授予 | 检查设置→应用权限 |
| 找不到保存的图片 | 相册未刷新 | 重启相册App |
| 应用崩溃 | 图片太大OOM | 选择小图片测试 |
| 列表为空 | 没有项目 | 先创建一个项目 |
| Gradle错误 | 依赖未下载 | Sync Project |

---

## 📱 测试清单

- [ ] 首次打开，授予相册权限
- [ ] 点击"+"按钮，选择照片
- [ ] 编辑页显示选中的照片
- [ ] 点击"AI追色"，照片变黑白
- [ ] 点击"撤销"，照片恢复原样
- [ ] 点击"导出"，提示保存成功
- [ ] 返回首页，列表显示新项目
- [ ] 打开系统相册，找到保存的图片
- [ ] 首页点击项目，可以重新编辑

---

## 📚 详细文档

- **完整实现说明**: `STAGE2_IMPLEMENTATION.md`
- **Android基础教程**: `TUTORIAL.md`
- **存储权限指南**: `STORAGE_GUIDE.md`
- **项目概述**: `README.md`

---

## 🎉 已完成！

第二阶段的所有功能都已实现并可以运行！

**下一步可以做什么？**
1. 添加更多滤镜效果
2. 实现亮度/对比度调整
3. 添加裁剪功能
4. 优化大图片加载

**有任何问题随时询问！** 🚀


