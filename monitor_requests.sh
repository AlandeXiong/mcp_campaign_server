#!/bin/bash

# MCP 请求监控脚本
echo "=== MCP 请求实时监控 ==="
echo "监控端口: 8081"
echo "按 Ctrl+C 停止监控"
echo

# 监控服务器日志中的请求
tail -f /dev/null 2>/dev/null || echo "开始监控请求模式..."

echo "监控项目："
echo "1. OPTIONS 预检请求"
echo "2. POST 初始化请求" 
echo "3. 重复请求检测"
echo "4. 请求时间间隔"
echo

# 实时测试请求模式
while true; do
    echo "=== $(date '+%H:%M:%S') 请求测试 ==="
    
    # 测试 OPTIONS 请求
    echo "OPTIONS 请求:"
    curl -X OPTIONS http://localhost:8081/mcp/v1/initialize \
        -H "Origin: http://localhost:6274" \
        -H "Access-Control-Request-Method: POST" \
        -H "Access-Control-Request-Headers: Content-Type,x-custom-auth-headers" \
        -s -w "Status: %{http_code}, Time: %{time_total}s\n" -o /dev/null
    
    # 测试 POST 请求
    echo "POST 请求:"
    curl -X POST http://localhost:8081/mcp/v1/initialize \
        -H "Content-Type: application/json" \
        -H "Origin: http://localhost:6274" \
        -H "x-custom-auth-headers: test-token" \
        -d '{"jsonrpc":"2.0","id":"'$(date +%s)'","method":"initialize","params":{}}' \
        -s -w "Status: %{http_code}, Time: %{time_total}s\n" -o /dev/null
    
    echo "---"
    sleep 2
done
