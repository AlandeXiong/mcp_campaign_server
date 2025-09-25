# HTTP Stream集成指南

## 概述

MCP Campaign服务器现在支持HTTP Stream方式，通过Server-Sent Events (SSE)提供流式MCP协议通信。

## HTTP Stream优势

- ✅ 基于HTTP协议，兼容性更好
- ✅ 支持Server-Sent Events (SSE)
- ✅ 支持多客户端连接
- ✅ 更好的错误处理和重连机制
- ✅ 支持流式数据传输

## 快速开始

### 1. 启动HTTP Stream服务器

#### 使用启动脚本
```bash
cd /Users/xiongjian/project/Mcp_Campaign
./start_http_stream.sh [port]
```

#### 使用Maven命令
```bash
mvn exec:java -Dexec.mainClass=com.insurance.mcp.McpHttpStreamApplication -Dexec.args="8081"
```

### 2. 配置Cline使用HTTP Stream

在 `~/.cline/config.json` 中添加：

```json
{
  "mcpServers": {
    "insurance-campaign-stream": {
      "command": "mvn",
      "args": [
        "exec:java",
        "-Dexec.mainClass=com.insurance.mcp.McpHttpStreamApplication",
        "-Dexec.args=8081"
      ],
      "cwd": "/Users/xiongjian/project/Mcp_Campaign"
    }
  }
}
```

### 3. 或者使用启动脚本配置

```json
{
  "mcpServers": {
    "insurance-campaign-stream": {
      "command": "./start_http_stream.sh",
      "args": ["8081"],
      "cwd": "/Users/xiongjian/project/Mcp_Campaign"
    }
  }
}
```

## API端点

### Spring Boot集成模式

当使用Spring Boot应用时，HTTP Stream端点如下：

#### SSE连接端点
- **连接**: `GET http://localhost:8080/mcp-stream/connect`
- **指定客户端ID**: `GET http://localhost:8080/mcp-stream/connect/{clientId}`

#### 请求端点
- **发送请求**: `POST http://localhost:8080/mcp-stream/request`
- **发送到指定客户端**: `POST http://localhost:8080/mcp-stream/request/{clientId}`

#### 管理端点
- **连接状态**: `GET http://localhost:8080/mcp-stream/connections`
- **关闭连接**: `DELETE http://localhost:8080/mcp-stream/connections/{clientId}`
- **健康检查**: `GET http://localhost:8080/mcp-stream/health`
- **Ping测试**: `GET http://localhost:8080/mcp-stream/ping`

### 独立模式

当使用独立的HTTP Stream应用时：

- **SSE连接**: `http://localhost:8081/connect`
- **HTTP请求**: `http://localhost:8081/request`

## 使用示例

### 1. 建立SSE连接

```javascript
// JavaScript示例
const eventSource = new EventSource('http://localhost:8080/mcp-stream/connect');

eventSource.onopen = function(event) {
    console.log('SSE连接已建立');
};

eventSource.addEventListener('mcp-message', function(event) {
    const data = JSON.parse(event.data);
    console.log('收到MCP消息:', data);
});

eventSource.addEventListener('connection', function(event) {
    const data = JSON.parse(event.data);
    console.log('连接状态:', data);
});
```

### 2. 发送MCP请求

```bash
# 发送工具列表请求
curl -X POST http://localhost:8080/mcp-stream/request \
  -H "Content-Type: application/json" \
  -H "X-Client-ID: my-client-123" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "tools/list",
    "params": {}
  }'
```

### 3. 调用工具

```bash
# 调用人群定位工具
curl -X POST http://localhost:8080/mcp-stream/request \
  -H "Content-Type: application/json" \
  -H "X-Client-ID: my-client-123" \
  -d '{
    "jsonrpc": "2.0",
    "id": "2",
    "method": "tools/call",
    "params": {
      "name": "audience_targeting",
      "arguments": {
        "requirements": "Target young professionals for life insurance",
        "insurance_type": "life_insurance",
        "campaign_objective": "acquisition"
      }
    }
  }'
```

## Cline集成配置

### 方法1：使用Maven命令

```json
{
  "mcpServers": {
    "insurance-campaign": {
      "command": "mvn",
      "args": [
        "exec:java",
        "-Dexec.mainClass=com.insurance.mcp.McpHttpStreamApplication",
        "-Dexec.args=8081"
      ],
      "cwd": "/Users/xiongjian/project/Mcp_Campaign"
    }
  }
}
```

### 方法2：使用启动脚本

```json
{
  "mcpServers": {
    "insurance-campaign": {
      "command": "./start_http_stream.sh",
      "args": ["8081"],
      "cwd": "/Users/xiongjian/project/Mcp_Campaign"
    }
  }
}
```

### 方法3：使用Spring Boot集成

```json
{
  "mcpServers": {
    "insurance-campaign": {
      "command": "mvn",
      "args": [
        "spring-boot:run",
        "-Dspring-boot.run.jvmArguments=-Dserver.port=8080"
      ],
      "cwd": "/Users/xiongjian/project/Mcp_Campaign"
    }
  }
}
```

## 测试HTTP Stream

### 1. 启动服务器

```bash
./start_http_stream.sh 8081
```

### 2. 测试连接

```bash
# 测试健康检查
curl http://localhost:8081/health

# 测试SSE连接
curl -N http://localhost:8081/connect
```

### 3. 测试MCP请求

```bash
# 测试工具列表
echo '{"jsonrpc":"2.0","id":"1","method":"tools/list","params":{}}' | \
curl -X POST http://localhost:8081/request \
  -H "Content-Type: application/json" \
  --data-binary @-
```

## 故障排除

### 常见问题

1. **端口占用**
   ```bash
   lsof -i :8081
   ```

2. **连接失败**
   - 检查服务器是否启动
   - 检查防火墙设置
   - 验证端口是否正确

3. **SSE连接断开**
   - 检查网络连接
   - 查看服务器日志
   - 验证客户端ID

### 调试技巧

1. **启用详细日志**
   ```bash
   export JAVA_OPTS="-Dlogging.level.com.insurance.mcp=DEBUG"
   ./start_http_stream.sh
   ```

2. **测试SSE连接**
   ```bash
   curl -N -H "Accept: text/event-stream" http://localhost:8081/connect
   ```

3. **检查连接状态**
   ```bash
   curl http://localhost:8081/connections
   ```

## 架构对比

| 特性 | WebSocket | Stdio | HTTP Stream |
|------|-----------|-------|-------------|
| 协议 | WebSocket | 标准输入输出 | HTTP/SSE |
| 多客户端 | 支持 | 不支持 | 支持 |
| 重连机制 | 手动 | 不适用 | 自动 |
| 兼容性 | 中等 | 高 | 高 |
| 调试难度 | 中等 | 简单 | 简单 |
| 网络依赖 | 需要 | 不需要 | 需要 |

## 最佳实践

1. **连接管理**
   - 使用唯一的客户端ID
   - 实现自动重连机制
   - 处理连接断开事件

2. **错误处理**
   - 监听所有事件类型
   - 处理JSON解析错误
   - 实现超时机制

3. **性能优化**
   - 合理设置缓冲区大小
   - 避免频繁连接断开
   - 使用连接池

HTTP Stream方式提供了更好的兼容性和稳定性，是推荐的集成方式之一！
