#!/bin/bash

# MCP Campaign Server Streamable HTTP Transport Startup Script

echo "Starting MCP Campaign Server with Streamable HTTP Transport..."

# Default port
PORT=${1:-8081}

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "Error: Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed. Please install Maven 3.6 or higher."
    exit 1
fi

echo "Compiling the application..."
# Use temp-settings.xml if it exists to bypass corporate proxy issues
if [ -f "temp-settings.xml" ]; then
    mvn clean compile --settings temp-settings.xml
else
    mvn clean compile
fi

if [ $? -eq 0 ]; then
    echo "Build successful. Starting the server with Streamable HTTP Transport on port $PORT..."
    echo "=========================================="
    echo "MCP Streamable HTTP Transport Endpoints:"
    echo "  Health Check:     http://localhost:$PORT/mcp/v1/health"
    echo "  Initialize:       POST http://localhost:$PORT/mcp/v1/initialize"
    echo "  List Tools:       POST http://localhost:$PORT/mcp/v1/tools/list"
    echo "  Call Tool:        POST http://localhost:$PORT/mcp/v1/tools/call"
    echo "  SSE Stream:       GET  http://localhost:$PORT/mcp/v1/stream/{clientId}"
    echo "  Stream Message:   POST http://localhost:$PORT/mcp/v1/stream/{clientId}/message"
    echo ""
    echo "Authentication Methods Supported:"
    echo "  - OAuth2 Bearer Token (Authorization header)"
    echo "  - API Key (X-API-Key header)"
    echo "  - Custom Headers"
    echo ""
    echo "Example API Key: mcp-campaign-api-key-12345"
    echo "=========================================="
    echo "Press Ctrl+C to stop the server."
    echo
    
    # Start the Streamable HTTP application
    if [ -f "temp-settings.xml" ]; then
        mvn spring-boot:run -Dspring-boot.run.main-class=com.insurance.mcp.McpStreamableHttpApplication -Dspring-boot.run.jvm-arguments="-Dserver.port=$PORT" --settings temp-settings.xml
    else
        mvn spring-boot:run -Dspring-boot.run.main-class=com.insurance.mcp.McpStreamableHttpApplication -Dspring-boot.run.jvm-arguments="-Dserver.port=$PORT"
    fi
else
    echo "Build failed. Please check the error messages above."
    exit 1
fi
