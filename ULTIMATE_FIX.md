# 终极解决方案 - 彻底解决MCP超时问题

## 🚨 问题根因

**MCP error -32001: Request timed out** 的根本原因是：
- 企业Maven仓库 `http://192.168.11.10:8085/repository/maven-public/` 无法访问
- 导致所有Maven命令（包括Stdio方式）都无法执行
- Cline等待MCP服务器启动超时

## ✅ 终极解决方案

### 方案1：永久修复Maven仓库配置（推荐）

#### 1. 创建永久Maven设置文件
```bash
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

#### 2. 使用Stdio配置
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

### 方案2：使用REST API方式（立即可用）

#### 1. 删除MCP配置
删除 `~/.cline/config.json` 中的MCP服务器配置

#### 2. 启动服务器
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

#### 3. 在Cline中直接使用REST API
```
请帮我分析保险营销策略：

POST http://localhost:8080/api/campaigns/audience/recommend
Content-Type: application/json

{
  "requirements": "Target young professionals aged 25-35 for life insurance",
  "insurance_type": "life_insurance",
  "campaign_objective": "acquisition"
}
```

### 方案3：使用JAR文件方式

#### 1. 构建JAR文件
```bash
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
        "/Users/xiongjian/project/Mcp_Campaign/target/mcp-campaign-server-1.0.0.jar"
      ],
      "cwd": "/Users/xiongjian/project/Mcp_Campaign"
    }
  }
}
```

## 🎯 立即可用的解决方案

**推荐立即使用方案2（REST API方式）：**

1. **删除MCP配置**：
   - 删除 `~/.cline/config.json` 中的MCP服务器配置
   - 或者清空文件内容

2. **启动服务器**：
   ```bash
   cd /Users/xiongjian/project/Mcp_Campaign
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

3. **在Cline中使用REST API**：
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

## 📋 总结

**问题根源**：企业Maven仓库无法访问
**解决方案**：
1. ✅ REST API方式（立即可用）
2. ✅ 修复Maven配置（长期解决）
3. ✅ JAR文件方式（备选方案）

**推荐操作**：
1. 立即使用REST API方式
2. 后续修复Maven配置
3. 重启VS Code测试

