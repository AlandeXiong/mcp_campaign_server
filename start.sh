#!/bin/bash

# MCP Campaign Server Startup Script

echo "Starting MCP Campaign Server..."

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

echo "Building the application..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo "Build successful. Starting the server..."
    mvn spring-boot:run
else
    echo "Build failed. Please check the error messages above."
    exit 1
fi
