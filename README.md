# ChatBridge Minecraft 插件

[![CI/CD Pipeline](https://github.com/your-username/chatbridge/actions/workflows/ci.yml/badge.svg)](https://github.com/your-username/chatbridge/actions/workflows/ci.yml)
[![Code Quality](https://github.com/your-username/chatbridge/actions/workflows/code-quality.yml/badge.svg)](https://github.com/your-username/chatbridge/actions/workflows/code-quality.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-11%2B-blue.svg)](https://www.oracle.com/java/)
[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.13.2%2B-green.svg)](https://www.minecraft.net/)

## 概述

ChatBridge 是一个 Minecraft 服务器插件，用于将游戏内的聊天消息实时转发到 QQ 群。支持签名验证、外置配置和完善的错误处理。

## 功能特性

- ✅ **实时消息转发**: 将玩家聊天消息转发到 QQ 群
- ✅ **签名验证**: 支持 SHA256 签名验证，确保通信安全
- ✅ **外置配置**: 所有配置项可通过配置文件修改
- ✅ **错误处理**: 完善的异常处理和日志记录
- ✅ **调试模式**: 可开启详细日志用于调试
- ✅ **命令支持**: 支持重载配置和测试功能
- ✅ **权限管理**: 细粒度的权限控制

## 安装步骤

### 1. 下载插件

将 `ChatBridge.jar` 文件放入服务器的 `plugins` 目录。

### 2. 首次启动

启动服务器，插件会自动生成默认配置文件 `plugins/ChatBridge/config.yml`。

### 3. 配置插件

编辑 `plugins/ChatBridge/config.yml` 文件：

```yaml
# API 配置
api:
  # 修改为你的服务器API地址
  url: "http://your-server.com:8080/api/v1/pushMessage"
  # 设置服务器ID（用于区分不同服务器）
  server-id: "survival-server"

# 签名验证配置
signature:
  # 生产环境建议启用
  enabled: true
  # 设置与服务端一致的密钥
  secret-key: "your-complex-secret-key-here"

# 网络配置
network:
  connect-timeout: 10
  request-timeout: 30

# 调试配置
debug:
  # 开发环境可启用，生产环境建议关闭
  enabled: false
```

### 4. 重载配置

在游戏中执行命令：

```
/chatbridge reload
```

## 配置说明

### API 配置

| 配置项             | 说明      | 默认值                                        |
|-----------------|---------|--------------------------------------------|
| `api.url`       | API接口地址 | `http://localhost:8080/api/v1/pushMessage` |
| `api.server-id` | 服务器标识ID | `"1"`                                      |

### 签名验证配置

| 配置项                    | 说明       | 默认值    |
|------------------------|----------|--------|
| `signature.enabled`    | 是否启用签名验证 | `true` |
| `signature.secret-key` | 签名密钥     | `""`   |

**重要**: 签名密钥必须与服务端配置的 `app.secret-key` 完全一致！

### 网络配置

| 配置项                       | 说明        | 默认值  |
|---------------------------|-----------|------|
| `network.connect-timeout` | 连接超时时间（秒） | `10` |
| `network.request-timeout` | 请求超时时间（秒） | `30` |

### 调试配置

| 配置项             | 说明       | 默认值     |
|-----------------|----------|---------|
| `debug.enabled` | 是否启用调试日志 | `false` |

## 命令使用

### 基本命令

```
/chatbridge          # 显示帮助信息
/chatbridge reload   # 重载配置文件
/chatbridge test     # 发送测试消息
```

### 命令别名

```
/cb reload           # 等同于 /chatbridge reload
/bridge test         # 等同于 /chatbridge test
```

## 权限配置

### 权限节点

| 权限                  | 说明     | 默认     |
|---------------------|--------|--------|
| `chatbridge.use`    | 使用基本命令 | `true` |
| `chatbridge.reload` | 重载配置   | `op`   |
| `chatbridge.test`   | 发送测试消息 | `op`   |
| `chatbridge.*`      | 所有权限   | `op`   |

### 权限配置示例

在权限插件中配置：

```yaml
# LuckPerms 示例
groups:
  admin:
    permissions:
      - chatbridge.*
  moderator:
    permissions:
      - chatbridge.reload
      - chatbridge.test
  default:
    permissions:
      - chatbridge.use
```

## 签名验证

### 签名算法

插件使用 SHA256 算法生成签名：

```
signature = SHA256(timestamp + nonce + secretKey)
```

### 请求头

启用签名验证时，插件会自动添加以下请求头：

- `X-Timestamp`: 时间戳（毫秒）
- `X-Nonce`: 随机字符串（UUID）
- `X-Sign`: 签名值

### 服务端配置

确保服务端 `application.yml` 中配置了相同的密钥：

```yaml
app:
  secret-key: "your-complex-secret-key-here"
```

## 故障排查

### 常见问题

#### 1. 消息发送失败

**现象**: 控制台显示 "消息发送失败"

**解决方案**:

- 检查 API 地址是否正确
- 确认服务器网络连接正常
- 查看详细错误信息

#### 2. 签名验证失败 (403错误)

**现象**: 状态码 403，签名验证失败

**解决方案**:

- 确认插件和服务端的密钥完全一致
- 检查服务器时间是否同步
- 确认签名算法实现正确

#### 3. 请求频率过高 (429错误)

**现象**: 状态码 429，请求过于频繁

**解决方案**:

- 检查是否有大量玩家同时发言
- 调整服务端限流配置
- 考虑增加请求间隔

#### 4. 配置验证失败

**现象**: 插件启动时提示配置验证失败

**解决方案**:

- 检查 API 地址格式是否正确
- 确认服务器ID不为空
- 如果启用签名验证，确保密钥不为空

### 调试模式

启用调试模式可以获得更详细的日志信息：

```yaml
debug:
  enabled: true
```

调试日志包括：

- 捕获的聊天消息
- 发送的请求详情
- 签名生成信息
- 服务器响应内容

### 日志分析

#### 成功日志

```
[INFO] 消息发送成功: PlayerName
```

#### 失败日志

```
[WARNING] 消息发送失败！
[WARNING] 状态码: 403
[WARNING] 响应内容: {"code":403,"msg":"签名验证失败"}
[WARNING] 玩家: PlayerName, 消息: Hello World
```

## 开发信息

### 编译要求

- Java 17+
- Bukkit/Spigot/Paper API 1.19+
- Maven 或 Gradle

### 依赖项

```xml

<dependencies>
    <dependency>
        <groupId>org.spigotmc</groupId>
        <artifactId>spigot-api</artifactId>
        <version>1.19.4-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### 构建命令

```bash
# Maven
mvn clean package

# Gradle
./gradlew build
```

## 更新日志

### v2.0.0

- ✅ 添加签名验证支持
- ✅ 外置配置文件
- ✅ 完善的错误处理
- ✅ 调试模式支持
- ✅ 命令系统重构
- ✅ 权限管理优化

### v1.0.0

- ✅ 基础消息转发功能
- ✅ HTTP 客户端实现
- ✅ 异步消息处理

## 技术支持

如果遇到问题，请：

1. 检查配置文件是否正确
2. 查看服务器日志
3. 启用调试模式获取详细信息
4. 在 GitHub 提交 Issue

## 许可证

本插件采用 MIT 许可证开源。