#!/bin/bash

# 远程HTTP Stream连接测试脚本

echo "=== 远程HTTP Stream连接测试 ==="
echo

# 默认远程服务器地址
REMOTE_SERVER="http://localhost:8081"

# 如果提供了参数，使用参数作为远程服务器地址
if [ $# -gt 0 ]; then
    REMOTE_SERVER=$1
fi

echo "测试远程服务器: $REMOTE_SERVER"
echo

# 1. 测试基本连接
echo "1. 测试基本连接..."
if curl -I "$REMOTE_SERVER/health" > /dev/null 2>&1; then
    echo "✅ 远程服务器可访问"
else
    echo "❌ 远程服务器不可访问"
    echo "请检查："
    echo "- 服务器地址是否正确"
    echo "- 端口是否开放"
    echo "- 防火墙设置"
    exit 1
fi

# 2. 测试健康检查
echo "2. 测试健康检查..."
HEALTH_RESPONSE=$(curl -s "$REMOTE_SERVER/health")
if [ $? -eq 0 ]; then
    echo "✅ 健康检查成功"
    echo "响应: $HEALTH_RESPONSE"
else
    echo "❌ 健康检查失败"
fi

# 3. 测试SSE连接（短时间测试）
echo "3. 测试SSE连接..."
echo "发送测试请求到SSE端点..."
timeout 5 curl -N -H "Accept: text/event-stream" "$REMOTE_SERVER/connect" > /dev/null 2>&1
if [ $? -eq 0 ] || [ $? -eq 124 ]; then
    echo "✅ SSE连接正常"
else
    echo "❌ SSE连接失败"
fi

# 4. 测试MCP请求
echo "4. 测试MCP请求..."
MCP_RESPONSE=$(curl -s -X POST "$REMOTE_SERVER/request" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": "1",
    "method": "tools/list",
    "params": {}
  }')

if [ $? -eq 0 ] && [[ $MCP_RESPONSE == *"tools"* ]]; then
    echo "✅ MCP请求成功"
    echo "工具数量: $(echo $MCP_RESPONSE | grep -o '"name"' | wc -l)"
else
    echo "❌ MCP请求失败"
    echo "响应: $MCP_RESPONSE"
fi

echo
echo "=== 测试完成 ==="
echo
echo "如果所有测试都通过，可以使用以下Cline配置："
echo
cat << EOF
{
  "mcpServers": {
    "insurance-campaign-remote": {
      "command": "curl",
      "args": [
        "-N",
        "-H", "Accept: text/event-stream",
        "$REMOTE_SERVER/connect"
      ]
    }
  }
}
EOF
echo
echo "或者使用本地代理脚本："
echo "1. 创建 local_http_stream_proxy.sh"
echo "2. 设置 REMOTE_SERVER=\"$REMOTE_SERVER\""
echo "3. 配置Cline使用代理脚本"
