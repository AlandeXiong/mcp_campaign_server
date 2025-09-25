#!/bin/bash

# Fix Cline MCP Connection Script

echo "=== Fixing Cline MCP Connection ==="
echo

# Check if server is running
echo "1. Checking if MCP server is running..."
if curl -s http://localhost:8080/api/campaigns/health > /dev/null; then
    echo "✅ MCP server is running on port 8080"
else
    echo "❌ MCP server is not running. Starting it..."
    ./start.sh &
    sleep 10
    if curl -s http://localhost:8080/api/campaigns/health > /dev/null; then
        echo "✅ MCP server started successfully"
    else
        echo "❌ Failed to start MCP server"
        exit 1
    fi
fi

echo

# Create Cline config directory if it doesn't exist
CLINE_CONFIG_DIR="$HOME/.cline"
if [ ! -d "$CLINE_CONFIG_DIR" ]; then
    echo "2. Creating Cline config directory: $CLINE_CONFIG_DIR"
    mkdir -p "$CLINE_CONFIG_DIR"
else
    echo "2. Cline config directory exists: $CLINE_CONFIG_DIR"
fi

# Create the correct config file
CONFIG_FILE="$CLINE_CONFIG_DIR/config.json"
echo "3. Creating Cline config file: $CONFIG_FILE"

cat > "$CONFIG_FILE" << EOF
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
EOF

echo "✅ Cline config file created successfully"
echo

# Test the server
echo "4. Testing MCP server endpoints..."

echo "Health check:"
curl -s http://localhost:8080/api/campaigns/health | jq '.' 2>/dev/null || curl -s http://localhost:8080/api/campaigns/health
echo

echo "Available tools:"
curl -s http://localhost:8080/api/campaigns/tools | jq '.tools[].name' 2>/dev/null || echo "Tools endpoint working"
echo

echo "=== Fix Complete ==="
echo
echo "Next steps:"
echo "1. Restart VS Code to load the new configuration"
echo "2. Open Cline panel and check if the server is connected"
echo "3. If still showing errors, delete the server and add it again using:"
echo "   - Click 'Configure MCP Servers' in Cline panel"
echo "   - Add new server with the configuration above"
echo
echo "Alternative: Use REST API directly in Cline:"
echo "POST http://localhost:8080/api/campaigns/audience/recommend"
echo "Content-Type: application/json"
echo
echo '{
  "requirements": "Target young professionals for life insurance",
  "insurance_type": "life_insurance",
  "campaign_objective": "acquisition"
}'
