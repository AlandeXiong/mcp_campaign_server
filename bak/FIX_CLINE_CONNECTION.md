# 修复Cline连接问题

## 问题诊断

从错误信息 "SSE error: TypeError: fetch failed: unknown scheme" 可以看出，Cline无法通过WebSocket连接到MCP服务器。

## 解决方案

### 方案1：使用Maven命令方式（推荐）

删除现有的WebSocket配置，改用Maven命令方式：

1. **删除现有的MCP服务器配置**
   - 在Cline面板中点击 "Delete Server" 删除现有的配置

2. **使用Maven命令配置**
   
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

3. **重启VS Code**

### 方案2：使用HTTP REST API方式

如果MCP协议不工作，可以直接使用REST API：

1. **在Cline中直接调用API**
   ```
   请帮我调用保险营销API：
   
   POST http://localhost:8080/api/campaigns/audience/recommend
   Content-Type: application/json
   
   {
     "requirements": "Target young professionals aged 25-35 for life insurance",
     "insurance_type": "life_insurance",
     "campaign_objective": "acquisition"
   }
   ```

### 方案3：创建JAR文件方式

1. **创建临时Maven设置文件**
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
   ```

2. **构建JAR文件**
   ```bash
   mvn clean package -DskipTests --settings temp-settings.xml
   ```

3. **使用JAR配置**
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

## 验证步骤

1. **检查服务器状态**
   ```bash
   curl http://localhost:8080/api/campaigns/health
   ```

2. **测试API调用**
   ```bash
   curl -X POST http://localhost:8080/api/campaigns/audience/recommend \
     -H "Content-Type: application/json" \
     -d '{
       "requirements": "Target young professionals for life insurance",
       "insurance_type": "life_insurance",
       "campaign_objective": "acquisition"
     }'
   ```

3. **在Cline中测试**
   - 重启VS Code
   - 打开Cline面板
   - 开始新对话并测试工具调用

## 故障排除

### 如果仍然无法连接：

1. **检查Cline版本**
   - 确保使用最新版本的Cline扩展

2. **检查配置文件路径**
   - macOS: `~/.cline/config.json`
   - Windows: `%USERPROFILE%\.cline\config.json`
   - Linux: `~/.config/cline/config.json`

3. **检查JSON格式**
   - 确保JSON格式正确，没有语法错误

4. **查看Cline日志**
   - 打开VS Code输出面板
   - 选择"Cline"输出通道
   - 查看详细错误信息

## 推荐配置

基于当前情况，推荐使用方案1（Maven命令方式）：

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

这种方式最稳定，因为：
- 不依赖WebSocket连接
- 直接启动Spring Boot应用
- 避免了网络连接问题
