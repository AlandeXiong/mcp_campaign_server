#!/bin/bash

# MCP Campaign Server - Build All JARs Script
# This script builds both stdio and streamable JARs separately

echo "=========================================="
echo "MCP Campaign Server - Build All JARs"
echo "=========================================="

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    exit 1
fi

echo "Maven version: $(mvn -version | head -n 1)"

# Clean previous builds
echo "Cleaning previous builds..."
mvn clean -q

if [ $? -ne 0 ]; then
    echo "Clean failed. Please check the errors above."
    exit 1
fi

# Compile the project
echo "Compiling project..."
mvn compile -q

if [ $? -ne 0 ]; then
    echo "Compilation failed. Please check the errors above."
    exit 1
fi

# Build default JAR (Streamable HTTP)
echo "Building default JAR (Streamable HTTP)..."
mvn package -q

if [ $? -ne 0 ]; then
    echo "Default JAR build failed. Please check the errors above."
    exit 1
fi

# Build Stdio JAR
echo "Building Stdio JAR..."
mvn package -P stdio -q

if [ $? -ne 0 ]; then
    echo "Stdio JAR build failed. Please check the errors above."
    exit 1
fi

# Build Streamable JAR
echo "Building Streamable JAR..."
mvn package -P streamable -q

if [ $? -ne 0 ]; then
    echo "Streamable JAR build failed. Please check the errors above."
    exit 1
fi

echo "Build completed successfully!"
echo "=========================================="

# List generated files
echo "Generated files:"
echo "1. Default JAR (Streamable HTTP):"
if [ -f "target/mcp-campaign-server-1.0.0.jar" ]; then
    ls -lh target/mcp-campaign-server-1.0.0.jar && echo "   Size: $(du -h target/mcp-campaign-server-1.0.0.jar | cut -f1)"
else
    echo "   Not found"
fi
echo ""

echo "2. Stdio JAR:"
if [ -f "target/mcp-campaign-server-1.0.0-stdio.jar" ]; then
    ls -lh target/mcp-campaign-server-1.0.0-stdio.jar && echo "   Size: $(du -h target/mcp-campaign-server-1.0.0-stdio.jar | cut -f1)"
else
    echo "   Not found"
fi
echo ""

echo "3. Streamable JAR:"
if [ -f "target/mcp-campaign-server-1.0.0-streamable.jar" ]; then
    ls -lh target/mcp-campaign-server-1.0.0-streamable.jar && echo "   Size: $(du -h target/mcp-campaign-server-1.0.0-streamable.jar | cut -f1)"
else
    echo "   Not found"
fi
echo ""

echo "4. Distribution packages:"
ls -lh target/mcp-campaign-server-1.0.0-*.zip 2>/dev/null || echo "   No distribution packages found"
echo ""

echo "=========================================="
echo "Usage Instructions:"
echo ""
echo "Stdio Mode:"
echo "  java -jar target/mcp-campaign-server-1.0.0-stdio.jar --spring.profiles.active=stdio"
echo ""
echo "Streamable HTTP Mode:"
echo "  java -jar target/mcp-campaign-server-1.0.0-streamable.jar --server.port=8081"
echo ""
echo "Default JAR (Streamable HTTP):"
echo "  java -jar target/mcp-campaign-server-1.0.0.jar --server.port=8081"
echo ""
echo "Extract distribution packages for standalone deployment:"
echo "  unzip target/mcp-campaign-server-1.0.0-stdio.zip"
echo "  unzip target/mcp-campaign-server-1.0.0-streamable.zip"
echo "=========================================="
