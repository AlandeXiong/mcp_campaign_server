# MCP Campaign Server - Build and Deployment Guide

## Overview

The MCP Campaign Server supports multiple deployment modes:
- **Stdio Mode**: Direct stdio communication for development and CLI tools
- **Streamable HTTP Mode**: HTTP-based communication for web clients and production

This guide covers building and deploying both modes.

## Build Options

### 1. Build All JARs

```bash
# Build both stdio and streamable JARs
./build-all.sh

# Or manually build each profile
mvn clean package -P stdio
mvn clean package -P streamable
```

### 2. Build Individual JARs

```bash
# Build only stdio JAR
mvn clean package -P stdio

# Build only streamable JAR  
mvn clean package -P streamable

# Build default JAR (streamable)
mvn clean package
```

## Generated Files

After building, you'll find these files in the `target/` directory:

### JAR Files
- `mcp-campaign-server-1.0.0.jar` - Default JAR (Streamable HTTP)
- `mcp-campaign-server-1.0.0-stdio.jar` - Stdio mode JAR
- `mcp-campaign-server-1.0.0-streamable.jar` - Streamable HTTP mode JAR

### Distribution Packages
- `mcp-campaign-server-1.0.0-stdio.zip` - Complete stdio distribution
- `mcp-campaign-server-1.0.0-streamable.zip` - Complete streamable distribution

## Running the JARs

### Stdio Mode

```bash
# Run stdio JAR
java -jar target/mcp-campaign-server-1.0.0-stdio.jar --spring.profiles.active=stdio

# Or with custom configuration
java -jar target/mcp-campaign-server-1.0.0-stdio.jar \
  --spring.profiles.active=stdio \
  --spring.config.location=classpath:/application-stdio.yml
```

### Streamable HTTP Mode

```bash
# Run streamable JAR on default port 8081
java -jar target/mcp-campaign-server-1.0.0-streamable.jar

# Run on custom port
java -jar target/mcp-campaign-server-1.0.0-streamable.jar --server.port=8082

# With custom configuration
java -jar target/mcp-campaign-server-1.0.0-streamable.jar \
  --server.port=8081 \
  --spring.config.location=classpath:/application.yml
```

### Default JAR (Streamable HTTP)

```bash
# Run default JAR (same as streamable)
java -jar target/mcp-campaign-server-1.0.0.jar --server.port=8081
```

## Distribution Packages

### Extracting Distribution Packages

```bash
# Extract stdio distribution
unzip target/mcp-campaign-server-1.0.0-stdio.zip
cd mcp-campaign-server-1.0.0-stdio

# Extract streamable distribution
unzip target/mcp-campaign-server-1.0.0-streamable.zip
cd mcp-campaign-server-1.0.0-streamable
```

### Distribution Contents

#### Stdio Distribution
```
mcp-campaign-server-1.0.0-stdio/
├── mcp-campaign-server-1.0.0-stdio.jar    # Main JAR
├── config/
│   └── application-stdio.yml              # Configuration
├── docs/
│   ├── STDIO_INTEGRATION_GUIDE.md         # Documentation
│   └── README.md                          # Project README
├── run-stdio.sh                           # Startup script
├── start_stdio.sh                         # Development script
└── cline_config_stdio.json                # Cline configuration
```

#### Streamable Distribution
```
mcp-campaign-server-1.0.0-streamable/
├── mcp-campaign-server-1.0.0-streamable.jar  # Main JAR
├── config/
│   └── application.yml                      # Configuration
├── docs/
│   └── README.md                           # Project README
├── run-streamable.sh                       # Startup script
└── start_streamable_http.sh                # Development script
```

## Configuration

### Stdio Mode Configuration

The stdio mode uses `application-stdio.yml`:

```yaml
spring:
  main:
    web-application-type: none  # Disable web server
  application:
    name: mcp-campaign-server-stdio

mcp:
  server:
    transport: "stdio"
    stdio:
      enabled: true
    streamable-http:
      enabled: false
```

### Streamable HTTP Mode Configuration

The streamable mode uses `application.yml`:

```yaml
server:
  port: 8081

mcp:
  server:
    transport: "streamable-http"
    stdio:
      enabled: false
    streamable-http:
      enabled: true
```

## Deployment Options

### 1. Development Mode

```bash
# Stdio mode for development
./start_stdio.sh

# Streamable HTTP mode for development
./start_streamable_http.sh 8081
```

### 2. Production Mode

```bash
# Extract distribution package
unzip target/mcp-campaign-server-1.0.0-streamable.zip
cd mcp-campaign-server-1.0.0-streamable

# Run with production settings
java -Xms512m -Xmx2g -jar mcp-campaign-server-1.0.0-streamable.jar \
  --server.port=8081 \
  --spring.profiles.active=prod
```

### 3. Docker Deployment

Create a `Dockerfile`:

```dockerfile
FROM openjdk:17-jre-slim

COPY target/mcp-campaign-server-1.0.0-streamable.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app.jar", "--server.port=8081"]
```

Build and run:

```bash
docker build -t mcp-campaign-server .
docker run -p 8081:8081 mcp-campaign-server
```

## Performance Tuning

### JVM Options

```bash
# Stdio mode (minimal memory)
java -Xms128m -Xmx512m -jar mcp-campaign-server-1.0.0-stdio.jar

# Streamable HTTP mode (more memory for concurrent connections)
java -Xms256m -Xmx1g -jar mcp-campaign-server-1.0.0-streamable.jar
```

### Production Settings

```bash
# Production JVM options
java -Xms512m -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -jar mcp-campaign-server-1.0.0-streamable.jar \
  --server.port=8081
```

## Monitoring and Health Checks

### Health Endpoints

```bash
# Check server health (streamable mode only)
curl http://localhost:8081/mcp/v1/health

# Spring Boot actuator endpoints
curl http://localhost:8081/actuator/health
curl http://localhost:8081/actuator/info
```

### Logging

Configure logging in `application.yml`:

```yaml
logging:
  level:
    com.insurance.mcp: INFO
    org.springframework: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  file:
    name: logs/mcp-campaign-server.log
```

## Troubleshooting

### Common Issues

1. **Port already in use**:
   ```bash
   # Check what's using the port
   lsof -i :8081
   
   # Use different port
   java -jar mcp-campaign-server-1.0.0-streamable.jar --server.port=8082
   ```

2. **Out of memory**:
   ```bash
   # Increase heap size
   java -Xms512m -Xmx2g -jar mcp-campaign-server-1.0.0-streamable.jar
   ```

3. **JAR not found**:
   ```bash
   # Ensure you've built the JARs
   ./build-all.sh
   
   # Check target directory
   ls -la target/*.jar
   ```

### Debug Mode

```bash
# Enable debug logging
java -jar mcp-campaign-server-1.0.0-streamable.jar \
  --logging.level.com.insurance.mcp=DEBUG \
  --logging.level.org.springframework=DEBUG
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Build and Test

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Build project
      run: mvn clean package -P stdio,streamable
    
    - name: Test stdio JAR
      run: |
        echo '{"jsonrpc":"2.0","id":"test","method":"initialize","params":{}}' | \
        java -jar target/mcp-campaign-server-1.0.0-stdio.jar --spring.profiles.active=stdio
    
    - name: Test streamable JAR
      run: |
        java -jar target/mcp-campaign-server-1.0.0-streamable.jar --server.port=8081 &
        sleep 10
        curl -f http://localhost:8081/mcp/v1/health
```

## Summary

The MCP Campaign Server provides flexible deployment options:

- **Development**: Use stdio mode for CLI tools like Cline
- **Testing**: Use streamable HTTP mode for integration testing
- **Production**: Use streamable HTTP mode with proper monitoring and scaling

Both modes support the same insurance marketing tools and provide identical functionality, with the choice depending on your deployment requirements.
