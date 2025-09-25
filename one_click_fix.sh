#!/bin/bash

# 一键修复MCP超时问题脚本

echo "=== 一键修复MCP超时问题 ==="
echo

# 1. 创建永久Maven设置文件
echo "1. 创建永久Maven设置文件..."
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
echo "✅ Maven设置文件已创建: ~/.m2/settings.xml"

# 2. 测试Maven是否工作
echo "2. 测试Maven配置..."
mvn --version > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Maven配置正常"
else
    echo "❌ Maven配置有问题"
fi

# 3. 编译项目
echo "3. 编译项目..."
mvn clean compile
if [ $? -eq 0 ]; then
    echo "✅ 项目编译成功"
else
    echo "❌ 项目编译失败"
    exit 1
fi

# 4. 创建Cline配置
echo "4. 创建Cline配置..."
CLINE_CONFIG_DIR="$HOME/.cline"
mkdir -p "$CLINE_CONFIG_DIR"

cat > "$CLINE_CONFIG_DIR/config.json" << EOF
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
EOF
echo "✅ Cline配置已创建: $CLINE_CONFIG_DIR/config.json"

# 5. 测试Stdio服务器
echo "5. 测试Stdio服务器..."
echo "发送测试请求..."
echo '{"jsonrpc":"2.0","id":"1","method":"initialize","params":{}}' | mvn exec:java -Dexec.mainClass=com.insurance.mcp.McpStdioApplication -q > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Stdio服务器测试成功"
else
    echo "⚠️ Stdio服务器测试失败，但配置已创建"
fi

echo
echo "=== 修复完成 ==="
echo
echo "下一步操作："
echo "1. 重启VS Code"
echo "2. 打开Cline面板"
echo "3. 检查服务器连接状态"
echo
echo "如果仍然有问题，可以使用REST API方式："
echo "1. 删除MCP配置"
echo "2. 启动服务器: mvn spring-boot:run"
echo "3. 在Cline中使用REST API调用"
echo
echo "REST API示例："
echo "POST http://localhost:8080/api/campaigns/audience/recommend"
echo "Content-Type: application/json"
echo '{"requirements": "Target young professionals for life insurance", "insurance_type": "life_insurance", "campaign_objective": "acquisition"}'

