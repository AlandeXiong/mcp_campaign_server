# Stdio方式支持总结

## ✅ 已完成的工作

### 1. 添加Stdio支持
- ✅ 创建了 `McpStdioHandler.java` - Spring Boot集成的Stdio处理器
- ✅ 创建了 `McpStdioApplication.java` - 独立的Stdio应用程序
- ✅ 更新了 `pom.xml` 添加Maven exec插件支持
- ✅ 创建了 `start_stdio.sh` 启动脚本
- ✅ 更新了配置文件支持Stdio模式

### 2. 双重架构支持
现在MCP Campaign服务器支持两种连接方式：

#### WebSocket方式
- 通过HTTP服务器和WebSocket连接
- 适合网络环境下的集成
- 支持多客户端连接

#### Stdio方式 ✅ 新增
- 通过标准输入输出流进行通信
- 适合本地集成和开发
- 更好的兼容性和稳定性

## 🚀 使用方法

### 启动Stdio服务器

```bash
cd /Users/xiongjian/project/Mcp_Campaign
./start_stdio.sh
```

### 配置Cline使用Stdio

在 `~/.cline/config.json` 中添加：

```json
{
  "mcpServers": {
    "insurance-campaign": {
      "command": "mvn",
      "args": [
        "exec:java",
        "-Dexec.mainClass=com.insurance.mcp.McpStdioApplication"
      ],
      "cwd": "/Users/xiongjian/project/Mcp_Campaign"
    }
  }
}
```

### 或者使用启动脚本

```json
{
  "mcpServers": {
    "insurance-campaign": {
      "command": "./start_stdio.sh",
      "cwd": "/Users/xiongjian/project/Mcp_Campaign"
    }
  }
}
```

## 🧪 测试验证

### 编译测试
```bash
mvn clean compile --settings temp-settings.xml
# ✅ BUILD SUCCESS - 编译成功
```

### 功能测试
Stdio服务器支持所有原有的MCP工具：
- `audience_targeting` - 人群定位
- `campaign_content_recommendation` - 内容推荐
- `inspector_query` - Inspector查询

## 📊 架构对比

| 特性 | WebSocket方式 | Stdio方式 |
|------|---------------|-----------|
| 配置复杂度 | 中等 | 简单 |
| 网络依赖 | 需要端口 | 无网络依赖 |
| 调试难度 | 中等 | 简单 |
| 性能 | 中等 | 高 |
| 兼容性 | 中等 | 高 |
| 多客户端 | 支持 | 不支持 |
| 本地开发 | 适合 | 更适合 |

## 🎯 推荐使用场景

### Stdio方式推荐用于：
- ✅ 本地开发和测试
- ✅ Cline集成
- ✅ Claude Desktop集成
- ✅ 避免网络连接问题
- ✅ 简单的配置需求

### WebSocket方式推荐用于：
- 网络环境下的集成
- 多客户端连接需求
- 远程服务器部署

## 🔧 故障排除

### 如果Stdio方式不工作：

1. **检查Java和Maven**:
   ```bash
   java -version  # 需要Java 17+
   mvn -version   # 需要Maven 3.6+
   ```

2. **使用临时设置文件**:
   ```bash
   mvn exec:java --settings temp-settings.xml -Dexec.mainClass=com.insurance.mcp.McpStdioApplication
   ```

3. **检查配置文件路径**:
   - macOS: `~/.cline/config.json`
   - Windows: `%USERPROFILE%\.cline\config.json`

4. **重启VS Code/Claude Desktop**

## 📁 文件结构

```
src/main/java/com/insurance/mcp/
├── McpCampaignServerApplication.java    # Spring Boot主应用
├── McpStdioApplication.java            # ✅ 新增：Stdio独立应用
├── handler/
│   ├── McpWebSocketHandler.java        # WebSocket处理器
│   └── McpStdioHandler.java            # ✅ 新增：Stdio处理器
├── service/
│   └── McpToolService.java             # MCP工具服务
└── model/                              # MCP协议模型
    ├── McpRequest.java
    ├── McpResponse.java
    └── McpTool.java

启动脚本：
├── start.sh                           # WebSocket方式启动
├── start_stdio.sh                     # ✅ 新增：Stdio方式启动
└── temp-settings.xml                  # 临时Maven设置
```

## 🎉 总结

现在您的MCP Campaign服务器完全支持Stdio方式接入！

**主要优势：**
- ✅ 解决了WebSocket连接问题
- ✅ 更简单的配置
- ✅ 更好的兼容性
- ✅ 避免网络依赖
- ✅ 适合本地集成

**下一步：**
1. 使用Stdio方式配置Cline
2. 重启VS Code
3. 开始使用保险营销工具

Stdio方式是推荐的集成方式，特别是对于本地开发和Cline集成！
