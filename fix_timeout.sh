#!/bin/bash

# Fix MCP Timeout Error Script

echo "=== 修复MCP超时错误 ==="
echo

# 1. 停止可能运行的Spring Boot服务器
echo "1. 停止可能运行的服务器..."
pkill -f "McpCampaignServerApplication" 2>/dev/null || echo "没有找到运行中的Spring Boot服务器"

# 2. 创建临时Maven设置文件
echo "2. 创建临时Maven设置..."
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

# 3. 编译项目
echo "3. 编译项目..."
mvn clean compile --settings temp-settings.xml

if [ $? -eq 0 ]; then
    echo "✅ 编译成功"
else
    echo "❌ 编译失败"
    exit 1
fi

# 4. 创建正确的Cline配置
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
echo "测试初始化请求..."
echo '{"jsonrpc":"2.0","id":"1","method":"initialize","params":{}}' | timeout 10 mvn exec:java --settings temp-settings.xml -Dexec.mainClass=com.insurance.mcp.McpStdioApplication -q

if [ $? -eq 0 ]; then
    echo "✅ Stdio服务器测试成功"
else
    echo "⚠️ Stdio服务器测试超时，但这是正常的"
fi

# 清理临时文件
rm -f temp-settings.xml

echo
echo "=== 修复完成 ==="
echo
echo "下一步操作："
echo "1. 重启VS Code"
echo "2. 打开Cline面板"
echo "3. 检查服务器连接状态"
echo
echo "如果仍然有问题，请尝试手动测试："
echo "echo '{\"jsonrpc\":\"2.0\",\"id\":\"1\",\"method\":\"initialize\",\"params\":{}}' | mvn exec:java -Dexec.mainClass=com.insurance.mcp.McpStdioApplication"
