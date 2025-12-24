# Photoshop Demo - 美图编辑器

这是一个Android图片编辑应用的演示项目，包含首页和编辑页两个主要界面。

## 项目结构

### Activity 列表

1. **HomeActivity** (首页)
   - 文件: `app/src/main/java/com/example/photoshop_demo/HomeActivity.java`
   - 布局: `app/src/main/res/layout/activity_home.xml`
   - 功能: 
     - 圣诞主题横幅广告
     - 四个圆形功能按钮（AI追色、相机模拟、特效、拼图）
     - 照片/项目标签切换
     - 照片网格展示

2. **EditActivity** (编辑页)
   - 文件: `app/src/main/java/com/example/photoshop_demo/EditActivity.java`
   - 布局: `app/src/main/res/layout/activity_edit.xml`
   - 功能:
     - 顶部工具栏（返回、撤销、更多、AI创意、导出）
     - 图片显示区域
     - 实时调色开关
     - 底部编辑工具栏（AI色彩调节、AI追色、滤镜、白平衡、影调）

### 资源文件

#### Drawable 资源
- `circle_button_bg.xml` - 圆形按钮背景
- `rounded_button_bg.xml` - 圆角按钮背景（带黄色边框）
- `rounded_button_dark_bg.xml` - 深色圆角按钮背景

#### Colors 配置
- `christmas_red` - 圣诞红色主题
- `dark_gray` - 深灰色背景
- `yellow` - 黄色强调色
- 等其他界面所需颜色

## 运行说明

### 前提条件
- Android Studio (最新版本)
- Android SDK API 24 或更高
- Java 11

### 运行步骤

1. **打开项目**
   ```
   在 Android Studio 中打开项目文件夹
   ```

2. **同步 Gradle**
   - 等待 Android Studio 自动同步 Gradle 文件
   - 或者手动点击 "Sync Project with Gradle Files"

3. **运行应用**
   - 连接 Android 设备或启动模拟器
   - 点击 "Run" 按钮（绿色三角形）
   - 或使用快捷键 Shift + F10

4. **测试导航**
   - 应用启动后会显示 HomeActivity（首页）
   - 点击"拼图"按钮可以跳转到 EditActivity（编辑页）
   - 在编辑页点击左上角返回按钮可返回首页

## UI 设计说明

### HomeActivity UI 特点
- **顶部横幅**: 200dp高度的圣诞主题广告区，包含标题、副标题和CTA按钮
- **功能按钮**: 4个70dp圆形按钮，均匀分布，灰色背景带边框
- **标签栏**: "照片"和"项目"切换，包含筛选和排序图标
- **网格视图**: 2列照片网格展示
- **深色主题**: 黑色背景，白色文字

### EditActivity UI 特点
- **顶部工具栏**: 56dp高度，包含多个操作按钮
- **图片区域**: 全屏显示编辑中的图片，带有"全图"和"实时调色"控制
- **底部面板**: 
  - 标签栏（我的/推荐）
  - 横向滚动的分类标签
  - 底部工具栏（80dp高度）包含多个编辑工具
- **深色主题**: 黑色背景，优化视觉体验

## 技术要点

### 使用的Android组件
- `RelativeLayout` - 灵活的相对布局
- `LinearLayout` - 线性排列的视图
- `GridView` - 网格展示照片
- `HorizontalScrollView` - 横向滚动标签
- `Intent` - Activity间导航

### 布局特点
- 响应式设计，适配不同屏幕尺寸
- 使用权重（weight）实现均匀分布
- 圆形和圆角按钮增强视觉效果
- 深色主题提升专业感

## 注意事项

### 当前版本
这是一个**纯UI演示版本**，主要功能：
- ✅ 布局展示完整
- ✅ 基本导航功能（首页到编辑页）
- ⏳ 图片编辑功能（待实现）
- ⏳ 实际照片加载（待实现）
- ⏳ 工具栏功能逻辑（待实现）

### 下一步开发建议
1. 添加图片选择和加载功能
2. 实现各个编辑工具的实际功能
3. 添加滤镜和特效处理
4. 实现照片保存和导出
5. 优化用户体验和动画效果

## 文件清单

```
Photoshop_demo/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/photoshop_demo/
│   │       │   ├── HomeActivity.java
│   │       │   └── EditActivity.java
│   │       ├── res/
│   │       │   ├── drawable/
│   │       │   │   ├── circle_button_bg.xml
│   │       │   │   ├── rounded_button_bg.xml
│   │       │   │   └── rounded_button_dark_bg.xml
│   │       │   ├── layout/
│   │       │   │   ├── activity_home.xml
│   │       │   │   └── activity_edit.xml
│   │       │   └── values/
│   │       │       ├── colors.xml
│   │       │       └── strings.xml
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## 许可证

演示项目 - 仅供学习使用

