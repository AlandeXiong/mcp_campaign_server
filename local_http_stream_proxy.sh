#!/bin/bash

# 本地HTTP Stream代理脚本
# 用于连接远程MCP HTTP Stream服务器

# 配置远程服务器地址
REMOTE_SERVER="${REMOTE_MCP_URL:-http://your-remote-server.com:8081}"

# 检查参数
if [ $# -gt 0 ]; then
    REMOTE_SERVER=$1
fi

echo "Connecting to remote MCP server: $REMOTE_SERVER" >&2

# 检查curl是否可用
if ! command -v curl &> /dev/null; then
    echo "Error: curl is not installed" >&2
    exit 1
fi

# 测试远程服务器连接
echo "Testing connection to remote server..." >&2
if ! curl -I "$REMOTE_SERVER/health" > /dev/null 2>&1; then
    echo "Error: Cannot connect to remote server $REMOTE_SERVER" >&2
    echo "Please check:" >&2
    echo "1. Remote server address is correct" >&2
    echo "2. Port is open and accessible" >&2
    echo "3. Firewall settings" >&2
    exit 1
fi

echo "Connection successful. Starting SSE stream..." >&2

# 建立SSE连接
curl -N \
  -H "Accept: text/event-stream" \
  -H "Cache-Control: no-cache" \
  -H "Connection: keep-alive" \
  "$REMOTE_SERVER/connect"

# 如果连接断开，尝试重连
echo "Connection lost. Attempting to reconnect..." >&2
sleep 5
exec "$0" "$REMOTE_SERVER"
