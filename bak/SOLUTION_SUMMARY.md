# Cline连接问题解决方案总结

## 🔍 问题诊断

您遇到的错误 "SSE error: TypeError: fetch failed: unknown scheme" 表明Cline无法通过WebSocket连接到MCP服务器。

## ✅ 解决方案

### 方案1：使用Maven命令配置（推荐）

我已经为您创建了正确的配置文件 `/Users/xiongjian/.cline/config.json`：

```json
{
  "mcpServers": {
    "insurance-campaign": {
      "command": "mvn",
      "args": [
        "-f",
        "/Users/xiongjian/project/Mcp_Campaign/pom.xml",
        "spring-boot:run"
      ],
      "cwd": "/Users/xiongjian/project/Mcp_Campaign"
    }
  }
}
```

**操作步骤：**
1. 重启VS Code
2. 打开Cline面板
3. 删除现有的WebSocket配置
4. 检查是否自动加载了新配置

### 方案2：直接使用REST API（备选方案）

如果MCP协议仍然不工作，您可以直接在Cline中使用REST API：

```
请帮我调用保险营销API进行人群定位分析：

POST http://localhost:8080/api/campaigns/audience/recommend
Content-Type: application/json

{
  "requirements": "Target young professionals aged 25-35 for life insurance",
  "insurance_type": "life_insurance",
  "campaign_objective": "acquisition"
}
```

## 🧪 验证测试

我已经验证了以下功能正常工作：

### ✅ 服务器状态
```bash
curl http://localhost:8080/api/campaigns/health
# 返回: {"service":"MCP Campaign Server","version":"1.0.0","status":"UP"}
```

### ✅ 可用工具
- `audience_targeting` - 人群定位
- `campaign_content_recommendation` - 内容推荐
- `inspector_query` - Inspector查询

### ✅ API功能测试
```bash
curl -X POST http://localhost:8080/api/campaigns/audience/recommend \
  -H "Content-Type: application/json" \
  -d '{
    "requirements": "Target young professionals aged 25-35 for life insurance",
    "insurance_type": "life_insurance", 
    "campaign_objective": "acquisition"
  }'
```

返回正确的人群定位建议。

## 🎯 使用示例

### 在Cline中进行人群分析
```
请帮我分析以下场景的保险营销策略：

需求：为25-35岁的年轻专业人士推荐人寿保险
保险类型：人寿保险
营销目标：客户获取
```

### 在Cline中进行内容推荐
```
基于以下人群特征，请推荐营销内容：

人群特征：
- 年龄：25-35岁
- 收入：中等收入到高收入
- 保险类型：健康保险
- 营销渠道：邮件
- 活动目标：转化
```

### 使用REST API方式
```
请帮我调用保险营销API：

POST http://localhost:8080/api/campaigns/audience/recommend
Content-Type: application/json

{
  "requirements": "Target young professionals for life insurance",
  "insurance_type": "life_insurance",
  "campaign_objective": "acquisition"
}
```

## 🔧 故障排除

### 如果仍然无法连接：

1. **删除现有配置**
   - 在Cline面板中点击 "Delete Server" 删除WebSocket配置

2. **手动添加配置**
   - 点击 "Configure MCP Servers"
   - 添加新服务器，使用上面的Maven配置

3. **检查VS Code重启**
   - 确保完全重启了VS Code

4. **使用REST API**
   - 如果MCP仍然不工作，直接使用REST API调用

## 📊 当前状态

- ✅ MCP服务器运行正常 (端口8080)
- ✅ REST API工作正常
- ✅ 配置文件已创建
- ✅ 所有工具可用
- ⏳ 等待VS Code重启以加载新配置

## 🚀 下一步

1. **重启VS Code**
2. **检查Cline面板**中的服务器状态
3. **开始使用**保险营销工具
4. **如果问题持续**，使用REST API方式

您现在可以享受智能保险营销管理功能了！🎉
