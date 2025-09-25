# Cline集成总结

## 🎯 目标
在VS Code的Cline中调用MCP Campaign服务器，实现保险营销活动管理功能。

## 📋 完成的工作

### 1. MCP服务器开发 ✅
- ✅ Spring Boot 3.2.0项目结构
- ✅ WebSocket MCP协议实现
- ✅ 人群定位服务 (AudienceTargetingService)
- ✅ 内容推荐服务 (CampaignContentService)
- ✅ Claude Inspector支持
- ✅ REST API端点

### 2. 集成文档 ✅
- ✅ Cline集成详细指南 (CLINE_INTEGRATION.md)
- ✅ 快速设置指南 (CLINE_QUICK_SETUP.md)
- ✅ Claude集成指南 (CLAUDE_INTEGRATION.md)

### 3. 配置脚本 ✅
- ✅ 自动设置脚本 (setup_cline.sh)
- ✅ 服务器测试脚本 (test_server.sh)
- ✅ 集成测试脚本 (test_integration.sh)

## 🚀 快速开始

### 步骤1：启动服务器
```bash
cd /Users/xiongjian/project/Mcp_Campaign
./start.sh
```

### 步骤2：配置Cline
在 `~/.cline/config.json` 中添加：
```json
{
  "mcpServers": {
    "insurance-campaign": {
      "command": "mvn",
      "args": [
        "-f",
        "/Users/xiongjian/project/Mcp_Campaign/pom.xml",
        "spring-boot:run",
        "-Dspring-boot.run.jvmArguments=-Dserver.port=8080"
      ],
      "cwd": "/Users/xiongjian/project/Mcp_Campaign"
    }
  }
}
```

### 步骤3：重启VS Code
重启VS Code以加载新配置。

### 步骤4：在Cline中使用
打开Cline面板，开始对话：
```
请帮我分析一下针对年轻专业人士的人寿保险营销活动的人群定位策略
```

## 🛠️ 可用工具

### 1. audience_targeting
**功能**: 人群定位建议
**使用**: 分析目标人群特征，提供精准定位策略

### 2. campaign_content_recommendation  
**功能**: 营销内容推荐
**使用**: 基于人群特征推荐个性化营销内容

### 3. inspector_query
**功能**: Claude Inspector查询
**使用**: 执行Claude Inspector相关查询

## 📊 使用场景

### 营销策略制定
```
请帮我制定一个全面的保险营销策略：
1. 分析年轻家庭对健康保险的需求
2. 推荐营销渠道和内容
3. 提供执行建议
```

### 内容创作
```
请创作人寿保险的邮件营销内容：
- 目标：中年专业人士（35-50岁）
- 目标：提高品牌认知
- 类型：邮件营销序列
```

### 数据分析
```
请分析汽车保险营销活动效果：
- 目标：新车主（18-30岁）
- 渠道：数字广告
- 周期：3个月
```

## 🔧 技术架构

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Cline (VS Code) │    │   MCP Server     │    │  Campaign APIs  │
│                 │    │                  │    │                 │
│  - 对话界面      │◄──►│  - WebSocket     │◄──►│  - 人群定位      │
│  - 工具调用      │    │  - JSON-RPC 2.0  │    │  - 内容推荐      │
│  - 结果展示      │    │  - 错误处理      │    │  - Inspector    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## 🌐 API端点

### WebSocket
- **MCP协议**: `ws://localhost:8080/api/mcp`

### REST API
- **健康检查**: `GET /api/campaigns/health`
- **工具列表**: `GET /api/campaigns/tools`
- **人群推荐**: `POST /api/campaigns/audience/recommend`
- **内容推荐**: `POST /api/campaigns/content/recommend`

## 🎨 支持的功能

### 保险类型
- 人寿保险 (life_insurance)
- 健康保险 (health_insurance)
- 汽车保险 (auto_insurance)
- 财产保险 (property_insurance)
- 旅行保险 (travel_insurance)

### 营销渠道
- 邮件营销 (email)
- 短信营销 (sms)
- 社交媒体 (social_media)
- 推送通知 (push_notifications)
- 直邮 (direct_mail)

### 目标人群
- 年轻专业人士 (25-35岁)
- 中年家庭 (30-50岁)
- 老年群体 (55-75岁)
- 新车主 (18-30岁)
- 高收入群体

## 🔍 故障排除

### 常见问题
1. **服务器无法启动**: 检查Java版本和端口占用
2. **Cline无法连接**: 确认配置文件路径和格式
3. **工具调用失败**: 检查WebSocket连接和服务器日志

### 调试命令
```bash
# 检查服务器状态
curl http://localhost:8080/api/campaigns/health

# 查看可用工具
curl http://localhost:8080/api/campaigns/tools

# 检查端口占用
lsof -i :8080
```

## 📈 最佳实践

1. **明确需求**: 提供详细的需求描述
2. **分步执行**: 先人群定位，再内容推荐
3. **测试验证**: 小规模测试后再大规模应用
4. **合规检查**: 确保内容符合保险法规

## 🎉 总结

通过以上配置，您已经成功在Cline中集成了MCP Campaign服务器！现在可以：

- ✅ 进行智能人群定位分析
- ✅ 获得个性化内容推荐
- ✅ 制定精准营销策略
- ✅ 执行Claude Inspector查询

开始您的智能保险营销之旅吧！🚀
