# Stdio方式集成指南

## 概述

现在MCP Campaign服务器支持两种连接方式：
1. **WebSocket方式** - 通过HTTP服务器和WebSocket连接
2. **Stdio方式** - 通过标准输入输出流进行通信

## Stdio方式优势

- ✅ 更简单的配置
- ✅ 更好的兼容性
- ✅ 避免网络连接问题
- ✅ 适合本地集成

## 快速开始

### 1. 启动Stdio服务器

```bash
cd /Users/xiongjian/project/Mcp_Campaign
./start_stdio.sh
```

### 2. 配置Cline使用Stdio

在 `~/.cline/config.json` 中添加：

```json
{
  "mcpServers": {
    "insurance-campaign-stdio": {
      "command": "./start_stdio.sh",
      "cwd": "/Users/xiongjian/project/Mcp_Campaign"
    }
  }
}
```

### 3. 或者使用Maven命令

```json
{
  "mcpServers": {
    "insurance-campaign-stdio": {
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

## 详细配置

### Claude Desktop配置

在Claude Desktop配置文件中添加：

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

### VS Code Cline配置

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

## 测试Stdio服务器

### 手动测试

1. 启动服务器：
```bash
./start_stdio.sh
```

2. 发送测试消息（在另一个终端）：
```bash
echo '{"jsonrpc":"2.0","id":"1","method":"initialize","params":{}}' | ./start_stdio.sh
```

3. 测试工具列表：
```bash
echo '{"jsonrpc":"2.0","id":"2","method":"tools/list","params":{}}' | ./start_stdio.sh
```

### 自动化测试脚本

```bash
#!/bin/bash
echo "Testing MCP Stdio Server..."

# Test initialize
echo "Testing initialize..."
echo '{"jsonrpc":"2.0","id":"1","method":"initialize","params":{}}' | mvn exec:java -Dexec.mainClass=com.insurance.mcp.McpStdioApplication -q

echo -e "\nTesting tools/list..."
echo '{"jsonrpc":"2.0","id":"2","method":"tools/list","params":{}}' | mvn exec:java -Dexec.mainClass=com.insurance.mcp.McpStdioApplication -q

echo -e "\nTesting audience_targeting tool..."
echo '{"jsonrpc":"2.0","id":"3","method":"tools/call","params":{"name":"audience_targeting","arguments":{"requirements":"Test audience targeting"}}}' | mvn exec:java -Dexec.mainClass=com.insurance.mcp.McpStdioApplication -q
```

## 可用的MCP工具

### 1. audience_targeting

**功能**: 人群定位建议

**示例调用**:
```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "tools/call",
  "params": {
    "name": "audience_targeting",
    "arguments": {
      "requirements": "Target young professionals aged 25-35 for life insurance",
      "insurance_type": "life_insurance",
      "campaign_objective": "acquisition"
    }
  }
}
```

### 2. campaign_content_recommendation

**功能**: 营销内容推荐

**示例调用**:
```json
{
  "jsonrpc": "2.0",
  "id": "2",
  "method": "tools/call",
  "params": {
    "name": "campaign_content_recommendation",
    "arguments": {
      "audience_criteria": {
        "minAge": 25,
        "maxAge": 35,
        "incomeRange": ["middle_income"]
      },
      "insurance_type": "life_insurance",
      "channel": "email",
      "campaign_goal": "conversion"
    }
  }
}
```

### 3. inspector_query

**功能**: Claude Inspector查询

**示例调用**:
```json
{
  "jsonrpc": "2.0",
  "id": "3",
  "method": "tools/call",
  "params": {
    "name": "inspector_query",
    "arguments": {
      "query": "Analyze the effectiveness of recent insurance marketing campaigns"
    }
  }
}
```

## 故障排除

### 常见问题

1. **服务器无法启动**
   - 检查Java版本 (需要Java 17+)
   - 检查Maven是否正确安装
   - 确保项目编译成功

2. **Cline无法连接**
   - 检查配置文件路径和格式
   - 确保工作目录正确
   - 重启Cline/VS Code

3. **工具调用失败**
   - 检查JSON格式是否正确
   - 查看服务器日志输出
   - 验证参数格式

### 调试技巧

1. **启用详细日志**:
```bash
export JAVA_OPTS="-Dlogging.level.com.insurance.mcp=DEBUG"
./start_stdio.sh
```

2. **检查进程**:
```bash
ps aux | grep McpStdioApplication
```

3. **测试JSON格式**:
```bash
echo '{"jsonrpc":"2.0","id":"test","method":"initialize","params":{}}' | jq .
```

## 性能优化

### 启动优化

1. **预编译**:
```bash
mvn clean compile
```

2. **使用JAR文件**:
```bash
mvn clean package
java -jar target/mcp-campaign-server-1.0.0.jar --stdio
```

### 内存优化

```bash
export JAVA_OPTS="-Xms128m -Xmx512m"
./start_stdio.sh
```

## 最佳实践

1. **使用绝对路径**: 在配置中使用完整的文件路径
2. **错误处理**: 实现适当的错误处理和重试机制
3. **日志记录**: 启用适当的日志级别进行调试
4. **资源管理**: 确保服务器正确启动和停止

## 与WebSocket方式的对比

| 特性 | Stdio方式 | WebSocket方式 |
|------|-----------|---------------|
| 配置复杂度 | 简单 | 中等 |
| 网络依赖 | 无 | 有 |
| 调试难度 | 简单 | 中等 |
| 性能 | 高 | 中等 |
| 兼容性 | 高 | 中等 |

Stdio方式是推荐的集成方式，特别是对于本地开发和测试。
