# 贡献指南

感谢您对 ChatBridge 项目的关注！本文档将帮助您了解如何为项目做出贡献。

## 🚀 快速开始

### 开发环境要求

- Java 11 或更高版本
- Maven 3.6+
- Git
- IDE（推荐 IntelliJ IDEA）

### 设置开发环境

1. Fork 本仓库
2. 克隆您的 fork：
   ```bash
   git clone https://github.com/your-username/chatbridge.git
   cd chatbridge
   ```
3. 安装依赖：
   ```bash
   mvn clean install
   ```

## 📋 开发流程

### 分支策略

- `main`: 主分支，包含稳定的生产代码
- `develop`: 开发分支，包含最新的开发代码
- `feature/*`: 功能分支，用于开发新功能
- `bugfix/*`: 修复分支，用于修复bug
- `hotfix/*`: 热修复分支，用于紧急修复

### 提交代码

1. 创建功能分支：
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. 进行开发并提交：
   ```bash
   git add .
   git commit -m "feat: 添加新功能描述"
   ```

3. 推送到您的 fork：
   ```bash
   git push origin feature/your-feature-name
   ```

4. 创建 Pull Request

### 提交信息规范

使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动

示例：

```
feat: 添加消息重试机制
fix: 修复签名验证失败的问题
docs: 更新API文档
```

## 🧪 测试

### 运行测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=ChatBridgePluginTest

# 生成测试报告
mvn surefire-report:report
```

### 测试覆盖率

```bash
# 生成覆盖率报告
mvn jacoco:report
```

### 代码质量检查

```bash
# 运行 SpotBugs 静态分析
mvn spotbugs:check

# 检查依赖安全性
mvn dependency-check:check

# 检查依赖更新
mvn versions:display-dependency-updates
```

## 📝 代码规范

### Java 代码风格

- 使用 4 个空格缩进
- 行长度不超过 120 字符
- 使用驼峰命名法
- 类名使用大驼峰，方法和变量使用小驼峰
- 常量使用全大写加下划线

### 注释规范

```java
/**
 * 发送消息到API服务器
 * 
 * @param player 发送消息的玩家
 * @param message 消息内容
 * @return 是否发送成功
 * @throws IOException 网络异常
 */
public boolean sendMessage(Player player, String message) throws IOException {
    // 实现代码
}
```

### 异常处理

- 使用具体的异常类型
- 提供有意义的错误信息
- 记录适当的日志级别

```java
try {
    // 业务逻辑
} catch (IOException e) {
    logger.warning("网络请求失败: " + e.getMessage());
    throw new MessageSendException("消息发送失败", e);
}
```

## 🔄 CI/CD 流程

### 自动化检查

每个 Pull Request 都会触发以下检查：

1. **多版本测试**: Java 11, 17, 21
2. **代码质量**: SpotBugs 静态分析
3. **安全检查**: OWASP 依赖检查
4. **测试覆盖率**: JaCoCo 报告

### 构建流程

- **测试阶段**: 运行单元测试和集成测试
- **构建阶段**: 编译并打包 JAR 文件
- **发布阶段**: 自动发布到 GitHub Releases

### 状态检查

在提交 PR 前，请确保：

- ✅ 所有测试通过
- ✅ 代码质量检查通过
- ✅ 没有安全漏洞
- ✅ 文档已更新

## 📚 文档

### 更新文档

如果您的更改影响了用户界面或API，请同时更新：

- README.md
- 配置文件示例
- API 文档
- 更新日志

### 文档风格

- 使用清晰简洁的语言
- 提供代码示例
- 包含配置说明
- 添加故障排查信息

## 🐛 报告问题

### 问题模板

报告 bug 时请包含：

1. **环境信息**:
    - Minecraft 版本
    - 服务端类型（Spigot/Paper/等）
    - Java 版本
    - 插件版本

2. **重现步骤**:
    - 详细的操作步骤
    - 预期结果
    - 实际结果

3. **日志信息**:
    - 相关的错误日志
    - 配置文件内容
    - 调试信息

### 功能请求

提出新功能时请说明：

- 功能描述
- 使用场景
- 预期收益
- 实现建议

## 🎯 发布流程

### 版本号规范

使用 [Semantic Versioning](https://semver.org/)：

- `MAJOR.MINOR.PATCH`
- `2.1.0`: 新功能
- `2.0.1`: bug修复
- `3.0.0`: 破坏性更改

### 发布检查清单

发布前确认：

- [ ] 所有测试通过
- [ ] 文档已更新
- [ ] 更新日志已完善
- [ ] 版本号已更新
- [ ] 标签已创建

## 💬 社区

### 沟通渠道

- GitHub Issues: 问题报告和功能请求
- GitHub Discussions: 一般讨论和问答
- Pull Requests: 代码审查和讨论

### 行为准则

请遵守以下原则：

- 尊重他人
- 建设性反馈
- 包容不同观点
- 专注于技术讨论

## 🙏 致谢

感谢所有为 ChatBridge 项目做出贡献的开发者！

您的贡献让这个项目变得更好。