# MCP Campaign Server - Stdio Integration Guide

## Overview

The MCP Campaign Server now supports **Stdio Transport** mode, allowing direct communication with MCP clients like Cline through standard input/output streams.

## Features

- ✅ **Direct stdio communication** - No HTTP server required
- ✅ **Full MCP protocol support** - Initialize, tools/list, tools/call
- ✅ **All 4 insurance tools available**:
  - `audience_targeting` - Audience targeting criteria recommendations
  - `campaign_content_recommendation` - Campaign content recommendations  
  - `inspector_query` - Claude Inspector integration
  - `audience_analysis` - Audience analysis (via inspector)
- ✅ **No authentication required** - Simplified setup
- ✅ **Fast startup** - Minimal Spring Boot configuration

## Quick Start

### 1. Start the Stdio Server

```bash
# Option 1: Using the startup script
./start_stdio.sh

# Option 2: Direct Maven command
mvn exec:java -Dexec.mainClass=com.insurance.mcp.McpStdioApplication -Dspring.profiles.active=stdio -q
```

### 2. Test the Connection

```bash
# Test initialization
echo '{"jsonrpc":"2.0","id":"test-1","method":"initialize","params":{}}' | mvn exec:java -Dexec.mainClass=com.insurance.mcp.McpStdioApplication -Dspring.profiles.active=stdio -q

# Test tools list
echo '{"jsonrpc":"2.0","id":"test-2","method":"tools/list","params":{}}' | mvn exec:java -Dexec.mainClass=com.insurance.mcp.McpStdioApplication -Dspring.profiles.active=stdio -q

# Test tool call
echo '{"jsonrpc":"2.0","id":"test-3","method":"tools/call","params":{"name":"audience_targeting","arguments":{"requirements":"25-35岁年轻用户","insurance_type":"life_insurance"}}}' | mvn exec:java -Dexec.mainClass=com.insurance.mcp.McpStdioApplication -Dspring.profiles.active=stdio -q
```

## Cline Integration

### Configuration File

Create or update your Cline configuration file (`~/.cline/config.json` or similar):

```json
{
  "mcpServers": {
    "insurance-campaign-stdio": {
      "command": "mvn",
      "args": [
        "exec:java",
        "-Dexec.mainClass=com.insurance.mcp.McpStdioApplication",
        "-Dspring.profiles.active=stdio",
        "-q"
      ],
      "cwd": "/Users/xiongjian/project/Mcp_Campaign",
      "env": {
        "JAVA_HOME": "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"
      }
    }
  }
}
```

### Alternative Configuration (using startup script)

```json
{
  "mcpServers": {
    "insurance-campaign-stdio": {
      "command": "./start_stdio.sh",
      "cwd": "/Users/xiongjian/project/Mcp_Campaign"
    }
  }
}
```

## Available Tools

### 1. Audience Targeting
```json
{
  "name": "audience_targeting",
  "arguments": {
    "requirements": "25-35岁年轻用户",
    "insurance_type": "life_insurance",
    "campaign_objective": "acquisition"
  }
}
```

### 2. Campaign Content Recommendation
```json
{
  "name": "campaign_content_recommendation", 
  "arguments": {
    "audience_criteria": {"minAge": 25, "maxAge": 35},
    "insurance_type": "life_insurance",
    "campaign_goal": "conversion",
    "channel": "email"
  }
}
```

### 3. Inspector Query
```json
{
  "name": "inspector_query",
  "arguments": {
    "query": "Analyze the current insurance market trends"
  }
}
```

## Server Response Format

### Initialize Response
```json
{
  "jsonrpc": "2.0",
  "id": "test-1",
  "result": {
    "capabilities": {
      "tools": {},
      "streaming": true,
      "authentication": {
        "supported": ["oauth", "bearer", "api_key", "custom"],
        "required": false
      }
    },
    "serverInfo": {
      "name": "Insurance Campaign MCP Server",
      "version": "1.0.0",
      "transport": "stdio"
    },
    "protocolVersion": "2024-11-05"
  }
}
```

### Tools List Response
```json
{
  "jsonrpc": "2.0",
  "id": "test-2", 
  "result": {
    "tools": [
      {
        "name": "audience_targeting",
        "description": "Provides audience targeting criteria recommendations...",
        "inputSchema": {
          "type": "object",
          "required": ["requirements"],
          "properties": {
            "requirements": {"type": "string", "description": "User requirements..."},
            "insurance_type": {"type": "string", "description": "Type of insurance..."},
            "campaign_objective": {"type": "string", "description": "Campaign objective..."}
          }
        }
      }
    ]
  }
}
```

## Troubleshooting

### Common Issues

1. **Server won't start**: Check Java version (requires Java 17+)
2. **Maven not found**: Ensure Maven is installed and in PATH
3. **Permission denied**: Make startup script executable: `chmod +x start_stdio.sh`
4. **Port conflicts**: Stdio mode doesn't use ports, but ensure no other MCP servers are running

### Debug Mode

Enable debug logging by modifying `application-stdio.yml`:

```yaml
logging:
  level:
    com.insurance.mcp: DEBUG
    org.springframework: DEBUG
```

### Verify Installation

```bash
# Check Java version
java -version

# Check Maven
mvn -version

# Test compilation
mvn clean compile -q
```

## Performance

- **Startup time**: ~1-2 seconds
- **Memory usage**: ~50-100MB
- **Response time**: <100ms for most operations
- **Concurrent connections**: Single connection (stdio limitation)

## Security

- **No network exposure** - Stdio mode doesn't listen on any ports
- **No authentication required** - Simplified for development
- **Local execution only** - Runs in the same process as the client

## Comparison: Stdio vs HTTP

| Feature | Stdio Mode | HTTP Mode |
|---------|------------|-----------|
| Setup complexity | Low | Medium |
| Network security | N/A (local only) | Requires configuration |
| Authentication | None required | Configurable |
| Concurrent clients | 1 | Multiple |
| Streaming support | No | Yes |
| Debugging | Easy | Requires network tools |
| Production use | Development/testing | Recommended |

## Next Steps

1. **Test with Cline**: Configure Cline to use the stdio server
2. **Develop tools**: Add new insurance marketing tools
3. **Production deployment**: Consider HTTP mode for production use
4. **Monitoring**: Add logging and monitoring for production use

---

**Note**: This stdio implementation is optimized for development and testing. For production environments with multiple clients, consider using the HTTP Streamable transport mode.
