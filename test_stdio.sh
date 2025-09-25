#!/bin/bash

# Test script for MCP Stdio mode

echo "=== Testing MCP Stdio Mode ==="

# Create a test input file
cat > test_input.json << EOF
{"jsonrpc":"2.0","id":"test-1","method":"initialize","params":{}}
{"jsonrpc":"2.0","id":"test-2","method":"tools/list","params":{}}
EOF

echo "Test input created. Starting server test..."

# Start server in background and capture output
mvn exec:java \
    -Dexec.mainClass=com.insurance.mcp.McpStdioApplication \
    -Dspring.profiles.active=stdio \
    -q < test_input.json > test_output.json 2>&1 &

SERVER_PID=$!

# Wait a bit for server to start
sleep 3

# Check if server is running
if kill -0 $SERVER_PID 2>/dev/null; then
    echo "Server started successfully (PID: $SERVER_PID)"
    
    # Wait a bit more for responses
    sleep 2
    
    # Kill the server
    kill $SERVER_PID 2>/dev/null
    wait $SERVER_PID 2>/dev/null
    
    echo "Server stopped."
    
    # Show output
    if [ -f test_output.json ]; then
        echo "=== Server Output ==="
        cat test_output.json
        echo "=== End Output ==="
    else
        echo "No output file generated."
    fi
else
    echo "Failed to start server."
fi

# Cleanup
rm -f test_input.json test_output.json

echo "Test completed."
