# Android 应用开发详细教程 - 美图编辑器

## 📚 目录
1. [Android 应用基础概念](#1-android-应用基础概念)
2. [项目结构详解](#2-项目结构详解)
3. [Activity 详解](#3-activity-详解)
4. [XML 布局详解](#4-xml-布局详解)
5. [资源文件详解](#5-资源文件详解)
6. [代码逐行解释](#6-代码逐行解释)

---

## 1. Android 应用基础概念
### 1.1 什么是 Android 应用？
Android 应用由多个组件组成，主要包括：
- **Activity**（活动）：用户可以看到和交互的屏幕
- **Layout**（布局）：定义屏幕的外观和结构
- **Resources**（资源）：颜色、字符串、图片等
- **Manifest**（清单）：应用的配置文件
### 1.2 MVC 设计模式
我们的应用使用了类似 MVC 的结构：
- **Model**（模型）：数据和业务逻辑（本项目暂未实现）
- **View**（视图）：XML 布局文件
- **Controller**（控制器）：Activity Java 代码
---

## 2. 项目结构详解

```
Photoshop_demo/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/                    # Java 源代码目录
│   │       │   └── com/example/photoshop_demo/
│   │       │       ├── HomeActivity.java      # 首页逻辑
│   │       │       └── EditActivity.java      # 编辑页逻辑
│   │       │
│   │       ├── res/                     # 资源文件目录
│   │       │   ├── drawable/            # 可绘制资源（图标、形状）
│   │       │   ├── layout/              # 布局文件
│   │       │   ├── values/              # 值资源（颜色、字符串）
│   │       │   └── mipmap/              # 应用图标
│   │       │
│   │       └── AndroidManifest.xml      # 应用清单文件
│   │
│   └── build.gradle.kts                 # 应用级构建配置
│
├── build.gradle.kts                     # 项目级构建配置
└── settings.gradle.kts                  # 项目设置
```

### 为什么要这样组织？
- **分离关注点**：代码、布局、资源分开，便于维护
- **资源管理**：Android 系统可以根据设备自动选择合适的资源
- **团队协作**：设计师负责 XML 布局，程序员负责 Java 代码

---

## 3. Activity 详解

### 3.1 什么是 Activity？
**Activity** 是 Android 应用的基本单元，代表一个屏幕。

**生命周期**：
```
onCreate() → onStart() → onResume() → 运行中
    ↓                                    ↓
onDestroy() ← onStop() ← onPause() ← 用户离开
```

### 3.2 HomeActivity 代码详解

```java
package com.example.photoshop_demo;
// 包名：定义代码的命名空间，防止类名冲突

import android.content.Intent;
// Intent：用于在不同 Activity 之间传递信息和跳转

import android.os.Bundle;
// Bundle：保存和恢复 Activity 状态的数据容器

import android.view.View;
// View：所有 UI 组件的基类

import androidx.appcompat.app.AppCompatActivity;
// AppCompatActivity：提供向后兼容的 Activity 基类

public class HomeActivity extends AppCompatActivity {
    // extends：继承 AppCompatActivity，获得 Activity 的所有功能

    @Override
    // @Override：注解，表示重写父类方法
    
    protected void onCreate(Bundle savedInstanceState) {
        // onCreate()：Activity 创建时调用的方法
        // Bundle savedInstanceState：保存之前的状态（如屏幕旋转后恢复数据）
        
        super.onCreate(savedInstanceState);
        // super：调用父类的 onCreate()，必须先调用
        
        setContentView(R.layout.activity_home);
        // 设置这个 Activity 的布局文件
        // R.layout.activity_home 指向 res/layout/activity_home.xml
        // R：自动生成的资源类，包含所有资源的 ID

        // 为按钮设置点击监听器
        findViewById(R.id.btn_collage).setOnClickListener(v -> {
            // findViewById()：通过 ID 查找布局中的视图
            // R.id.btn_collage：布局文件中定义的按钮 ID
            // setOnClickListener()：设置点击事件处理器
            // v -> {...}：Lambda 表达式，Java 8 的简洁写法
            
            Intent intent = new Intent(HomeActivity.this, EditActivity.class);
            // Intent：意图，用于启动另一个 Activity
            // HomeActivity.this：当前 Activity 的上下文
            // EditActivity.class：要启动的目标 Activity
            
            startActivity(intent);
            // 启动 EditActivity
        });
    }
}
```

### 3.3 为什么要用这些？

| 组件 | 作用 | 不用会怎样？ |
|------|------|-------------|
| `AppCompatActivity` | 提供向后兼容的功能 | 旧设备上功能缺失 |
| `onCreate()` | 初始化 Activity | 界面无法显示 |
| `setContentView()` | 加载布局 | 屏幕是空白的 |
| `findViewById()` | 获取 UI 组件 | 无法操作界面元素 |
| `Intent` | Activity 间通信 | 无法跳转页面 |

---

## 4. XML 布局详解

### 4.1 什么是 XML 布局？
XML 是一种标记语言，用于定义 UI 的结构和外观。

**为什么用 XML 而不是纯代码？**
- ✅ 可视化：可以在设计器中预览
- ✅ 分离：逻辑和界面分开
- ✅ 复用：布局可以被多次使用
- ✅ 适配：系统自动处理不同屏幕

### 4.2 布局容器类型

#### RelativeLayout（相对布局）
```xml
<RelativeLayout>
    <!-- 子视图相对于父视图或其他子视图定位 -->
    <View 
        android:layout_below="@id/other_view"     <!-- 在某个视图下方 -->
        android:layout_alignParentTop="true" />    <!-- 对齐父视图顶部 -->
</RelativeLayout>
```
**优点**：灵活，适合复杂布局  
**缺点**：嵌套多时性能较差

#### LinearLayout（线性布局）
```xml
<LinearLayout 
    android:orientation="vertical">  <!-- 垂直排列 -->
    <!-- 子视图按顺序排列 -->
</LinearLayout>
```
**优点**：简单，性能好  
**缺点**：只能单方向排列

### 4.3 activity_home.xml 关键部分解析

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- XML 声明：版本和编码 -->

<RelativeLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    <!-- xmlns：XML 命名空间，定义 android 属性的来源 -->
    
    android:layout_width="match_parent"
    <!-- match_parent：宽度填满父视图 -->
    
    android:layout_height="match_parent"
    <!-- match_parent：高度填满父视图 -->
    
    android:background="@color/black">
    <!-- background：背景颜色，引用 colors.xml 中定义的颜色 -->

    <!-- 顶部横幅 -->
    <RelativeLayout
        android:id="@+id/banner_container"
        <!-- id：给视图分配唯一标识符，供代码和其他视图引用 -->
        <!-- @+id/：创建新 ID -->
        
        android:layout_width="match_parent"
        android:layout_height="200dp"
        <!-- dp：密度无关像素，在不同屏幕上保持相同物理尺寸 -->
        
        android:background="@color/christmas_red"
        android:padding="20dp">
        <!-- padding：内边距，内容与边框的距离 -->

        <TextView
            <!-- TextView：显示文本的组件 -->
            
            android:id="@+id/banner_title"
            android:layout_width="wrap_content"
            <!-- wrap_content：宽度适应内容 -->
            
            android:layout_height="wrap_content"
            android:text="让这个圣诞，拼出彩"
            <!-- text：显示的文字 -->
            
            android:textColor="@color/white"
            android:textSize="24sp"
            <!-- sp：可缩放像素，用于文字大小，用户可以调整 -->
            
            android:textStyle="bold"
            android:layout_marginTop="20dp"/>
            <!-- margin：外边距，与其他视图的距离 -->
    </RelativeLayout>

    <!-- 四个功能按钮 -->
    <LinearLayout
        android:id="@+id/function_buttons"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/banner_container"
        <!-- layout_below：RelativeLayout 属性，放在某个视图下方 -->
        
        android:orientation="horizontal"
        <!-- horizontal：水平排列子视图 -->
        
        android:gravity="center">
        <!-- gravity：内容对齐方式（内部元素） -->

        <!-- 单个按钮 -->
        <LinearLayout
            android:id="@+id/btn_ai_color"
            android:layout_width="0dp"
            <!-- 0dp：配合 weight 使用，平均分配空间 -->
            
            android:layout_height="wrap_content"
            android:layout_weight="1"
            <!-- weight：权重，4 个按钮都是 1，所以平均分配空间 -->
            
            android:orientation="vertical"
            android:gravity="center"
            android:clickable="true"
            <!-- clickable：可以点击 -->
            
            android:focusable="true">
            <!-- focusable：可以获得焦点（如用遥控器导航） -->

            <View
                android:layout_width="70dp"
                android:layout_height="70dp"
                android:background="@drawable/circle_button_bg"/>
                <!-- 引用自定义的圆形背景 -->

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="AI追色"
                android:textColor="@color/white"
                android:textSize="14sp"
                android:layout_marginTop="8dp"/>
        </LinearLayout>
        
        <!-- 其他 3 个按钮类似... -->
    </LinearLayout>
</RelativeLayout>
```

### 4.4 尺寸单位说明

| 单位 | 说明 | 用途 |
|------|------|------|
| `dp` (dip) | 密度无关像素 | 布局尺寸、间距 |
| `sp` | 可缩放像素 | 文字大小 |
| `px` | 像素 | ❌ 不建议使用 |
| `match_parent` | 填满父视图 | 宽度/高度 |
| `wrap_content` | 适应内容 | 宽度/高度 |

**为什么用 dp 而不是 px？**
```
假设：
- 低密度屏幕：160 dpi → 1dp = 1px
- 高密度屏幕：320 dpi → 1dp = 2px
- 超高密度屏幕：480 dpi → 1dp = 3px

结果：使用 dp，按钮在所有屏幕上物理尺寸相同
```

---

## 5. 资源文件详解

### 5.1 colors.xml（颜色资源）

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="christmas_red">#C93A3A</color>
    <!-- 
    格式：#RRGGBB 或 #AARRGGBB
    - #：十六进制颜色标记
    - AA：透明度（可选，00=完全透明，FF=完全不透明）
    - RR：红色分量（00-FF）
    - GG：绿色分量（00-FF）
    - BB：蓝色分量（00-FF）
    
    例如：#C93A3A = RGB(201, 58, 58) = 深红色
    -->
</resources>
```

**为什么要单独定义颜色？**
- ✅ 统一：全局修改一次，所有地方都变
- ✅ 维护：不用在代码里搜索颜色值
- ✅ 主题：可以定义日间/夜间主题

### 5.2 strings.xml（字符串资源）

```xml
<resources>
    <string name="app_name">美图编辑器</string>
    <string name="ai_color">AI追色</string>
</resources>
```

**为什么要单独定义字符串？**
- ✅ 国际化：轻松翻译成多种语言
- ✅ 维护：统一管理所有文字
- ✅ 复用：同一个字符串多处使用

**国际化示例**：
```
res/
├── values/              # 默认（中文）
│   └── strings.xml
├── values-en/           # 英语
│   └── strings.xml
└── values-ja/           # 日语
    └── strings.xml
```

### 5.3 drawable 资源（可绘制对象）

#### circle_button_bg.xml（圆形背景）

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <!-- shape：定义形状类型
         - rectangle：矩形
         - oval：椭圆/圆形
         - line：线
         - ring：环形
    -->
    
    <solid android:color="@color/dark_gray"/>
    <!-- solid：填充颜色 -->
    
    <stroke
        android:width="1dp"
        android:color="@color/gray"/>
    <!-- stroke：边框
         - width：边框宽度
         - color：边框颜色
    -->
</shape>
```

#### rounded_button_bg.xml（圆角矩形）

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    
    <solid android:color="@color/dark_gray"/>
    
    <corners android:radius="18dp"/>
    <!-- corners：圆角
         - radius：所有角的圆角半径
         - topLeftRadius：单独设置某个角
    -->
    
    <stroke
        android:width="1dp"
        android:color="@color/yellow"/>
</shape>
```

**为什么用 XML 定义形状？**
- ✅ 无需图片：减小 APK 大小
- ✅ 可缩放：矢量图形，任意大小清晰
- ✅ 可修改：改颜色、尺寸非常容易
- ✅ 性能好：系统直接绘制

---

## 6. 代码逐行解释

### 6.1 HomeActivity.java 完整解析

```java
// 第 1 行：包声明
package com.example.photoshop_demo;
/*
作用：定义类所在的包（命名空间）
为什么：防止类名冲突，组织代码结构
规则：通常是 com.公司名.应用名
*/

// 第 3-7 行：导入语句
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import androidx.appcompat.app.AppCompatActivity;
/*
作用：导入需要使用的类
为什么：Java 需要明确声明使用的外部类
类比：在 Python 中的 import
*/

// 第 9 行：类声明
public class HomeActivity extends AppCompatActivity {
/*
- public：访问修饰符，表示类可以被外部访问
- class：声明一个类
- HomeActivity：类名，必须与文件名一致
- extends：继承关键字
- AppCompatActivity：父类，提供 Activity 的所有功能

为什么继承 AppCompatActivity？
- 获得 Activity 的生命周期管理
- 获得界面显示能力
- 获得向后兼容支持
*/

    // 第 11-14 行：onCreate 方法
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    /*
    @Override：注解，表示重写父类方法
    protected：访问修饰符，子类和同包可访问
    void：返回类型，不返回任何值
    onCreate：方法名，Activity 创建时调用
    Bundle：保存状态的数据结构
    
    super.onCreate()：必须调用，初始化父类
    setContentView()：设置布局文件
    R.layout.activity_home：自动生成的资源 ID
    */

        // 第 16-19 行：设置按钮点击事件
        findViewById(R.id.btn_ai_color).setOnClickListener(v -> {
            // AI追色功能
        });
        /*
        findViewById()：
        - 作用：通过 ID 查找视图对象
        - 参数：资源 ID（R.id.btn_ai_color）
        - 返回：View 对象
        
        setOnClickListener()：
        - 作用：设置点击监听器
        - 参数：监听器对象（Lambda 表达式）
        
        Lambda 表达式 v -> {...}：
        - v：被点击的视图（View）
        - ->：箭头操作符
        - {...}：点击后执行的代码
        
        等价于传统写法：
        findViewById(R.id.btn_ai_color).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // AI追色功能
                }
            }
        );
        */

        // 第 29-33 行：跳转到编辑页
        findViewById(R.id.btn_collage).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, EditActivity.class);
            startActivity(intent);
        });
        /*
        Intent：意图对象
        - 作用：在组件之间传递信息和启动组件
        - 参数 1：Context（上下文），当前 Activity
        - 参数 2：目标 Activity 的 Class 对象
        
        HomeActivity.this：
        - 当前 Activity 的引用
        - 为什么不用 this？在 Lambda 内，this 指向 Lambda 对象
        
        startActivity()：
        - 作用：启动新的 Activity
        - 参数：Intent 对象
        */
    }
}
```

### 6.2 EditActivity.java 完整解析

```java
package com.example.photoshop_demo;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class EditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        // 加载编辑页布局

        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> {
            finish();
        });
        /*
        finish()：
        - 作用：结束当前 Activity
        - 效果：返回上一个 Activity（HomeActivity）
        - 生命周期：onPause() → onStop() → onDestroy()
        */

        // 其他按钮的点击事件
        findViewById(R.id.btn_undo).setOnClickListener(v -> {
            // 撤销操作
        });
        // 目前只是占位，实际功能需要实现

        // ... 其他按钮类似
    }
}
```

### 6.3 AndroidManifest.xml 详解

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    <!-- manifest：清单文件根元素，声明应用的所有组件 -->

    <application
        android:allowBackup="true"
        <!-- 允许备份应用数据 -->
        
        android:icon="@mipmap/ic_launcher"
        <!-- 应用图标 -->
        
        android:label="@string/app_name"
        <!-- 应用名称 -->
        
        android:theme="@style/Theme.Photoshop_demo">
        <!-- 应用主题 -->
        
        <!-- 首页 Activity -->
        <activity
            android:name=".HomeActivity"
            <!-- Activity 类名，.表示包名前缀 -->
            
            android:exported="true">
            <!-- exported：是否可以被其他应用启动
                 true：可以（因为是启动页）
                 false：不可以（内部 Activity）
            -->
            
            <intent-filter>
                <!-- Intent 过滤器：声明 Activity 可以响应的 Intent -->
                
                <action android:name="android.intent.action.MAIN" />
                <!-- MAIN：主入口 -->
                
                <category android:name="android.intent.category.LAUNCHER" />
                <!-- LAUNCHER：在启动器中显示 -->
                
                <!-- 这两行合起来表示：这是应用的启动页 -->
            </intent-filter>
        </activity>
        
        <!-- 编辑页 Activity -->
        <activity
            android:name=".EditActivity"
            android:exported="false" />
            <!-- false：只能从应用内部启动 -->
    </application>

</manifest>

/*
为什么需要 AndroidManifest.xml？
1. 系统需要知道应用有哪些组件
2. 系统需要知道启动哪个 Activity
3. 声明应用需要的权限（如网络、相机）
4. 声明应用的配置（图标、名称、主题）

如果不在 Manifest 中声明 Activity 会怎样？
- 编译通过
- 运行时崩溃：android.content.ActivityNotFoundException
*/
```

### 6.4 build.gradle.kts 详解

```kotlin
plugins {
    alias(libs.plugins.android.application)
}
// 应用 Android 应用插件，提供构建 Android 应用的功能

android {
    namespace = "com.example.photoshop_demo"
    // 应用的包名（命名空间）
    
    compileSdk = 34
    // 编译 SDK 版本：使用哪个版本的 Android SDK 编译
    // 34 = Android 14
    
    defaultConfig {
        applicationId = "com.example.photoshop_demo"
        // 应用 ID：在设备上唯一标识应用
        // 用于应用商店、系统识别
        
        minSdk = 24
        // 最低 SDK 版本：应用可以运行的最低 Android 版本
        // 24 = Android 7.0
        // 低于此版本的设备无法安装
        
        targetSdk = 34
        // 目标 SDK 版本：应用测试过的最高版本
        // 系统会根据此版本应用兼容性行为
        
        versionCode = 1
        // 版本号：内部版本号，整数，用于更新判断
        
        versionName = "1.0"
        // 版本名：显示给用户的版本号
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    // Java 版本兼容性设置
}

dependencies {
    implementation(libs.appcompat)
    // AndroidX AppCompat 库：向后兼容支持
    
    implementation(libs.material)
    // Material Design 组件库：提供现代 UI 组件
    
    testImplementation(libs.junit)
    // JUnit：单元测试框架
    
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Android 测试框架
}

/*
为什么需要这些依赖？
- appcompat：让应用在旧版 Android 上使用新功能
- material：使用 Google 的 Material Design 组件

SDK 版本选择建议：
- minSdk：根据目标用户决定（24 覆盖 94% 设备）
- targetSdk：使用最新稳定版（获得最新功能和安全性）
- compileSdk：至少等于 targetSdk
*/
```

---

## 7. 核心概念总结

### 7.1 Activity 生命周期

```
用户打开应用：
    onCreate() → 创建 Activity，加载布局
    onStart() → Activity 可见
    onResume() → Activity 可交互
    ↓
[运行中 - 用户可以操作]
    ↓
用户按返回键：
    onPause() → 失去焦点
    onStop() → 不可见
    onDestroy() → 销毁
```

**为什么要有生命周期？**
- 系统资源有限，需要管理内存
- 保存和恢复用户数据
- 响应系统事件（如来电、息屏）

### 7.2 R 类（资源类）

```java
R.layout.activity_home  // res/layout/activity_home.xml
R.id.btn_collage        // XML 中的 android:id="@+id/btn_collage"
R.color.black           // res/values/colors.xml 中的颜色
R.string.app_name       // res/values/strings.xml 中的字符串
R.drawable.circle_bg    // res/drawable/circle_bg.xml
```

**R 类的作用：**
- 自动生成，不要手动编辑
- 为每个资源分配唯一的整数 ID
- 提供类型安全的资源访问

### 7.3 常用 View 组件

| 组件 | 作用 | 示例 |
|------|------|------|
| `TextView` | 显示文本 | 标题、说明 |
| `Button` | 按钮 | 提交、取消 |
| `ImageView` | 显示图片 | 头像、照片 |
| `EditText` | 文本输入 | 用户名、密码 |
| `RecyclerView` | 列表 | 聊天记录、商品列表 |
| `ScrollView` | 滚动容器 | 长文章 |

### 7.4 布局参数对比

```xml
<!-- 宽度 -->
android:layout_width="match_parent"    <!-- 填满父视图 -->
android:layout_width="wrap_content"    <!-- 适应内容 -->
android:layout_width="100dp"           <!-- 固定尺寸 -->
android:layout_width="0dp"             <!-- 配合 weight 使用 -->

<!-- 对齐 -->
android:gravity="center"               <!-- 内容居中（内部元素） -->
android:layout_gravity="center"        <!-- 自身居中（在父视图中） -->

<!-- 间距 -->
android:padding="20dp"                 <!-- 内边距（内容与边框） -->
android:margin="20dp"                  <!-- 外边距（与其他视图） -->
```

---

## 8. 学习路线建议

### 8.1 已掌握（本项目）
- ✅ Activity 基础
- ✅ XML 布局
- ✅ 资源管理
- ✅ Intent 导航

### 8.2 下一步学习
1. **数据绑定**：ViewBinding / DataBinding
2. **列表显示**：RecyclerView
3. **网络请求**：Retrofit / OkHttp
4. **数据存储**：SharedPreferences / Room 数据库
5. **异步处理**：Coroutines / RxJava
6. **图片加载**：Glide / Picasso
7. **现代架构**：MVVM / Jetpack Compose

### 8.3 实践建议
1. 修改颜色、文字，观察效果
2. 添加新的按钮和功能
3. 尝试不同的布局方式
4. 阅读官方文档
5. 做小项目练手

---

## 9. 常见问题 FAQ

### Q1: 为什么 XML 中要用 @？
**A:** `@` 表示引用资源
- `@id/`：引用 ID
- `@+id/`：创建新 ID
- `@color/`：引用颜色
- `@string/`：引用字符串

### Q2: findViewById() 每次都要调用吗？
**A:** 不推荐。现代做法是使用 ViewBinding：
```java
// ViewBinding 方式（推荐）
ActivityHomeBinding binding = ActivityHomeBinding.inflate(getLayoutInflater());
setContentView(binding.getRoot());
binding.btnCollage.setOnClickListener(v -> {...});
```

### Q3: 如何在 Activity 间传递数据？
**A:** 使用 Intent 的 extras：
```java
// 发送数据
Intent intent = new Intent(this, EditActivity.class);
intent.putExtra("image_path", "/path/to/image");
startActivity(intent);

// 接收数据
String imagePath = getIntent().getStringExtra("image_path");
```

### Q4: 为什么要用 dp 而不是 px？
**A:** dp 会根据屏幕密度自动缩放，保证在不同设备上物理尺寸相同。

### Q5: 如何调试 Android 应用？
**A:** 
- 使用 `Log.d("TAG", "message")` 打印日志
- 在代码中设置断点
- 使用 Android Studio 的 Logcat 查看日志

---

## 10. 总结

### 核心要点
1. **Activity** 是应用的基本单元，每个屏幕一个 Activity
2. **XML 布局** 定义界面外观，与代码分离
3. **资源文件** 统一管理颜色、字符串、图片等
4. **AndroidManifest** 声明应用的所有组件
5. **Intent** 用于 Activity 间的导航和通信

### 设计原则
- **分离关注点**：UI 和逻辑分开
- **资源复用**：颜色、字符串定义一次，多处使用
- **向后兼容**：使用 AppCompat 支持旧设备
- **响应式设计**：使用 dp/sp 适配不同屏幕

### 继续学习
- 官方文档：https://developer.android.com
- 视频教程：YouTube、B站搜索"Android 开发"
- 实践项目：做一个简单的待办事项应用

---

**祝你学习愉快！有问题随时问我。** 🚀

