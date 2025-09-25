#!/bin/bash

# Update Cline configuration to use HTTP Stream mode

echo "=== 更新Cline配置为HTTP Stream模式 ==="
echo

# 1. 创建Cline配置目录
CLINE_CONFIG_DIR="$HOME/.cline"
mkdir -p "$CLINE_CONFIG_DIR"

# 2. 备份现有配置
if [ -f "$CLINE_CONFIG_DIR/config.json" ]; then
    echo "1. 备份现有配置..."
    cp "$CLINE_CONFIG_DIR/config.json" "$CLINE_CONFIG_DIR/config.json.backup"
    echo "✅ 配置已备份到: $CLINE_CONFIG_DIR/config.json.backup"
else
    echo "1. 没有现有配置需要备份"
fi

# 3. 创建HTTP Stream配置
echo "2. 创建HTTP Stream配置..."
cat > "$CLINE_CONFIG_DIR/config.json" << EOF
{
  "mcpServers": {
    "insurance-campaign": {
      "command": "mvn",
      "args": [
        "exec:java",
        "-Dexec.mainClass=com.insurance.mcp.McpHttpStreamApplication",
        "-Dexec.args=8081"
      ],
      "cwd": "/Users/xiongjian/project/Mcp_Campaign"
    }
  }
}
EOF

echo "✅ HTTP Stream配置已创建: $CLINE_CONFIG_DIR/config.json"

# 4. 测试编译
echo "3. 测试项目编译..."
mvn clean compile > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✅ 项目编译成功"
else
    echo "❌ 项目编译失败，请检查错误"
    exit 1
fi

# 5. 提供测试命令
echo "4. 提供测试选项..."
echo "✅ 配置更新完成！"
echo
echo "测试选项："
echo "1. 启动HTTP Stream服务器: ./start_http_stream.sh 8081"
echo "2. 测试SSE连接: curl -N http://localhost:8081/connect"
echo "3. 测试健康检查: curl http://localhost:8081/health"
echo
echo "下一步操作："
echo "1. 重启VS Code"
echo "2. 打开Cline面板"
echo "3. 检查服务器连接状态"
echo
echo "如果HTTP Stream方式不工作，可以恢复备份配置："
echo "cp $CLINE_CONFIG_DIR/config.json.backup $CLINE_CONFIG_DIR/config.json"
echo
echo "或者使用Stdio方式："
cat << 'EOF'
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
