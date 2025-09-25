# MCP超时错误最终解决方案

## 🔍 报错原因分析

**"MCP error -32001: Request timed out"** 错误的根本原因：

1. **企业Maven仓库问题** - 无法连接到 `http://192.168.11.10:8085/repository/maven-public/`
2. **Spring Boot服务器无法启动** - 因为依赖下载失败
3. **Cline等待超时** - 没有可用的MCP服务器响应

## ✅ 最终解决方案

### 方案1：使用REST API方式（推荐）

既然MCP服务器有启动问题，我们可以直接使用REST API：

#### 1. 启动现有的WebSocket服务器
```bash
# 如果您之前成功启动过服务器
curl http://localhost:8080/api/campaigns/health
```

#### 2. 在Cline中直接使用REST API
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

### 方案2：修复Maven仓库问题

#### 1. 创建永久Maven设置文件
```bash
# 创建 ~/.m2/settings.xml
mkdir -p ~/.m2
cat > ~/.m2/settings.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <mirrors>
        <mirror>
            <id>central</id>
            <name>Maven Central</name>
            <url>https://repo1.maven.org/maven2</url>
            <mirrorOf>*</mirrorOf>
        </mirror>
    </mirrors>
</settings>
EOF
```

#### 2. 重新配置Cline
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

### 方案3：使用JAR文件方式

#### 1. 构建JAR文件
```bash
# 使用临时设置文件构建
cat > temp-settings.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <mirrors>
        <mirror>
            <id>central</id>
            <name>Maven Central</name>
            <url>https://repo1.maven.org/maven2</url>
            <mirrorOf>*</mirrorOf>
        </mirror>
    </mirrors>
</settings>
EOF

mvn clean package -DskipTests --settings temp-settings.xml
```

#### 2. 配置Cline使用JAR
```json
{
  "mcpServers": {
    "insurance-campaign": {
      "command": "java",
      "args": [
        "-jar",
        "/Users/xiongjian/project/Mcp_Campaign/target/mcp-campaign-server-1.0.0.jar",
        "--stdio"
      ]
    }
  }
}
```

## 🚀 立即解决方案

### 最简单的解决方法：

1. **检查是否有运行的服务器**：
```bash
curl http://localhost:8080/api/campaigns/health
```

2. **如果有响应，直接在Cline中使用REST API**：
```
请帮我分析保险营销策略：

POST http://localhost:8080/api/campaigns/audience/recommend
Content-Type: application/json

{
  "requirements": "Target young professionals for life insurance",
  "insurance_type": "life_insurance",
  "campaign_objective": "acquisition"
}
```

3. **如果没有响应，启动服务器**：
```bash
# 使用临时设置启动
cat > temp-settings.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <mirrors>
        <mirror>
            <id>central</id>
            <name>Maven Central</name>
            <url>https://repo1.maven.org/maven2</url>
            <mirrorOf>*</mirrorOf>
        </mirror>
    </mirrors>
</settings>
EOF

mvn spring-boot:run --settings temp-settings.xml
```

## 📋 总结

**超时错误的根本原因**：
- ❌ 企业Maven仓库无法访问
- ❌ Spring Boot应用无法启动
- ❌ MCP服务器不可用

**推荐解决方案**：
- ✅ 使用REST API方式（最简单）
- ✅ 修复Maven仓库配置（长期解决）
- ✅ 使用JAR文件方式（备选方案）

**立即行动**：
1. 检查现有服务器状态
2. 如果可用，使用REST API
3. 如果不可用，启动服务器
4. 重启VS Code并测试
