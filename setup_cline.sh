#!/bin/bash

# Cline MCP Server Setup Script

echo "=== Cline MCP Campaign Server Setup ==="
echo

# Get current directory
PROJECT_DIR=$(pwd)
JAR_PATH="$PROJECT_DIR/target/mcp-campaign-server-1.0.0.jar"

echo "Project directory: $PROJECT_DIR"
echo "JAR path: $JAR_PATH"
echo

# Check if JAR exists
if [ ! -f "$JAR_PATH" ]; then
    echo "❌ JAR file not found. Building the project..."
    mvn clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        echo "✅ Project built successfully"
    else
        echo "❌ Build failed. Please check the errors above."
        exit 1
    fi
else
    echo "✅ JAR file found"
fi

echo

# Create Cline config directory if it doesn't exist
CLINE_CONFIG_DIR="$HOME/.cline"
if [ ! -d "$CLINE_CONFIG_DIR" ]; then
    echo "Creating Cline config directory: $CLINE_CONFIG_DIR"
    mkdir -p "$CLINE_CONFIG_DIR"
fi

# Create config file
CONFIG_FILE="$CLINE_CONFIG_DIR/config.json"
echo "Creating Cline config file: $CONFIG_FILE"

cat > "$CONFIG_FILE" << EOF
{
  "mcpServers": {
    "insurance-campaign": {
      "command": "java",
      "args": [
        "-jar",
        "$JAR_PATH"
      ],
      "env": {
        "SERVER_PORT": "8080"
      }
    }
  }
}
EOF

echo "✅ Cline config file created"
echo

# Start the server
echo "Starting MCP Campaign Server..."
echo "You can start the server manually with: ./start.sh"
echo "Or run it in the background with: nohup ./start.sh &"
echo

# Provide instructions
echo "=== Next Steps ==="
echo
echo "1. Start the MCP server:"
echo "   ./start.sh"
echo
echo "2. Restart VS Code to load the new Cline configuration"
echo
echo "3. Open Cline in VS Code and start a new conversation"
echo
echo "4. Test the integration by asking:"
echo "   '请帮我分析一下针对年轻专业人士的人寿保险营销活动的人群定位策略'"
echo
echo "=== Available Tools ==="
echo "- audience_targeting: 人群定位建议"
echo "- campaign_content_recommendation: 营销内容推荐"
echo "- inspector_query: Claude Inspector查询"
echo
echo "=== Server Endpoints ==="
echo "- WebSocket: ws://localhost:8080/api/mcp"
echo "- REST API: http://localhost:8080/api/campaigns"
echo "- Health Check: http://localhost:8080/api/campaigns/health"
echo
echo "Setup complete! 🎉"
