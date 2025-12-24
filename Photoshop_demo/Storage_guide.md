# Android 相册访问和照片存储完全指南

## 📋 目录
1. [Android 存储系统概述](#1-android-存储系统概述)
2. [res 目录 vs 用户数据存储](#2-res-目录-vs-用户数据存储)
3. [相册权限申请](#3-相册权限申请)
4. [如何访问相册](#4-如何访问相册)
5. [照片存储最佳实践](#5-照片存储最佳实践)
6. [完整代码示例](#6-完整代码示例)

---

## 1. Android 存储系统概述

### 1.1 存储类型对比

| 存储类型 | 位置 | 用途 | 是否需要权限 | 卸载后是否删除 |
|---------|------|------|-------------|---------------|
| **res/** | 应用内部 | 应用自带的资源（图标、布局） | ❌ 不需要 | ✅ 会删除 |
| **内部存储** | `/data/data/包名/` | 应用私有数据（设置、缓存） | ❌ 不需要 | ✅ 会删除 |
| **外部存储（私有）** | `/sdcard/Android/data/包名/` | 应用私有文件（临时编辑） | ❌ 不需要（Android 10+） | ✅ 会删除 |
| **外部存储（公共）** | `/sdcard/Pictures/` | 用户照片（相册） | ✅ 需要 | ❌ 不会删除 |
| **MediaStore** | 系统媒体库 | 照片、视频、音频 | ✅ 需要（分情况） | ❌ 不会删除 |

### 1.2 ❌ res/ 目录不能用来存储用户照片

**原因：**
```
res/
├── drawable/        # 这里放的是应用自带的图片资源
├── layout/          # 布局文件
├── values/          # 颜色、字符串等
└── mipmap/          # 应用图标

特点：
❌ 只读：无法在运行时写入新文件
❌ 编译时确定：所有文件在打包 APK 时就固定了
❌ 有限空间：会增加 APK 大小
✅ 用途：应用自带的资源（图标、默认图片等）
```

### 1.3 ✅ 用户照片应该存储在哪里？

```
推荐方案：

1. 临时编辑文件（用户可能不保存）
   → 外部存储（私有目录）
   → /sdcard/Android/data/com.example.photoshop_demo/files/temp/
   → 卸载应用后自动删除

2. 用户保存的照片（要在相册显示）
   → MediaStore（系统媒体库）
   → /sdcard/Pictures/Photoshop_demo/
   → 显示在相册中，卸载应用后保留
```

---

## 2. res 目录 vs 用户数据存储

### 2.1 res/ 目录的真实用途

```xml
res/drawable/ic_camera.xml       ← 应用图标（自带）
res/drawable/default_avatar.png  ← 默认头像（自带）
res/layout/activity_home.xml     ← 布局文件（自带）
```

**特点：**
- 在开发时添加到项目中
- 编译到 APK 包里
- 无法在运行时修改或添加新文件

### 2.2 用户数据的正确存储位置

```java
// ❌ 错误：无法写入 res 目录
File wrongPath = new File("res/drawable/user_photo.jpg");  // 这不可行！

// ✅ 正确：临时文件（应用私有）
File tempFile = new File(getExternalFilesDir(null), "temp/edited_photo.jpg");

// ✅ 正确：保存到相册（用户可见）
// 使用 MediaStore API（见下文详解）
```

### 2.3 存储路径示例

```
Android 设备存储结构：

/sdcard/  （外部存储根目录）
│
├── DCIM/                          # 相机照片
│   └── Camera/
│       └── IMG_20251224_123456.jpg
│
├── Pictures/                      # 图片目录（相册）
│   ├── Screenshots/               # 截图
│   └── Photoshop_demo/            # 你的应用保存的照片 ✅
│       └── edited_photo_001.jpg
│
├── Download/                      # 下载文件
│
└── Android/
    └── data/
        └── com.example.photoshop_demo/  # 你的应用私有目录
            ├── files/                   # 私有文件 ✅
            │   └── temp/
            │       └── editing.jpg      # 临时编辑文件
            └── cache/                   # 缓存文件
                └── thumbnail.jpg        # 缩略图
```

---

## 3. 相册权限申请

### 3.1 权限的演变（重要！）

Android 的存储权限在不同版本有很大变化：

```
Android 版本与权限要求：

📱 Android 5.0 - 9.0 (API 21-28)
   读取相册：READ_EXTERNAL_STORAGE ✅
   写入相册：WRITE_EXTERNAL_STORAGE ✅

📱 Android 10 (API 29)
   引入了"分区存储"（Scoped Storage）
   读取相册：READ_EXTERNAL_STORAGE ✅
   写入应用私有目录：不需要权限 ✅
   写入公共目录：需要 MediaStore API

📱 Android 11+ (API 30+)
   读取所有文件：MANAGE_EXTERNAL_STORAGE（很难获批）
   读取图片：READ_EXTERNAL_STORAGE ✅
   
📱 Android 13+ (API 33+)
   更细粒度的权限：
   - READ_MEDIA_IMAGES（读图片）✅ 推荐
   - READ_MEDIA_VIDEO（读视频）
   - READ_MEDIA_AUDIO（读音频）
```

### 3.2 AndroidManifest.xml 权限声明

```xml
<!-- 在 AndroidManifest.xml 中添加 -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- 相册读取权限 -->
    <!-- Android 13 以下使用 -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />
    
    <!-- Android 13+ 使用 -->
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    
    <!-- 写入相册权限（Android 10 以下需要） -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="28" />

    <application>
        ...
    </application>
</manifest>
```

**解释：**
- `maxSdkVersion="32"`：只在 Android 12 及以下请求这个权限
- Android 13+ 自动使用 `READ_MEDIA_IMAGES`

### 3.3 运行时权限请求代码

```java
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

public class HomeActivity extends AppCompatActivity {

    // 权限请求启动器
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 初始化权限请求
        setupPermissionLauncher();

        // 当用户点击"选择照片"时
        findViewById(R.id.btn_select_photo).setOnClickListener(v -> {
            checkAndRequestPermission();
        });
    }

    /**
     * 设置权限请求启动器
     * 原因：Android 要求使用新的权限请求方式
     */
    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    // 权限已授予，打开相册
                    openGallery();
                } else {
                    // 权限被拒绝，显示提示
                    Toast.makeText(this, "需要相册权限才能选择照片", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    /**
     * 检查并请求权限
     */
    private void checkAndRequestPermission() {
        String permission;
        
        // 根据 Android 版本选择权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            // Android 12 及以下
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        // 检查是否已有权限
        if (ContextCompat.checkSelfPermission(this, permission) 
                == PackageManager.PERMISSION_GRANTED) {
            // 已有权限，直接打开相册
            openGallery();
        } else {
            // 请求权限
            requestPermissionLauncher.launch(permission);
        }
    }

    /**
     * 打开相册选择照片
     */
    private void openGallery() {
        // 见第 4 节
    }
}
```

**关键点解释：**

1. **为什么要运行时请求权限？**
   - Android 6.0+ 引入"运行时权限"
   - 用户可以在设置中随时撤销权限
   - 必须在使用功能前检查和请求

2. **为什么要判断 Android 版本？**
   - 不同版本的权限名称不同
   - 旧版本请求新权限会崩溃

3. **ActivityResultLauncher 是什么？**
   - 新的权限请求方式（替代旧的 `requestPermissions()`）
   - 使用回调处理结果，更安全

---

## 4. 如何访问相册

### 4.1 方法 1：使用系统相册选择器（推荐）

```java
import android.content.Intent;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class HomeActivity extends AppCompatActivity {

    private ActivityResultLauncher<String> pickImageLauncher;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 设置图片选择器
        setupImagePicker();

        // 按钮点击
        findViewById(R.id.btn_select_photo).setOnClickListener(v -> {
            checkAndRequestPermission();  // 见上一节
        });
    }

    /**
     * 设置图片选择器
     */
    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    // 加载图片到编辑器
                    loadImageForEditing(uri);
                }
            }
        );
    }

    /**
     * 打开相册
     */
    private void openGallery() {
        // 只选择图片类型
        pickImageLauncher.launch("image/*");
    }

    /**
     * 加载图片进行编辑
     */
    private void loadImageForEditing(Uri imageUri) {
        try {
            // 方法 1：使用 Bitmap
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                getContentResolver(), imageUri);
            
            // 方法 2：使用 ImageDecoder（Android 10+）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = 
                    ImageDecoder.createSource(getContentResolver(), imageUri);
                Bitmap bitmap2 = ImageDecoder.decodeBitmap(source);
            }
            
            // 跳转到编辑页面
            Intent intent = new Intent(this, EditActivity.class);
            // 传递图片 URI（推荐）
            intent.putExtra("image_uri", imageUri.toString());
            startActivity(intent);
            
        } catch (IOException e) {
            Toast.makeText(this, "加载图片失败", Toast.LENGTH_SHORT).show();
        }
    }
}
```

**为什么用 GetContent 而不是手动读取？**
- ✅ 系统提供统一的选择界面
- ✅ 自动处理权限
- ✅ 支持云存储（Google Photos）
- ✅ 不需要手动遍历文件

### 4.2 方法 2：使用相机拍照（扩展）

```java
private ActivityResultLauncher<Uri> takePictureLauncher;
private Uri photoUri;

private void setupCamera() {
    takePictureLauncher = registerForActivityResult(
        new ActivityResultContracts.TakePicture(),
        success -> {
            if (success) {
                // 照片已保存到 photoUri
                loadImageForEditing(photoUri);
            }
        }
    );
}

private void takePicture() {
    // 创建临时文件
    File photoFile = createImageFile();
    photoUri = FileProvider.getUriForFile(this,
        "com.example.photoshop_demo.fileprovider", photoFile);
    
    // 启动相机
    takePictureLauncher.launch(photoUri);
}

private File createImageFile() throws IOException {
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", 
        Locale.getDefault()).format(new Date());
    String imageFileName = "JPEG_" + timeStamp + "_";
    File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    return File.createTempFile(imageFileName, ".jpg", storageDir);
}
```

---

## 5. 照片存储最佳实践

### 5.1 临时文件存储（编辑过程中）

```java
/**
 * 保存临时编辑文件
 * 用途：用户正在编辑，还没决定是否保存
 * 位置：应用私有目录（卸载后删除）
 */
private File saveTempEditedImage(Bitmap bitmap) {
    // 获取应用私有目录
    File tempDir = new File(getExternalFilesDir(null), "temp");
    if (!tempDir.exists()) {
        tempDir.mkdirs();  // 创建目录
    }

    // 生成文件名
    String fileName = "temp_" + System.currentTimeMillis() + ".jpg";
    File imageFile = new File(tempDir, fileName);

    try (FileOutputStream fos = new FileOutputStream(imageFile)) {
        // 压缩并保存
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        fos.flush();
        return imageFile;
    } catch (IOException e) {
        e.printStackTrace();
        return null;
    }
}

// 完整路径示例：
// /sdcard/Android/data/com.example.photoshop_demo/files/temp/temp_1703419200000.jpg
```

**为什么用应用私有目录？**
- ✅ 不需要权限（Android 10+）
- ✅ 不会弄乱用户的相册
- ✅ 卸载应用自动清理
- ✅ 其他应用无法访问

### 5.2 保存到相册（用户点击"保存"）

#### Android 10+ 方法（MediaStore）- 推荐

```java
import android.content.ContentValues;
import android.provider.MediaStore;
import android.os.Environment;

/**
 * 保存编辑后的照片到相册
 * Android 10+ 方法（推荐）
 */
private void saveImageToGallery(Bitmap bitmap) {
    // 准备图片信息
    ContentValues values = new ContentValues();
    values.put(MediaStore.Images.Media.DISPLAY_NAME, 
        "Edited_" + System.currentTimeMillis() + ".jpg");
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
    
    // Android 10+ 保存到特定目录
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        values.put(MediaStore.Images.Media.RELATIVE_PATH, 
            Environment.DIRECTORY_PICTURES + "/Photoshop_demo");
        // 路径：/sdcard/Pictures/Photoshop_demo/
    }

    // 插入到媒体库
    Uri imageUri = getContentResolver().insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

    if (imageUri != null) {
        try (OutputStream outputStream = 
                getContentResolver().openOutputStream(imageUri)) {
            // 保存图片
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);
            
            Toast.makeText(this, "已保存到相册", Toast.LENGTH_SHORT).show();
            
            // 通知系统扫描新文件（Android 10 以下需要）
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));
            }
            
        } catch (IOException e) {
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }
}
```

**MediaStore 的优势：**
- ✅ 官方推荐方式
- ✅ 兼容分区存储（Scoped Storage）
- ✅ 自动显示在相册中
- ✅ 系统自动管理权限

#### Android 9 及以下方法（传统方式）

```java
/**
 * 保存到相册（Android 9 及以下）
 * 需要 WRITE_EXTERNAL_STORAGE 权限
 */
private void saveImageToGalleryLegacy(Bitmap bitmap) {
    // 获取 Pictures 目录
    File picturesDir = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_PICTURES);
    File appDir = new File(picturesDir, "Photoshop_demo");
    
    if (!appDir.exists()) {
        appDir.mkdirs();
    }

    String fileName = "Edited_" + System.currentTimeMillis() + ".jpg";
    File imageFile = new File(appDir, fileName);

    try (FileOutputStream fos = new FileOutputStream(imageFile)) {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
        
        // 通知媒体库扫描
        MediaScannerConnection.scanFile(this,
            new String[]{imageFile.getAbsolutePath()},
            new String[]{"image/jpeg"}, null);
        
        Toast.makeText(this, "已保存到相册", Toast.LENGTH_SHORT).show();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

### 5.3 完整的保存方法（兼容所有版本）

```java
/**
 * 保存图片到相册（兼容所有 Android 版本）
 */
private void saveToGallery(Bitmap bitmap) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Android 10+ 使用 MediaStore
        saveImageToGallery(bitmap);
    } else {
        // Android 9 及以下使用传统方式
        // 需要先检查 WRITE_EXTERNAL_STORAGE 权限
        if (ContextCompat.checkSelfPermission(this, 
                Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                == PackageManager.PERMISSION_GRANTED) {
            saveImageToGalleryLegacy(bitmap);
        } else {
            // 请求写入权限
            requestWritePermission();
        }
    }
}
```

---

## 6. 完整代码示例

### 6.1 EditActivity 中的保存功能

```java
public class EditActivity extends AppCompatActivity {

    private Bitmap editedBitmap;  // 编辑后的图片

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // 加载传入的图片
        loadImageFromIntent();

        // 导出按钮
        findViewById(R.id.btn_export).setOnClickListener(v -> {
            if (editedBitmap != null) {
                saveToGallery(editedBitmap);
            }
        });
    }

    /**
     * 从 Intent 加载图片
     */
    private void loadImageFromIntent() {
        String uriString = getIntent().getStringExtra("image_uri");
        if (uriString != null) {
            Uri imageUri = Uri.parse(uriString);
            try {
                editedBitmap = MediaStore.Images.Media.getBitmap(
                    getContentResolver(), imageUri);
                // 显示在 ImageView 中
                // imageView.setImageBitmap(editedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存到相册
     */
    private void saveToGallery(Bitmap bitmap) {
        // 使用上面的方法
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, 
            "Edited_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, 
                Environment.DIRECTORY_PICTURES + "/Photoshop_demo");
        }

        Uri imageUri = getContentResolver().insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (imageUri != null) {
            try (OutputStream os = getContentResolver().openOutputStream(imageUri)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, os);
                Toast.makeText(this, "已保存到相册", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
```

### 6.2 存储路径总结图

```
📱 Android 存储结构 for 图片编辑应用

应用内资源（不可写）
└── res/drawable/
    └── ic_camera.xml                    # 应用图标（APK 内）

应用私有存储（临时文件）
└── /sdcard/Android/data/com.example.photoshop_demo/
    ├── files/
    │   └── temp/
    │       └── temp_1234567890.jpg      # 编辑中的图片 ✅
    └── cache/
        └── thumbnail.jpg                 # 缩略图缓存

用户相册（永久保存）
└── /sdcard/Pictures/
    └── Photoshop_demo/
        ├── Edited_1234567890.jpg        # 保存的作品 ✅
        └── Edited_1234567891.jpg
```

---

## 7. 常见问题 FAQ

### Q1: res/ 目录可以存储用户照片吗？
**A:** ❌ **不可以**！
- res/ 是只读的，编译到 APK 中
- 只能存放应用自带的资源
- 用户照片必须存到外部存储

### Q2: 编辑中的临时文件存哪里？
**A:** 应用私有目录
```java
File tempDir = new File(getExternalFilesDir(null), "temp");
// /sdcard/Android/data/包名/files/temp/
```
**原因：**
- 不需要权限
- 不会出现在相册
- 卸载自动删除

### Q3: 保存到相册后为什么看不到？
**A:** 可能原因：
1. 没有触发媒体扫描（Android 9 以下）
2. 使用了错误的保存方式
3. 权限不足

**解决：** 使用 MediaStore API（Android 10+）

### Q4: Android 13 权限被拒绝怎么办？
**A:** 检查：
1. 是否在 Manifest 中声明 `READ_MEDIA_IMAGES`
2. 是否在运行时请求权限
3. 用户是否在设置中永久拒绝

### Q5: 如何在编辑页显示图片？
**A:** 使用 URI 传递
```java
// HomeActivity
intent.putExtra("image_uri", imageUri.toString());

// EditActivity
String uriString = getIntent().getStringExtra("image_uri");
Uri imageUri = Uri.parse(uriString);
Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
```

### Q6: 保存图片质量如何控制？
**A:** 
```java
bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);
//                                          ^^
//                                      质量 0-100
// 95：高质量，文件较大
// 80：平衡
// 60：低质量，文件小
```

### Q7: 如何减小图片文件大小？
**A:** 
```java
// 方法 1：降低质量
bitmap.compress(Bitmap.CompressFormat.JPEG, 80, os);

// 方法 2：缩小尺寸
Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 
    bitmap.getWidth() / 2, bitmap.getHeight() / 2, true);

// 方法 3：使用 WebP 格式（更小）
bitmap.compress(Bitmap.CompressFormat.WEBP, 80, os);
```

---

## 8. 实现路线图

### 阶段 1：基础功能（当前）
- ✅ UI 布局完成
- ⏳ 权限申请
- ⏳ 相册访问
- ⏳ 图片显示

### 阶段 2：编辑功能
- ⏳ 图片加载到编辑器
- ⏳ 基础滤镜（黑白、怀旧等）
- ⏳ 调整亮度、对比度
- ⏳ 裁剪、旋转

### 阶段 3：保存功能
- ⏳ 保存到临时目录
- ⏳ 导出到相册
- ⏳ 分享到其他应用

### 阶段 4：高级功能
- ⏳ AI 功能（调色、美颜）
- ⏳ 图层管理
- ⏳ 撤销/重做
- ⏳ 模板和预设

---

## 9. 代码清单

当你准备实现时，需要修改以下文件：

### 9.1 需要添加的文件

```
app/src/main/java/com/example/photoshop_demo/
├── HomeActivity.java           # 添加权限请求、相册选择
├── EditActivity.java           # 添加图片加载、保存功能
└── utils/
    ├── PermissionHelper.java   # 权限管理工具类
    └── ImageSaver.java         # 图片保存工具类
```

### 9.2 需要修改的文件

```
app/src/main/
├── AndroidManifest.xml         # 添加权限声明
└── res/
    └── xml/
        └── file_paths.xml      # FileProvider 配置（相机拍照需要）
```

### 9.3 需要添加的依赖（可选）

```kotlin
// build.gradle.kts (app)
dependencies {
    // 图片加载库（推荐）
    implementation("com.github.bumptech.glide:glide:4.16.0")
    
    // 图片裁剪库（可选）
    implementation("com.github.yalantis:ucrop:2.2.8")
}
```

---

## 10. 总结

### 核心要点

1. **res/ ≠ 用户数据存储**
   - res/ 是应用资源（只读）
   - 用户数据存外部存储

2. **临时文件 → 应用私有目录**
   ```
   getExternalFilesDir(null) + "/temp/"
   ```

3. **永久保存 → MediaStore**
   ```
   Pictures/Photoshop_demo/
   ```

4. **权限要分版本处理**
   - Android 13+: READ_MEDIA_IMAGES
   - Android 6-12: READ_EXTERNAL_STORAGE
   - Android 10+: 写入用 MediaStore，不需要权限

5. **使用系统 API 而不是手动读取**
   - GetContent: 选择图片
   - MediaStore: 保存到相册
   - FileProvider: 分享文件

### 推荐阅读
- [Android 官方文档 - 存储](https://developer.android.com/training/data-storage)
- [Android 官方文档 - 权限](https://developer.android.com/training/permissions/requesting)
- [Android 官方文档 - MediaStore](https://developer.android.com/training/data-storage/shared/media)

---

**准备好实现这些功能时，随时告诉我！我可以帮你一步步添加代码。** 🚀

