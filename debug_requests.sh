#!/bin/bash

# MCP Request Debug Script
echo "=== MCP Request Debug Analysis ==="
echo

# 1. 检查服务器状态
echo "1. 检查服务器状态..."
curl -s http://localhost:8081/mcp/v1/health | jq '.' 2>/dev/null || curl -s http://localhost:8081/mcp/v1/health
echo

# 2. 测试 OPTIONS 预检请求
echo "2. 测试 CORS 预检请求..."
echo "OPTIONS /mcp/v1/initialize"
curl -X OPTIONS http://localhost:8081/mcp/v1/initialize \
  -H "Origin: http://localhost:6274" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type,x-custom-auth-headers" \
  -v 2>&1 | grep -E "(< HTTP|< Access-Control)"
echo

# 3. 测试初始化请求（无认证）
echo "3. 测试初始化请求（无认证）..."
curl -X POST http://localhost:8081/mcp/v1/initialize \
  -H "Content-Type: application/json" \
  -H "Origin: http://localhost:6274" \
  -d '{"jsonrpc":"2.0","id":"1","method":"initialize","params":{}}' \
  -w "\nHTTP Status: %{http_code}\nTotal Time: %{time_total}s\n"
echo

# 4. 测试初始化请求（API Key 认证）
echo "4. 测试初始化请求（API Key 认证）..."
curl -X POST http://localhost:8081/mcp/v1/initialize \
  -H "Content-Type: application/json" \
  -H "Origin: http://localhost:6274" \
  -H "X-API-Key: mcp-campaign-api-key-12345" \
  -d '{"jsonrpc":"2.0","id":"2","method":"initialize","params":{}}' \
  -w "\nHTTP Status: %{http_code}\nTotal Time: %{time_total}s\n"
echo

# 5. 测试初始化请求（Custom Headers 认证）
echo "5. 测试初始化请求（Custom Headers 认证）..."
curl -X POST http://localhost:8081/mcp/v1/initialize \
  -H "Content-Type: application/json" \
  -H "Origin: http://localhost:6274" \
  -H "x-custom-auth-headers: test-value" \
  -d '{"jsonrpc":"2.0","id":"3","method":"initialize","params":{}}' \
  -w "\nHTTP Status: %{http_code}\nTotal Time: %{time_total}s\n"
echo

# 6. 模拟浏览器的完整请求序列
echo "6. 模拟浏览器请求序列..."
echo "步骤 1: OPTIONS 预检"
curl -X OPTIONS http://localhost:8081/mcp/v1/initialize \
  -H "Origin: http://localhost:6274" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type,x-custom-auth-headers" \
  -s -o /dev/null -w "Status: %{http_code}\n"

echo "步骤 2: POST 初始化"
curl -X POST http://localhost:8081/mcp/v1/initialize \
  -H "Content-Type: application/json" \
  -H "Origin: http://localhost:6274" \
  -H "x-custom-auth-headers: MCP_PROXY_AUTH_TOKEN=eb1fcbc82a19e4b43f14a0e8f562faa6fd18ba51b373d4e68018de097ca06c07:1" \
  -d '{"jsonrpc":"2.0","id":"browser-test","method":"initialize","params":{}}' \
  -w "Status: %{http_code}\nTotal Time: %{time_total}s\n"
echo

# 7. 检查服务器日志中的重复请求
echo "7. 分析可能的重复请求原因..."
echo "常见原因："
echo "- CORS 预检请求 + 实际请求 = 2次"
echo "- 认证失败重试 = 多次"
echo "- 连接超时重试 = 多次"
echo "- 客户端自动重连 = 多次"
echo

echo "=== 诊断完成 ==="
