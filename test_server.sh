#!/bin/bash

# Simple server test script

echo "=== MCP Campaign Server Test ==="
echo

# Create temp settings for Maven
cat > temp-settings.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <mirrors>
        <mirror>
            <id>central</id>
            <name>Maven Central</name>
            <url>https://repo1.maven.org/maven2</url>
            <mirrorOf>*</mirrorOf>
        </mirror>
    </mirrors>
</settings>
EOF

echo "Testing compilation..."
mvn clean compile --settings temp-settings.xml

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
    echo
    echo "Server is ready to run. You can start it with:"
    echo "mvn spring-boot:run --settings temp-settings.xml"
    echo
    echo "Or use the start script:"
    echo "./start.sh"
else
    echo "❌ Compilation failed"
fi

# Clean up
rm -f temp-settings.xml

echo
echo "=== Cline Configuration ==="
echo
echo "Add this to your ~/.cline/config.json:"
echo
cat << 'EOF'
{
  "mcpServers": {
    "insurance-campaign": {
      "command": "mvn",
      "args": [
        "-f",
        "/Users/xiongjian/project/Mcp_Campaign/pom.xml",
        "spring-boot:run",
        "-Dspring-boot.run.jvmArguments=-Dserver.port=8080"
      ],
      "cwd": "/Users/xiongjian/project/Mcp_Campaign"
    }
  }
}
EOF
echo
echo "Then restart VS Code and start using Cline!"
