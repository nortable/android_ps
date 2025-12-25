# 第二阶段功能实现说明文档

## 🎉 已完成功能

### 1. ✅ 相册访问与权限管理

#### 添加的权限（AndroidManifest.xml）
```xml
<!-- 读取相册权限 -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />  <!-- Android 12及以下 -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    <!-- Android 13+ -->

<!-- 写入权限（Android 9及以下需要） -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" -->
```

**原因**：
- 不同Android版本需要不同的权限
- Android 10+使用MediaStore API不需要写入权限
- 运行时需要请求权限，用户可以拒绝

---

### 2. ✅ 项目管理系统

#### 新增文件：
1. **EditProject.java** - 项目数据模型
   - `projectId`: 唯一项目ID（格式：project_时间戳）
   - `originalImageUri`: 原始图片URI
   - `editedImagePath`: 编辑后的图片路径
   - `createTime`: 创建时间
   - `lastEditTime`: 最后编辑时间

2. **ProjectManager.java** - 项目管理器
   - 使用SharedPreferences存储项目列表
   - 使用Gson库序列化/反序列化JSON
   - 提供CRUD操作（创建、读取、更新、删除）

**为什么这样设计？**
- SharedPreferences：简单、快速，适合少量数据
- Gson：自动处理Java对象↔JSON转换
- 每个项目有唯一ID，便于追踪和管理

---

### 3. ✅ 项目列表显示

#### 新增文件：
1. **ProjectAdapter.java** - RecyclerView适配器
   - 显示项目缩略图
   - 显示项目创建时间（刚刚/分钟前/小时前/日期）
   - 显示项目状态（编辑中/已保存）
   - 支持点击重新编辑

2. **item_project.xml** - 项目列表项布局
   - 左侧：100x100 缩略图
   - 中间：项目名称、时间、状态
   - 右侧：重新编辑箭头

**为什么用RecyclerView？**
- 性能好：只渲染可见的item
- 灵活：支持复杂布局
- 动画：自带item动画效果

---

### 4. ✅ HomeActivity 完整实现

#### 核心功能：

**4.1 权限请求流程**
```java
1. 用户点击"选择照片"按钮
   ↓
2. 检查Android版本，选择对应权限
   - Android 13+: READ_MEDIA_IMAGES
   - Android 6-12: READ_EXTERNAL_STORAGE
   ↓
3. 检查是否已有权限
   - 有：直接打开相册
   - 无：弹出权限请求对话框
   ↓
4. 用户授权后打开系统相册选择器
```

**4.2 相册选择**
- 使用`ActivityResultContracts.GetContent()`
- 只选择图片类型：`"image/*"`
- 获得图片URI后创建新项目

**4.3 项目列表**
- 显示所有历史项目
- 点击项目可重新编辑
- 从EditActivity返回时自动刷新列表（onResume）

**原因说明**：
- `ActivityResultLauncher`：新的权限请求方式，替代旧的`requestPermissions()`
- `GetContent`：系统提供的文件选择器，自动处理权限
- `onResume()`刷新：确保列表始终显示最新数据

---

### 5. ✅ EditActivity 完整实现

#### 核心功能：

**5.1 图片加载**
```java
Uri imageUri = Uri.parse(imageUriString);
currentBitmap = MediaStore.Images.Media.getBitmap(
    getContentResolver(), imageUri);
imageView.setImageBitmap(currentBitmap);
```
- 从URI加载Bitmap
- 保存原始图片副本（用于撤销）
- 显示在ImageView中

**5.2 简单编辑（示例：黑白滤镜）**
```java
// 遍历所有像素
for (int x = 0; x < width; x++) {
    for (int y = 0; y < height; y++) {
        // 获取RGB值
        // 计算灰度值 = 0.299*R + 0.587*G + 0.114*B
        // 设置新像素
    }
}
```
- 演示基本的图片处理算法
- 可以扩展为其他滤镜

**5.3 保存到相册（核心功能）**
```java
// 使用MediaStore API（Android 10+兼容）
ContentValues values = new ContentValues();
values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
values.put(MediaStore.Images.Media.RELATIVE_PATH, 
    "Pictures/Photoshop_demo");

Uri uri = getContentResolver().insert(
    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

try (OutputStream os = getContentResolver().openOutputStream(uri)) {
    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, os);
}
```

**保存路径**：`/sdcard/Pictures/Photoshop_demo/Edited_xxx.jpg`

**为什么这样保存？**
- Android 10+强制使用MediaStore（分区存储）
- 自动显示在系统相册中
- 不需要WRITE_EXTERNAL_STORAGE权限
- 质量95：高质量JPEG

**5.4 更新项目状态**
- 保存成功后更新项目的`editedImagePath`
- 项目列表显示"已保存"状态
- 可以再次打开继续编辑

---

## 📱 完整用户流程

### 新建项目流程
```
1. 打开App → HomeActivity
   ↓
2. 点击"+"或功能按钮（AI追色/拼图等）
   ↓
3. 检查权限
   - 第一次：弹出权限请求
   - 已授权：直接继续
   ↓
4. 打开系统相册选择照片
   ↓
5. 选择照片后：
   - 创建新项目（生成project_id）
   - 保存到ProjectManager
   - 跳转到EditActivity
   ↓
6. EditActivity：
   - 加载图片显示
   - 可以应用滤镜（示例：黑白）
   - 点击"撤销"恢复原图
   ↓
7. 点击"导出"按钮：
   - 保存到相册（Pictures/Photoshop_demo/）
   - 更新项目状态为"已保存"
   - 返回HomeActivity
   ↓
8. HomeActivity自动刷新
   - 显示新项目在列表顶部
   - 状态显示"已保存"
```

### 重新编辑流程
```
1. HomeActivity列表中点击项目
   ↓
2. 跳转到EditActivity
   - 传递project_id和image_uri
   - 如果有edited_image_path，优先加载
   ↓
3. 继续编辑
   ↓
4. 再次点击"导出"
   - 覆盖之前的保存
   - 更新项目时间
```

---

## 🗂️ 数据存储位置

### 项目数据（元数据）
```
位置：SharedPreferences
路径：/data/data/com.example.photoshop_demo/shared_prefs/photoshop_projects.xml
内容：JSON格式的项目列表

{
  "projects_list": [
    {
      "projectId": "project_1703419200000",
      "originalImageUri": "content://media/external/images/media/123",
      "editedImagePath": "content://media/external/images/media/456",
      "createTime": 1703419200000,
      "lastEditTime": 1703419260000
    }
  ]
}
```

### 导出的图片
```
位置：系统相册
路径：/sdcard/Pictures/Photoshop_demo/Edited_1703419260000.jpg
特点：
- 用户可以在相册中看到
- 可以分享到其他应用
- 卸载应用后不会删除
```

---

## 🔧 技术实现细节

### 1. 为什么用URI而不是文件路径？

**原因**：
- Android 10+强制使用Scoped Storage
- URI是访问文件的抽象方式
- 支持云存储（Google Photos等）
- 更安全，应用无法随意访问文件系统

```java
// ❌ 旧方式（Android 10+不可用）
File file = new File("/sdcard/DCIM/photo.jpg");

// ✅ 新方式
Uri uri = Uri.parse("content://media/external/images/media/123");
Bitmap bitmap = MediaStore.Images.Media.getBitmap(resolver, uri);
```

### 2. 为什么在后台线程保存图片？

```java
new Thread(() -> {
    // 保存图片（耗时操作）
    saveImageToGallery(bitmap);
    
    runOnUiThread(() -> {
        // 更新UI（必须在主线程）
        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
    });
}).start();
```

**原因**：
- 保存图片是耗时操作（可能1-3秒）
- 在主线程会导致ANR（Application Not Responding）
- 后台线程处理，完成后回到主线程更新UI

### 3. 为什么要释放Bitmap内存？

```java
@Override
protected void onDestroy() {
    if (currentBitmap != null && !currentBitmap.isRecycled()) {
        currentBitmap.recycle();  // 释放内存
    }
}
```

**原因**：
- Bitmap占用大量内存（4K图片≈50MB）
- Java GC不会立即回收
- 手动释放避免内存泄漏

---

## 📊 代码结构总结

```
app/src/main/
├── java/com/example/photoshop_demo/
│   ├── EditProject.java           # 数据模型
│   ├── ProjectManager.java        # 业务逻辑（CRUD）
│   ├── ProjectAdapter.java        # UI适配器
│   ├── HomeActivity.java          # 首页控制器
│   └── EditActivity.java          # 编辑页控制器
│
├── res/
│   ├── layout/
│   │   ├── activity_home.xml      # 首页布局
│   │   ├── activity_edit.xml      # 编辑页布局
│   │   └── item_project.xml       # 列表项布局
│   └── values/
│       ├── colors.xml             # 颜色资源
│       └── strings.xml            # 字符串资源
│
└── AndroidManifest.xml            # 权限声明
```

---

## ✨ 已实现功能清单

- ✅ 权限申请（兼容Android 6-14）
- ✅ 相册访问（系统选择器）
- ✅ 项目管理（创建、保存、加载）
- ✅ 项目列表显示（RecyclerView）
- ✅ 图片加载与显示
- ✅ 简单编辑（黑白滤镜示例）
- ✅ 撤销功能
- ✅ 保存到相册（MediaStore API）
- ✅ 重新编辑已保存项目

---

## 🚀 如何运行

### 1. 同步Gradle
```
打开Android Studio → File → Sync Project with Gradle Files
```
**原因**：新增了Gson依赖，需要下载

### 2. 运行应用
```
连接设备或启动模拟器 → Run 'app'
```

### 3. 测试流程
```
1. 点击"+"按钮
2. 允许相册权限
3. 选择一张照片
4. 点击"AI追色"应用黑白滤镜
5. 点击"导出"保存到相册
6. 返回首页，看到项目列表
7. 点击项目可以重新编辑
8. 打开系统相册，在Photoshop_demo文件夹中看到保存的图片
```

---

## 🎯 下一步扩展建议

### 阶段3：更多编辑功能
- [ ] 亮度、对比度调整
- [ ] 更多滤镜（怀旧、鲜艳、冷色调等）
- [ ] 裁剪、旋转
- [ ] 文字、贴纸

### 阶段4：优化
- [ ] 加载大图优化（缩放、缓存）
- [ ] 撤销/重做栈（支持多步撤销）
- [ ] 进度提示（保存时显示进度条）
- [ ] 数据库存储（替代SharedPreferences）

### 阶段5：高级功能
- [ ] AI美颜
- [ ] 图层管理
- [ ] 模板和预设
- [ ] 分享到社交平台

---

## 💡 重要提示

### 测试时注意事项

1. **首次运行必须授予权限**
   - 如果拒绝权限，需要去系统设置中手动开启

2. **使用真机测试效果更好**
   - 模拟器的相册可能没有照片
   - 可以通过拖拽添加图片到模拟器

3. **保存的图片在相册中查看**
   - 打开系统相册App
   - 找到"Photoshop_demo"文件夹

4. **内存占用**
   - 处理大图片会占用较多内存
   - 如果崩溃，考虑缩小图片

---

## 🐛 常见问题

### Q1: 点击按钮没有反应？
**A**: 检查权限是否授予。查看Logcat看是否有错误。

### Q2: 保存后在相册找不到图片？
**A**: 
- 确认保存成功的Toast提示
- 打开文件管理器，查看`Pictures/Photoshop_demo`目录
- 某些相册App需要刷新才能显示

### Q3: 应用崩溃？
**A**: 
- 查看Logcat错误信息
- 可能是图片太大导致OOM
- 可能是权限不足

### Q4: 项目列表显示空白？
**A**: 
- 先创建一个项目
- 检查ProjectManager是否正确保存数据

---

## 📖 相关文档

- `TUTORIAL.md` - Android基础教程
- `STORAGE_GUIDE.md` - 存储和权限详细指南
- `README.md` - 项目概述

---

**恭喜！第二阶段功能已全部实现！** 🎉

现在你可以：
1. ✅ 从相册选择照片
2. ✅ 进入编辑页面
3. ✅ 应用简单滤镜
4. ✅ 保存到相册
5. ✅ 在首页查看和重新编辑项目

**准备好进入第三阶段了吗？** 🚀

