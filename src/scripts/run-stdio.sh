#!/bin/bash

# MCP Campaign Server - Stdio Mode Runner
# This script runs the stdio JAR with appropriate configuration

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_DIR="$(dirname "$SCRIPT_DIR")"
JAR_FILE="$JAR_DIR/mcp-campaign-server-1.0.0-stdio.jar"
CONFIG_DIR="$JAR_DIR/config"

echo "=========================================="
echo "MCP Campaign Server - Stdio Mode"
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

# Set JVM options
JVM_OPTS="-Xms128m -Xmx512m -Dspring.profiles.active=stdio"

# Set configuration file path if exists
if [ -d "$CONFIG_DIR" ]; then
    CONFIG_OPTS="--spring.config.location=classpath:/application-stdio.yml,file:$CONFIG_DIR/application-stdio.yml"
else
    CONFIG_OPTS="--spring.config.location=classpath:/application-stdio.yml"
fi

echo "Starting MCP Server in Stdio mode..."
echo "Configuration: $CONFIG_OPTS"
echo "=========================================="
echo "Ready for MCP client connections via stdio."
echo "Press Ctrl+C to stop the server."
echo "=========================================="

# Run the JAR
exec java $JVM_OPTS -jar "$JAR_FILE" $CONFIG_OPTS
