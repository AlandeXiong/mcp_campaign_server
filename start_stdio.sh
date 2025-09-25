#!/bin/bash

# MCP Campaign Server - Stdio Mode Startup Script
# This script starts the MCP server in stdio mode for direct client communication

echo "=========================================="
echo "MCP Campaign Server - Stdio Mode"
echo "=========================================="

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    exit 1
fi

# Clean and compile the project
echo "Building project..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "Build failed. Please check the errors above."
    exit 1
fi

echo "Build successful. Starting MCP Server in Stdio mode..."
echo "=========================================="
echo "MCP Stdio Transport Information:"
echo "  Transport: Stdio (Standard Input/Output)"
echo "  Protocol: MCP 2024-11-05"
echo "  Available Tools: 4 (audience_targeting, campaign_content_recommendation, audience_analysis, campaign_performance_prediction)"
echo "  Authentication: Disabled"
echo "=========================================="
echo "Ready for MCP client connections via stdio."
echo "Press Ctrl+C to stop the server."
echo "=========================================="

# Start the server in stdio mode
mvn exec:java \
    -Dexec.mainClass="com.insurance.mcp.McpStdioApplication" \
    -Dspring.profiles.active=stdio \
    -q