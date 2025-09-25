# HTTP Stream远程服务接入Cline配置指南

## 概述

当MCP Campaign服务器部署在远程服务器上时，Cline可以通过HTTP Stream方式远程接入服务。

## 远程服务配置方案

### 方案1：直接HTTP Stream连接（推荐）

#### 1. 远程服务器配置

在远程服务器上启动HTTP Stream服务：

```bash
# 在远程服务器上
cd /path/to/Mcp_Campaign
./start_http_stream.sh 8081
```

#### 2. Cline本地配置

在本地 `~/.cline/config.json` 中配置：

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

### 方案2：使用HTTP代理连接

#### 1. 创建本地代理脚本

创建 `local_http_stream_proxy.sh`：

```bash
#!/bin/bash

# 本地HTTP Stream代理脚本
REMOTE_SERVER="http://your-remote-server.com:8081"

echo "Connecting to remote MCP server: $REMOTE_SERVER"

# 建立SSE连接
curl -N \
  -H "Accept: text/event-stream" \
  -H "Cache-Control: no-cache" \
  -H "Connection: keep-alive" \
  "$REMOTE_SERVER/connect"
```

#### 2. Cline配置使用代理脚本

```json
{
  "mcpServers": {
    "insurance-campaign-remote": {
      "command": "./local_http_stream_proxy.sh",
      "cwd": "/Users/xiongjian/project/Mcp_Campaign"
    }
  }
}
```

### 方案3：使用Node.js代理服务

#### 1. 创建Node.js代理

创建 `http_stream_proxy.js`：

```javascript
const http = require('http');
const https = require('https');
const { EventEmitter } = require('events');

class MCPHttpStreamProxy extends EventEmitter {
    constructor(remoteUrl, localPort = 8082) {
        super();
        this.remoteUrl = remoteUrl;
        this.localPort = localPort;
        this.server = null;
    }

    start() {
        this.server = http.createServer((req, res) => {
            this.handleRequest(req, res);
        });

        this.server.listen(this.localPort, () => {
            console.log(`MCP HTTP Stream Proxy listening on port ${this.localPort}`);
            console.log(`Proxying to: ${this.remoteUrl}`);
        });
    }

    handleRequest(req, res) {
        // 设置CORS头
        res.setHeader('Access-Control-Allow-Origin', '*');
        res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');
        res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');

        if (req.method === 'OPTIONS') {
            res.writeHead(200);
            res.end();
            return;
        }

        if (req.url === '/connect') {
            this.handleSSEConnection(req, res);
        } else if (req.url === '/request') {
            this.handleMCPRequest(req, res);
        } else {
            res.writeHead(404);
            res.end('Not Found');
        }
    }

    handleSSEConnection(req, res) {
        res.writeHead(200, {
            'Content-Type': 'text/event-stream',
            'Cache-Control': 'no-cache',
            'Connection': 'keep-alive',
            'Access-Control-Allow-Origin': '*'
        });

        // 连接到远程服务器
        const remoteReq = http.get(`${this.remoteUrl}/connect`, (remoteRes) => {
            remoteRes.on('data', (chunk) => {
                res.write(chunk);
            });

            remoteRes.on('end', () => {
                res.end();
            });
        });

        remoteReq.on('error', (err) => {
            console.error('Remote connection error:', err);
            res.writeHead(500);
            res.end('Connection Error');
        });

        req.on('close', () => {
            remoteReq.destroy();
        });
    }

    handleMCPRequest(req, res) {
        let body = '';
        req.on('data', chunk => {
            body += chunk.toString();
        });

        req.on('end', () => {
            // 转发请求到远程服务器
            const options = {
                hostname: new URL(this.remoteUrl).hostname,
                port: new URL(this.remoteUrl).port,
                path: '/request',
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Content-Length': Buffer.byteLength(body)
                }
            };

            const proxyReq = http.request(options, (proxyRes) => {
                res.writeHead(proxyRes.statusCode, proxyRes.headers);
                proxyRes.pipe(res);
            });

            proxyReq.on('error', (err) => {
                console.error('Proxy request error:', err);
                res.writeHead(500);
                res.end('Proxy Error');
            });

            proxyReq.write(body);
            proxyReq.end();
        });
    }
}

// 使用示例
const remoteUrl = process.env.REMOTE_MCP_URL || 'http://your-remote-server.com:8081';
const localPort = process.env.LOCAL_PORT || 8082;

const proxy = new MCPHttpStreamProxy(remoteUrl, localPort);
proxy.start();
```

#### 2. 创建package.json

```json
{
  "name": "mcp-http-stream-proxy",
  "version": "1.0.0",
  "description": "MCP HTTP Stream Proxy for remote connections",
  "main": "http_stream_proxy.js",
  "scripts": {
    "start": "node http_stream_proxy.js"
  },
  "dependencies": {}
}
```

#### 3. 启动代理服务

```bash
# 设置远程服务器地址
export REMOTE_MCP_URL="http://your-remote-server.com:8081"
export LOCAL_PORT="8082"

# 启动代理
node http_stream_proxy.js
```

#### 4. Cline配置使用本地代理

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

### 方案4：使用Docker容器化部署

#### 1. 创建Dockerfile

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

# 复制项目文件
COPY . .

# 安装Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# 编译项目
RUN mvn clean compile

# 暴露端口
EXPOSE 8081

# 启动HTTP Stream服务
CMD ["mvn", "exec:java", "-Dexec.mainClass=com.insurance.mcp.McpHttpStreamApplication", "-Dexec.args=8081"]
```

#### 2. 创建docker-compose.yml

```yaml
version: '3.8'

services:
  mcp-campaign-server:
    build: .
    ports:
      - "8081:8081"
    environment:
      - SERVER_PORT=8081
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

#### 3. 部署到远程服务器

```bash
# 在远程服务器上
docker-compose up -d
```

#### 4. Cline配置连接远程Docker服务

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

## 安全配置

### 1. 使用HTTPS

#### 配置SSL证书

```bash
# 在远程服务器上配置SSL
# 使用Let's Encrypt或其他SSL证书
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
        "https://your-remote-server.com:8081/connect"
      ]
    }
  }
}
```

### 2. 添加认证

#### 服务器端添加API密钥认证

```java
// 在McpHttpStreamController中添加认证
@PostMapping("/request")
public ResponseEntity<Map<String, Object>> sendRequest(
        @RequestBody McpRequest request,
        @RequestHeader("X-API-Key") String apiKey,
        HttpServletRequest httpRequest) {
    
    if (!isValidApiKey(apiKey)) {
        return ResponseEntity.status(401).body(Map.of("error", "Invalid API key"));
    }
    
    // 处理请求...
}
```

#### Cline配置添加认证头

```json
{
  "mcpServers": {
    "insurance-campaign-remote": {
      "command": "curl",
      "args": [
        "-N",
        "-H", "Accept: text/event-stream",
        "-H", "X-API-Key: your-api-key-here",
        "https://your-remote-server.com:8081/connect"
      ]
    }
  }
}
```

## 测试远程连接

### 1. 测试连接

```bash
# 测试远程服务器是否可访问
curl -I http://your-remote-server.com:8081/health

# 测试SSE连接
curl -N http://your-remote-server.com:8081/connect
```

### 2. 测试MCP请求

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
```

## 故障排除

### 常见问题

1. **连接超时**
   - 检查防火墙设置
   - 验证端口是否开放
   - 检查网络连接

2. **CORS错误**
   - 在服务器端配置CORS
   - 使用代理服务

3. **SSL证书问题**
   - 检查证书有效性
   - 使用自签名证书测试

### 调试命令

```bash
# 检查端口是否开放
telnet your-remote-server.com 8081

# 检查DNS解析
nslookup your-remote-server.com

# 检查路由
traceroute your-remote-server.com
```

## 最佳实践

1. **使用反向代理**（如Nginx）处理SSL和负载均衡
2. **配置防火墙**只开放必要端口
3. **使用API密钥**进行认证
4. **监控服务状态**确保高可用性
5. **配置日志记录**便于问题排查

通过这些配置方案，您可以成功地将Cline连接到远程的MCP HTTP Stream服务！
