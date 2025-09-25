# Claude集成指南

本指南详细说明如何在Claude中调用MCP Campaign服务器。

## 1. 启动MCP服务器

首先启动MCP服务器：

```bash
cd /Users/xiongjian/project/Mcp_Campaign
./start.sh
```

服务器将在 `http://localhost:8080` 启动，WebSocket端点位于 `ws://localhost:8080/api/mcp`

## 2. Claude Desktop集成

### 2.1 配置文件位置

Claude Desktop的配置文件位置：
- **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Windows**: `%APPDATA%\Claude\claude_desktop_config.json`
- **Linux**: `~/.config/Claude/claude_desktop_config.json`

### 2.2 配置MCP服务器

在Claude Desktop配置文件中添加以下配置：

```json
{
  "mcpServers": {
    "insurance-campaign": {
      "command": "java",
      "args": [
        "-jar",
        "/Users/xiongjian/project/Mcp_Campaign/target/mcp-campaign-server-1.0.0.jar"
      ],
      "env": {
        "SERVER_PORT": "8080"
      }
    }
  }
}
```

### 2.3 替代配置方式（使用WebSocket）

如果上述方式不工作，可以使用WebSocket连接：

```json
{
  "mcpServers": {
    "insurance-campaign": {
      "type": "websocket",
      "url": "ws://localhost:8080/api/mcp"
    }
  }
}
```

## 3. 在Claude中调用工具

### 3.1 人群定位工具

在Claude中，您可以这样调用人群定位功能：

```
请帮我分析一下针对年轻专业人士的人寿保险营销活动的人群定位策略。具体要求是：
- 目标人群：25-35岁的年轻专业人士
- 保险类型：人寿保险
- 营销目标：获取新客户
```

Claude将自动调用 `audience_targeting` 工具并返回详细的人群定位建议。

### 3.2 活动内容推荐工具

您可以请求内容推荐：

```
基于以下人群特征，请推荐适合的营销内容：
- 年龄：25-35岁
- 收入：中等收入到高收入
- 职业：专业人士
- 保险类型：人寿保险
- 营销渠道：邮件
- 活动目标：转化
```

### 3.3 Inspector查询工具

对于Claude Inspector相关查询：

```
请使用Inspector工具分析最近的保险营销活动效果
```

## 4. 可用的MCP工具

### 4.1 audience_targeting

**功能**: 提供保险营销活动的人群定位建议

**参数**:
- `requirements` (必需): 用户对人群定位的要求
- `insurance_type` (可选): 保险类型 (life_insurance, health_insurance, auto_insurance, property_insurance, travel_insurance)
- `campaign_objective` (可选): 活动目标 (acquisition, retention, upselling)

**示例调用**:
```json
{
  "name": "audience_targeting",
  "arguments": {
    "requirements": "Target young professionals aged 25-35 for life insurance acquisition",
    "insurance_type": "life_insurance",
    "campaign_objective": "acquisition"
  }
}
```

### 4.2 campaign_content_recommendation

**功能**: 推荐个性化的保险营销内容

**参数**:
- `audience_criteria` (必需): 目标人群标准
- `insurance_type` (必需): 保险产品类型
- `channel` (可选): 营销渠道 (email, sms, social_media, push_notifications)
- `campaign_goal` (可选): 活动目标 (awareness, conversion, retention)

**示例调用**:
```json
{
  "name": "campaign_content_recommendation",
  "arguments": {
    "audience_criteria": {
      "minAge": 25,
      "maxAge": 35,
      "incomeRange": ["middle_income", "high_income"]
    },
    "insurance_type": "life_insurance",
    "channel": "email",
    "campaign_goal": "conversion"
  }
}
```

### 4.3 inspector_query

**功能**: 执行Claude Inspector工具查询

**参数**:
- `query` (必需): Inspector查询内容

**示例调用**:
```json
{
  "name": "inspector_query",
  "arguments": {
    "query": "Analyze the effectiveness of recent insurance marketing campaigns"
  }
}
```

## 5. 测试集成

### 5.1 验证服务器运行

```bash
# 检查服务器状态
curl http://localhost:8080/api/campaigns/health

# 查看可用工具
curl http://localhost:8080/api/campaigns/tools
```

### 5.2 测试REST API

```bash
# 测试人群推荐
curl -X POST http://localhost:8080/api/campaigns/audience/recommend \
  -H "Content-Type: application/json" \
  -d '{
    "requirements": "Target young professionals for life insurance",
    "insurance_type": "life_insurance",
    "campaign_objective": "acquisition"
  }'

# 测试内容推荐
curl -X POST http://localhost:8080/api/campaigns/content/recommend \
  -H "Content-Type: application/json" \
  -d '{
    "audience_criteria": {
      "minAge": 25,
      "maxAge": 35,
      "incomeRange": ["middle_income"]
    },
    "insurance_type": "life_insurance",
    "channel": "email",
    "campaign_goal": "conversion"
  }'
```

## 6. 故障排除

### 6.1 常见问题

1. **服务器无法启动**
   - 检查Java版本 (需要Java 17+)
   - 检查端口8080是否被占用

2. **Claude无法连接**
   - 确认服务器正在运行
   - 检查配置文件路径和格式
   - 重启Claude Desktop

3. **工具调用失败**
   - 检查WebSocket连接
   - 查看服务器日志

### 6.2 日志查看

```bash
# 查看服务器日志
tail -f logs/spring.log

# 或使用Maven运行查看实时日志
mvn spring-boot:run
```

## 7. 高级用法

### 7.1 自定义保险类型

您可以扩展支持更多保险类型：
- 车险 (auto_insurance)
- 健康险 (health_insurance)
- 财产险 (property_insurance)
- 旅行险 (travel_insurance)
- 人寿险 (life_insurance)

### 7.2 多渠道营销

支持多种营销渠道：
- 邮件营销 (email)
- 短信营销 (sms)
- 社交媒体 (social_media)
- 推送通知 (push_notifications)
- 直邮 (direct_mail)

### 7.3 个性化内容

系统会根据以下因素推荐个性化内容：
- 年龄范围
- 收入水平
- 地理位置
- 兴趣爱好
- 购买历史
- 风险偏好

## 8. 最佳实践

1. **明确需求**: 在调用工具时提供详细的需求描述
2. **分步执行**: 先进行人群定位，再基于结果推荐内容
3. **测试验证**: 在生产环境使用前先进行小规模测试
4. **合规检查**: 确保所有营销内容符合保险行业法规要求

通过以上配置，您就可以在Claude中无缝使用保险营销活动管理功能了！
