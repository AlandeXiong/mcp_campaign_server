# MCP Inspector 配置指南

## 🚀 快速配置

### 1. 启动服务器

```bash
# 停止现有进程
pkill -f "spring-boot:run"

# 启动 Streamable HTTP 服务器
./start_streamable_http.sh 8081
```

### 2. MCP Inspector 配置

在 MCP Inspector 中设置以下参数：

#### 基本配置
- **Transport Type**: `Streamable HTTP`
- **URL**: `http://localhost:8081/mcp/v1/initialize`
- **Connection Type**: `Direct`

#### 认证配置（可选）
点击 **Authentication** 按钮，选择以下任一方式：

**方式 1: API Key**
- **Type**: `API Key`
- **Key**: `mcp-campaign-api-key-12345`

**方式 2: Bearer Token**
- **Type**: `Bearer Token`
- **Token**: `your-bearer-token`

**方式 3: Custom Headers**
- **Type**: `Custom Headers`
- **Header Name**: `x-custom-auth-headers`
- **Header Value**: `your-custom-value`

### 3. 测试连接

#### 命令行测试
```bash
# 测试初始化
curl -X POST http://localhost:8081/mcp/v1/initialize \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"1","method":"initialize","params":{}}'

# 测试工具列表
curl -X POST http://localhost:8081/mcp/v1/tools/list \
  -H "Content-Type: application/json" \
  -H "X-API-Key: mcp-campaign-api-key-12345" \
  -d '{"jsonrpc":"2.0","id":"2","method":"tools/list","params":{}}'

# 健康检查
curl http://localhost:8081/mcp/v1/health
```

#### MCP Inspector 测试
1. 点击 **Connect** 按钮
2. 应该看到连接成功状态
3. 查看可用工具列表

### 4. 可用工具

连接成功后，您可以使用以下工具：

1. **audience_targeting** - 人群圈选推荐
2. **campaign_content_recommendation** - 活动内容推荐
3. **inspector_query** - Claude Inspector 查询
4. **streaming_analysis** - 流式分析

### 5. 故障排除

#### 问题 1: CORS 错误
```
Access to fetch at 'http://localhost:8081/mcp/v1/initialize' from origin 'http://localhost:6274' has been blocked by CORS policy
```

**解决方案**: 服务器已配置 CORS，支持跨域请求。如果仍有问题，检查：
- 服务器是否正在运行
- URL 是否正确
- 认证头是否正确

#### 问题 2: 连接超时
```
Connection Error - Check if your MCP server is running and proxy token is correct
```

**解决方案**:
1. 确认服务器正在运行：`curl http://localhost:8081/mcp/v1/health`
2. 检查端口是否被占用：`lsof -i :8081`
3. 重启服务器：`./start_streamable_http.sh 8081`

#### 问题 3: 认证失败
```
401 Unauthorized
```

**解决方案**:
1. 添加正确的 API Key：`X-API-Key: mcp-campaign-api-key-12345`
2. 或使用 Bearer Token
3. 或配置自定义头部认证

### 6. 服务器端点

- **健康检查**: `GET http://localhost:8081/mcp/v1/health`
- **初始化**: `POST http://localhost:8081/mcp/v1/initialize`
- **工具列表**: `POST http://localhost:8081/mcp/v1/tools/list`
- **调用工具**: `POST http://localhost:8081/mcp/v1/tools/call`
- **SSE 流**: `GET http://localhost:8081/mcp/v1/stream/{clientId}`
- **流消息**: `POST http://localhost:8081/mcp/v1/stream/{clientId}/message`

### 7. 示例工具调用

```json
{
  "jsonrpc": "2.0",
  "id": "3",
  "method": "tools/call",
  "params": {
    "name": "audience_targeting",
    "arguments": {
      "product_type": "life_insurance",
      "target_demographics": {
        "age_range": "25-45",
        "income_level": "middle"
      }
    }
  }
}
```

## ✅ 成功指标

连接成功后，您应该看到：
1. MCP Inspector 显示 "Connected" 状态
2. 工具列表显示 4 个可用工具
3. 可以成功调用工具并获得响应

## 📞 需要帮助？

如果仍有问题，请提供：
1. 服务器启动日志
2. MCP Inspector 错误截图
3. 浏览器开发者工具的网络请求详情

