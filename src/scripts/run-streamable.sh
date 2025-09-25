#!/bin/bash

# MCP Campaign Server - Streamable HTTP Mode Runner
# This script runs the streamable JAR with appropriate configuration

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_DIR="$(dirname "$SCRIPT_DIR")"
JAR_FILE="$JAR_DIR/mcp-campaign-server-1.0.0-streamable.jar"
CONFIG_DIR="$JAR_DIR/config"

# Default port
PORT=${1:-8081}

echo "=========================================="
echo "MCP Campaign Server - Streamable HTTP Mode"
echo "=========================================="

# Check if JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found: $JAR_FILE"
    echo "Please run 'mvn clean package' first to build the JAR."
    exit 1
fi

# Check Java version
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java 17 or higher"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "Error: Java 17 or higher is required, found Java $JAVA_VERSION"
    exit 1
fi

echo "Java version: $(java -version 2>&1 | head -n 1)"
echo "JAR file: $JAR_FILE"
echo "Port: $PORT"

# Set JVM options
JVM_OPTS="-Xms256m -Xmx1g -Dserver.port=$PORT"

# Set configuration file path if exists
if [ -d "$CONFIG_DIR" ]; then
    CONFIG_OPTS="--spring.config.location=classpath:/application.yml,file:$CONFIG_DIR/application.yml"
else
    CONFIG_OPTS="--spring.config.location=classpath:/application.yml"
fi

echo "Starting MCP Server in Streamable HTTP mode..."
echo "Configuration: $CONFIG_OPTS"
echo "=========================================="
echo "MCP Streamable HTTP Transport Endpoints:"
echo "  Health Check:     http://localhost:$PORT/mcp/v1/health"
echo "  Initialize:       POST http://localhost:$PORT/mcp/v1/initialize"
echo "  List Tools:       POST http://localhost:$PORT/mcp/v1/tools/list"
echo "  Call Tool:        POST http://localhost:$PORT/mcp/v1/tools/call"
echo "  SSE Stream:       GET  http://localhost:$PORT/mcp/v1/stream/{clientId}"
echo "  Stream Message:   POST http://localhost:$PORT/mcp/v1/stream/{clientId}/message"
echo "Authentication: DISABLED"
echo "=========================================="
echo "Press Ctrl+C to stop the server."

# Run the JAR
exec java $JVM_OPTS -jar "$JAR_FILE" $CONFIG_OPTS
