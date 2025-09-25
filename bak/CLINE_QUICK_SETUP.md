# Cline快速集成指南

## 方法一：使用Maven运行（推荐）

### 1. 配置Cline

在 `~/.cline/config.json` 中添加以下配置：

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

### 2. 启动服务器

```bash
cd /Users/xiongjian/project/Mcp_Campaign
./start.sh
```

### 3. 重启VS Code

重启VS Code以加载新的Cline配置。

### 4. 在Cline中测试

打开Cline面板，开始新对话，输入：

```
请帮我分析一下针对年轻专业人士的人寿保险营销活动的人群定位策略
```

## 方法二：WebSocket连接

### 1. 启动服务器

```bash
cd /Users/xiongjian/project/Mcp_Campaign
./start.sh
```

### 2. 配置Cline使用WebSocket

在 `~/.cline/config.json` 中添加：

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

### 3. 重启VS Code

## 方法三：REST API调用

如果MCP集成有问题，可以直接使用REST API：

### 1. 启动服务器

```bash
cd /Users/xiongjian/project/Mcp_Campaign
./start.sh
```

### 2. 在Cline中使用HTTP请求

在Cline中，您可以这样请求：

```
请帮我调用保险营销API，获取人群定位建议：

POST http://localhost:8080/api/campaigns/audience/recommend
Content-Type: application/json

{
  "requirements": "Target young professionals aged 25-35 for life insurance",
  "insurance_type": "life_insurance",
  "campaign_objective": "acquisition"
}
```

## 故障排除

### 1. 检查服务器状态

```bash
curl http://localhost:8080/api/campaigns/health
```

### 2. 检查可用工具

```bash
curl http://localhost:8080/api/campaigns/tools
```

### 3. 查看服务器日志

如果使用 `./start.sh` 启动，日志会显示在终端中。

### 4. 检查端口占用

```bash
lsof -i :8080
```

## 使用示例

### 人群定位分析

```
请帮我分析以下场景的人群定位策略：

需求：为25-35岁的年轻专业人士推荐人寿保险
保险类型：人寿保险
营销目标：客户获取
```

### 内容推荐

```
基于以下人群特征，请推荐营销内容：

人群特征：
- 年龄：25-35岁
- 收入：中等收入到高收入
- 保险类型：健康保险
- 营销渠道：邮件
- 活动目标：转化
```

### 综合营销策略

```
我需要制定一个完整的保险营销策略：

1. 目标人群：30-40岁的已婚家庭
2. 产品类型：综合保险套餐（人寿+健康+财产）
3. 营销目标：提升品牌认知和客户获取
4. 预算：中等规模

请提供：
- 人群定位分析
- 内容推荐
- 渠道策略
- 执行建议
```

## 注意事项

1. **服务器必须先启动**：确保MCP服务器在Cline调用之前已经运行
2. **配置文件路径**：确认Cline配置文件的路径正确
3. **重启VS Code**：修改配置后需要重启VS Code
4. **网络连接**：确保localhost:8080可以访问

## 高级用法

### 批量分析

```
请帮我分析多个保险产品的营销策略：

1. 人寿保险 - 年轻专业人士（25-35岁）
2. 健康保险 - 中老年群体（45-65岁）
3. 汽车保险 - 新车主（18-30岁）

为每个产品提供人群定位和内容推荐建议。
```

### 竞品分析

```
请帮我分析保险行业的营销趋势，并制定我们的差异化策略：

- 目标市场：一线城市白领
- 产品类型：综合保险
- 竞争对手：平安、人保、太保
```

通过以上方法，您就可以在Cline中成功使用MCP Campaign服务器了！
