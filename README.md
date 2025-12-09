# NotePad - Android 记事本应用

一个功能丰富的 Android 记事本应用，支持多种笔记管理功能，包括 OCR 文字识别、语音搜索、提醒功能等。

## 功能特性

### 核心功能
- **笔记管理**：创建、编辑、删除笔记
- **分类管理**：支持自定义分类，快速筛选笔记
- **搜索功能**：实时搜索笔记标题和内容
- **多种排序**：按时间、标题、分类、优先级排序

### 高级功能
- **OCR 文字识别**：使用 Google ML Kit 识别图片中的文字（支持中文）
- **语音搜索**：通过语音快速搜索笔记
- **便签模式**：创建快速便签
- **待办事项**：支持待办事项标记和完成状态
- **优先级管理**：为笔记设置优先级（低、中、高）
- **提醒功能**：为笔记设置提醒时间
- **笔记加密**：支持为单个笔记设置密码保护

### 数据管理
- **备份与恢复**：支持笔记备份和恢复功能
- **导出功能**：将笔记导出为文本文件
- **用户系统**：支持用户注册、登录和登出

### 多媒体支持
- **图片附件**：为笔记添加图片
- **音频附件**：为笔记添加音频
- **视频附件**：为笔记添加视频
- **颜色标记**：为笔记设置颜色标签

## 技术栈

- **开发语言**：Java
- **最低 SDK 版本**：24 (Android 7.0)
- **目标 SDK 版本**：34 (Android 14)
- **构建工具**：Gradle 8.1.0

### 主要依赖库
- **AndroidX**：AppCompat, Material Design, RecyclerView
- **Google ML Kit**：文字识别（中英文）
- **ExifInterface**：图片方向处理

## 项目结构

```
app/src/main/java/com/example/notepad/
├── MainActivity.java           # 主界面
├── EditNoteActivity.java       # 笔记编辑界面
├── LoginActivity.java          # 登录界面
├── RegisterActivity.java       # 注册界面
├── SettingsActivity.java       # 设置界面
├── OCRActivity.java            # OCR 识别界面
├── StickyNoteActivity.java     # 便签界面
├── VoiceSearchActivity.java    # 语音搜索界面
├── Note.java                   # 笔记数据模型
├── NotesAdapter.java           # 笔记列表适配器
├── NoteStorage.java            # 笔记存储管理
├── User.java                   # 用户数据模型
├── UserStorage.java            # 用户存储管理
├── BackupManager.java          # 备份管理
├── ReminderManager.java        # 提醒管理
└── ReminderReceiver.java       # 提醒广播接收器
```

## 安装与运行

### 环境要求
- Android Studio Arctic Fox 或更高版本
- JDK 8 或更高版本
- Android SDK 34

### 构建步骤

1. 克隆项目到本地
```bash
git clone <repository-url>
```

2. 使用 Android Studio 打开项目

3. 等待 Gradle 同步完成

4. 连接 Android 设备或启动模拟器

5. 点击运行按钮或使用命令：
```bash
./gradlew installDebug
```

## 使用说明

### 首次使用
1. 启动应用后进入登录界面
2. 点击"注册"创建新账户
3. 登录后即可开始使用

### 创建笔记
1. 点击右下角的"+"按钮
2. 输入标题和内容
3. 可选择分类、设置优先级、添加提醒等
4. 点击保存

### OCR 识别
1. 点击菜单中的"OCR 识别"
2. 选择图片或拍照
3. 应用会自动识别图片中的文字
4. 识别结果可保存为笔记

### 备份与恢复
1. 点击菜单中的"备份"保存当前所有笔记
2. 点击"恢复"可从备份文件中恢复笔记
3. 删除笔记时会自动创建备份

## 权限说明

应用需要以下权限：
- **相机权限**：用于拍照和 OCR 识别
- **存储权限**：用于读取图片和保存备份
- **录音权限**：用于语音搜索功能

## 数据存储

- 笔记数据存储在应用私有目录
- 备份文件存储在 `外部存储/Android/data/com.example.notepad/files/backups/`
- 导出文件存储在 `外部存储/Android/data/com.example.notepad/files/exports/`

## 开发计划

- [ ] 云同步功能
- [ ] Markdown 支持
- [ ] 笔记分享功能
- [ ] 主题切换（深色模式）
- [ ] 笔记标签系统
- [ ] 手写笔记支持
