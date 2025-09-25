# Cline远程HTTP Stream配置完整指南

## 🎯 概述

本指南提供Cline通过远程服务方式接入MCP HTTP Stream的完整配置方案。

## 📋 配置方案对比

| 方案 | 复杂度 | 稳定性 | 功能 | 推荐度 |
|------|--------|--------|------|--------|
| 直接连接 | 简单 | 中等 | 基础 | ⭐⭐⭐ |
| 本地代理脚本 | 简单 | 高 | 基础+重连 | ⭐⭐⭐⭐ |
| Node.js代理 | 中等 | 高 | 完整 | ⭐⭐⭐⭐⭐ |
| Docker部署 | 复杂 | 最高 | 完整+扩展 | ⭐⭐⭐⭐ |

## 🚀 方案1：直接连接（最简单）

### 步骤1：配置Cline

在 `~/.cline/config.json` 中配置：

```json
{
  "mcpServers": {
    "insurance-campaign-remote": {
      "command": "curl",
      "args": [
        "-N",
        "-H", "Accept: text/event-stream",
        "-H", "Cache-Control: no-cache",
        "http://your-remote-server.com:8081/connect"
      ]
    }
  }
}
```

### 步骤2：测试连接

```bash
# 测试远程服务器
./test_remote_connection.sh http://your-remote-server.com:8081

# 如果测试通过，重启VS Code
```

## 🔧 方案2：本地代理脚本（推荐）

### 步骤1：使用代理脚本

```json
{
  "mcpServers": {
    "insurance-campaign-remote": {
      "command": "./local_http_stream_proxy.sh",
      "args": ["http://your-remote-server.com:8081"],
      "cwd": "/Users/xiongjian/project/Mcp_Campaign"
    }
  }
}
```

### 步骤2：测试代理

```bash
# 测试代理脚本
./local_http_stream_proxy.sh http://your-remote-server.com:8081
```

## 🌟 方案3：Node.js代理（最佳）

### 步骤1：启动代理服务

```bash
# 启动Node.js代理
node http_stream_proxy.js http://your-remote-server.com:8081 8082
```

### 步骤2：配置Cline使用本地代理

```json
{
  "mcpServers": {
    "insurance-campaign-remote": {
      "command": "curl",
      "args": [
        "-N",
        "-H", "Accept: text/event-stream",
        "http://localhost:8082/connect"
      ]
    }
  }
}
```

### 步骤3：测试本地代理

```bash
# 测试本地代理健康状态
curl http://localhost:8082/health

# 测试SSE连接
curl -N http://localhost:8082/connect
```

## 🐳 方案4：Docker部署（生产环境）

### 步骤1：创建Docker配置

```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app
COPY . .
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
RUN mvn clean compile

EXPOSE 8081
CMD ["mvn", "exec:java", "-Dexec.mainClass=com.insurance.mcp.McpHttpStreamApplication", "-Dexec.args=8081"]
```

```yaml
# docker-compose.yml
version: '3.8'
services:
  mcp-campaign-server:
    build: .
    ports:
      - "8081:8081"
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

### 步骤2：部署到远程服务器

```bash
# 在远程服务器上
docker-compose up -d
```

### 步骤3：配置Cline

```json
{
  "mcpServers": {
    "insurance-campaign-remote": {
      "command": "curl",
      "args": [
        "-N",
        "-H", "Accept: text/event-stream",
        "http://your-remote-server.com:8081/connect"
      ]
    }
  }
}
```

## 🔒 安全配置

### 1. HTTPS配置

#### 服务器端（使用Nginx反向代理）

```nginx
server {
    listen 443 ssl;
    server_name your-remote-server.com;
    
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    
    location / {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # SSE specific headers
        proxy_set_header Connection '';
        proxy_http_version 1.1;
        proxy_buffering off;
        proxy_cache off;
    }
}
```

#### Cline配置使用HTTPS

```json
{
  "mcpServers": {
    "insurance-campaign-remote": {
      "command": "curl",
      "args": [
        "-N",
        "-H", "Accept: text/event-stream",
        "-k",  // 如果使用自签名证书
        "https://your-remote-server.com/connect"
      ]
    }
  }
}
```

### 2. API密钥认证

#### 服务器端添加认证

```java
@PostMapping("/request")
public ResponseEntity<Map<String, Object>> sendRequest(
        @RequestBody McpRequest request,
        @RequestHeader("X-API-Key") String apiKey) {
    
    if (!isValidApiKey(apiKey)) {
        return ResponseEntity.status(401).body(Map.of("error", "Invalid API key"));
    }
    
    // 处理请求...
}
```

#### Cline配置添加认证

```json
{
  "mcpServers": {
    "insurance-campaign-remote": {
      "command": "curl",
      "args": [
        "-N",
        "-H", "Accept: text/event-stream",
        "-H", "X-API-Key: your-secret-api-key",
        "https://your-remote-server.com/connect"
      ]
    }
  }
}
```

## 🧪 测试和验证

### 1. 连接测试

```bash
# 测试远程服务器
./test_remote_connection.sh http://your-remote-server.com:8081

# 测试本地代理
curl http://localhost:8082/health

# 测试SSE连接
curl -N http://localhost:8082/connect
```

### 2. MCP功能测试

```bash
# 测试工具列表
curl -X POST http://your-remote-server.com:8081/request \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "tools/list",
    "params": {}
  }'

# 测试人群定位工具
curl -X POST http://your-remote-server.com:8081/request \
  -H "Content-Type: application/json" \
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

## 🔧 故障排除

### 常见问题及解决方案

#### 1. 连接超时

**问题**: `curl: (7) Failed to connect to remote server`

**解决方案**:
```bash
# 检查网络连接
ping your-remote-server.com

# 检查端口是否开放
telnet your-remote-server.com 8081

# 检查防火墙
sudo ufw status
```

#### 2. CORS错误

**问题**: `Access to fetch at '...' has been blocked by CORS policy`

**解决方案**:
- 使用本地代理脚本
- 配置服务器端CORS
- 使用Node.js代理服务

#### 3. SSL证书问题

**问题**: `SSL certificate problem`

**解决方案**:
```bash
# 临时解决方案（测试用）
curl -k https://your-remote-server.com/connect

# 永久解决方案：配置正确的SSL证书
```

#### 4. 认证失败

**问题**: `401 Unauthorized`

**解决方案**:
- 检查API密钥是否正确
- 验证请求头格式
- 确认服务器端认证配置

### 调试技巧

#### 1. 启用详细日志

```bash
# 使用curl调试模式
curl -v -N http://your-remote-server.com:8081/connect

# Node.js代理调试
DEBUG=* node http_stream_proxy.js http://your-remote-server.com:8081
```

#### 2. 网络诊断

```bash
# 检查DNS解析
nslookup your-remote-server.com

# 检查路由
traceroute your-remote-server.com

# 检查端口连通性
nc -zv your-remote-server.com 8081
```

## 📊 性能优化

### 1. 连接池配置

```javascript
// Node.js代理中的连接池
const http = require('http');
const httpAgent = new http.Agent({
    keepAlive: true,
    maxSockets: 10,
    maxFreeSockets: 2,
    timeout: 60000
});
```

### 2. 缓存配置

```nginx
# Nginx缓存配置
location /static/ {
    proxy_cache my_cache;
    proxy_cache_valid 200 1h;
    proxy_cache_use_stale error timeout invalid_header updating;
}
```

### 3. 负载均衡

```nginx
upstream mcp_backend {
    server backend1:8081;
    server backend2:8081;
    server backend3:8081;
}

server {
    location / {
        proxy_pass http://mcp_backend;
    }
}
```

## 🎯 最佳实践

1. **使用HTTPS** - 保护数据传输安全
2. **配置认证** - 防止未授权访问
3. **监控服务** - 确保高可用性
4. **日志记录** - 便于问题排查
5. **备份配置** - 防止配置丢失
6. **定期测试** - 确保服务正常

## 📋 配置检查清单

- [ ] 远程服务器可访问
- [ ] 端口8081开放
- [ ] SSL证书配置（如使用HTTPS）
- [ ] API密钥配置（如使用认证）
- [ ] Cline配置文件正确
- [ ] 本地代理服务正常（如使用）
- [ ] 网络连接稳定
- [ ] 防火墙规则正确

通过以上配置，您可以成功地将Cline连接到远程的MCP HTTP Stream服务！
