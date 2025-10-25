# Uniform Format

[![Uniform Format](https://img.shields.io/badge/Uniform-Format-blue)](https://github.com/dong4j/zeka.stack)
[![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ-IDEA-red)](https://www.jetbrains.com/idea/)

> 统一的代码格式化和模板管理插件

Uniform Format 是一个 IntelliJ IDEA 插件，提供标准化的代码风格配置、文件模板和 Live Template，帮助开发团队保持代码风格的一致性。

## 功能特性

### 📝 文件模板

- 自动添加统一的文件头部注释
- 包含公司信息、作者、版本、邮箱、日期等
- 支持 Java Class、Interface、Enum 等文件类型

### ⚡ Live Template

- **todo-xxx**：标记待处理的地方
- **fixme-xxx**：标记需要修复的地方
- **cd**：生成 class javadoc
- **li/ld/lw/le**：快速生成日志语句
- **test**：快速生成单元测试方法

### 🎨 代码风格

- 自动配置统一的代码格式化规则
- 支持 Java、SQL、JavaScript 等多种语言
- 可自定义缩进、换行、导入等规则

### 📊 使用统计

- 统计模板使用情况
- 帮助了解团队开发习惯

## 安装方式

### 方式一：从 Marketplace 安装

1. 打开 IntelliJ IDEA
2. 进入 `File` → `Settings` → `Plugins`
3. 搜索 "Uniform Format"
4. 点击 `Install` 安装

### 方式二：本地安装

1. 下载插件 JAR 文件
2. 进入 `File` → `Settings` → `Plugins`
3. 点击齿轮图标 → `Install Plugin from Disk`
4. 选择下载的 JAR 文件

## 使用方法

### 文件模板

插件安装后会自动配置文件模板，创建新文件时会自动添加统一的头部注释。

### Live Template

在编辑器中输入以下快捷键：

| 快捷键          | 功能         | 示例                                            |
|--------------|------------|-----------------------------------------------|
| `todo` + Tab | 标记待处理      | `todo-dong4j : (2024.12.19 15:30) [需要实现的功能]`  |
| `fix` + Tab  | 标记需要修复     | `fixme-dong4j : (2024.12.19 15:30) [修复这个bug]` |
| `cd` + Tab   | 生成 javadoc | 自动生成类注释                                       |
| `li` + Tab   | 生成日志       | `log.info()`                                  |
| `ld` + Tab   | 生成调试日志     | `log.debug()`                                 |
| `lw` + Tab   | 生成警告日志     | `log.warn()`                                  |
| `le` + Tab   | 生成错误日志     | `log.error("{}", exception)`                  |
| `test` + Tab | 生成测试方法     | `@Test void test_methodName(){}`              |

### 代码风格

插件会自动应用统一的代码风格配置，包括：

- 缩进：使用 Tab 字符
- 行宽：140 字符
- 导入：按包名分组
- 注释：保持格式

## 配置说明

### 设置面板

进入 `File` → `Settings` → `Tools` → `Uniform Format` 可以配置：

- ✅ **启用文件模板**：自动添加统一的文件头部注释
- ✅ **启用 Live Template**：快速生成常用代码片段
- ✅ **启用代码风格配置**：自动配置统一的代码格式化规则
- ✅ **启用使用统计**：统计模板使用情况

### 自定义配置

可以通过修改插件源码来自定义：

- 文件模板内容
- Live Template 规则
- 代码风格设置

## 开发指南

### 项目结构

```
uniform-format/
├── src/main/java/
│   └── dev/dong4j/zeka/stack/idea/plugin/uniform/format/
│       ├── UniformFormatComponent.java          # 主组件
│       ├── codestyle/
│       │   └── UniformCodeStyleHandler.java     # 代码风格处理器
│       ├── template/
│       │   ├── file/
│       │   │   └── UniformFileTemplatesHandler.java  # 文件模板处理器
│       │   └── live/
│       │       ├── UniformLiveTemplateContext.java   # Live Template 上下文
│       │       └── UniformLiveTemplateProvider.java   # Live Template 提供者
│       └── settings/
│           ├── UniformFormatSettingsState.java         # 设置状态
│           ├── UniformFormatSettingsConfigurable.java # 设置配置
│           └── UniformFormatSettingsPanel.java        # 设置面板
├── src/main/resources/
│   ├── META-INF/
│   │   └── plugin.xml                        # 插件配置
│   ├── liveTemplates/
│   │   └── uniform-live-template.xml         # Live Template 定义
│   ├── settings/
│   │   └── UniformFormatSettingsPanel.form   # 设置面板 UI
│   ├── uniform-code-style.xml                # 代码风格配置
│   └── messages*.properties                  # 国际化资源
└── build.gradle.kts                          # 构建配置
```

### 构建项目

```bash
# 克隆项目
git clone https://github.com/dong4j/zeka.stack.git
cd zeka.stack/good-kits/uniform-format

# 构建插件
./gradlew buildPlugin

# 运行测试
./gradlew test

# 运行插件（开发模式）
./gradlew runIde
```

### 开发环境

- **JDK**: 17+
- **IntelliJ IDEA**: 2022.3+
- **Gradle**: 8.0+
- **IntelliJ Platform**: 2.1.0

## 版本历史

### v1.0.0 (2024-12-19)

- 🎉 初始版本发布
- ✨ 支持文件模板自动配置
- ✨ 支持 Live Template 快速生成
- ✨ 支持代码风格自动配置
- ✨ 支持使用统计功能
- ✨ 提供设置面板配置

## 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

### 代码规范

- 使用 Java 17+
- 遵循 IntelliJ Platform 开发规范
- 添加适当的注释和文档
- 编写单元测试

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

- **作者**: dong4j
- **邮箱**: dong4j@gmail.com
- **项目地址**: https://github.com/dong4j/zeka.stack

## 致谢

感谢以下开源项目：

- [IntelliJ Platform](https://github.com/JetBrains/intellij-community)
- [IntelliJ Platform Gradle Plugin](https://github.com/JetBrains/gradle-intellij-plugin)

---

⭐ 如果这个插件对你有帮助，请给个 Star 支持一下！