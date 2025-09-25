#!/bin/bash

# MCP Campaign Server Integration Test Script

echo "=== MCP Campaign Server Integration Test ==="
echo

# Check if server is running
echo "1. Checking if server is running..."
if curl -s http://localhost:8080/api/campaigns/health > /dev/null; then
    echo "✅ Server is running on port 8080"
else
    echo "❌ Server is not running. Please start it first with: ./start.sh"
    exit 1
fi

echo

# Test health endpoint
echo "2. Testing health endpoint..."
HEALTH_RESPONSE=$(curl -s http://localhost:8080/api/campaigns/health)
echo "Health Response: $HEALTH_RESPONSE"
echo

# Test tools endpoint
echo "3. Testing tools endpoint..."
TOOLS_RESPONSE=$(curl -s http://localhost:8080/api/campaigns/tools)
echo "Available tools:"
echo "$TOOLS_RESPONSE" | jq '.tools[].name' 2>/dev/null || echo "$TOOLS_RESPONSE"
echo

# Test audience targeting
echo "4. Testing audience targeting..."
AUDIENCE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/campaigns/audience/recommend \
  -H "Content-Type: application/json" \
  -d '{
    "requirements": "Target young professionals aged 25-35 for life insurance",
    "insurance_type": "life_insurance",
    "campaign_objective": "acquisition"
  }')

echo "Audience targeting response:"
echo "$AUDIENCE_RESPONSE" | jq '.' 2>/dev/null || echo "$AUDIENCE_RESPONSE"
echo

# Test content recommendation
echo "5. Testing content recommendation..."
CONTENT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/campaigns/content/recommend \
  -H "Content-Type: application/json" \
  -d '{
    "audience_criteria": {
      "minAge": 25,
      "maxAge": 35,
      "incomeRange": ["middle_income", "high_income"]
    },
    "insurance_type": "life_insurance",
    "channel": "email",
    "campaign_goal": "conversion"
  }')

echo "Content recommendation response:"
echo "$CONTENT_RESPONSE" | jq '.recommended_content[0].title' 2>/dev/null || echo "$CONTENT_RESPONSE"
echo

# Test WebSocket connection (basic check)
echo "6. Testing WebSocket endpoint availability..."
if nc -z localhost 8080; then
    echo "✅ WebSocket endpoint is accessible"
else
    echo "❌ WebSocket endpoint is not accessible"
fi

echo
echo "=== Test Complete ==="
echo
echo "If all tests passed, you can now configure Claude Desktop:"
echo
echo "1. Add the following to your Claude Desktop config:"
echo '{
  "mcpServers": {
    "insurance-campaign": {
      "command": "java",
      "args": ["-jar", "'$(pwd)'/target/mcp-campaign-server-1.0.0.jar"],
      "env": {"SERVER_PORT": "8080"}
    }
  }
}'
echo
echo "2. Restart Claude Desktop"
echo
echo "3. Start using the insurance campaign tools in Claude!"
