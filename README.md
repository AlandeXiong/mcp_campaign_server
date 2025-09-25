# MCP Campaign Server

A Spring Boot MCP (Model Context Protocol) server that provides insurance marketing campaign management capabilities for Claude AI integration.

## Features

- **Audience Targeting**: AI-powered recommendations for insurance marketing audience segmentation
- **Campaign Content**: Personalized content recommendations for different insurance products and channels
- **Claude Inspector Integration**: Support for Claude Inspector tool queries
- **MCP Protocol**: Full MCP server implementation with WebSocket support
- **REST API**: Additional HTTP endpoints for direct integration

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd Mcp_Campaign
```

2. Build the project:
```bash
# If you have corporate Maven repository issues, use the startup script
./start.sh

# Or manually with custom settings
mvn clean compile --settings temp-settings.xml
```

3. Run the application:
```bash
mvn spring-boot:run
```

The server will start on port 8080 with WebSocket endpoint at `ws://localhost:8080/api/mcp`

### Corporate Repository Issues

If you encounter corporate Maven repository connectivity issues, create a temporary settings file:

```bash
# Create temp-settings.xml with Maven Central only
cat > temp-settings.xml << EOF
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

# Then build with custom settings
mvn clean compile --settings temp-settings.xml
```

## MCP Tools

The server provides the following MCP tools for Claude integration:

### 1. audience_targeting

Provides audience targeting criteria recommendations based on user requirements.

**Input Parameters:**
- `requirements` (required): User requirements for audience targeting
- `insurance_type` (optional): Type of insurance (life, health, auto, property, etc.)
- `campaign_objective` (optional): Campaign objective (acquisition, retention, upselling, etc.)

**Example Usage:**
```json
{
  "name": "audience_targeting",
  "arguments": {
    "requirements": "Target young professionals aged 25-35 who are interested in life insurance",
    "insurance_type": "life_insurance",
    "campaign_objective": "acquisition"
  }
}
```

### 2. campaign_content_recommendation

Recommends personalized campaign content for insurance marketing.

**Input Parameters:**
- `audience_criteria` (required): Target audience criteria object
- `insurance_type` (required): Type of insurance product
- `channel` (optional): Marketing channel (email, sms, social_media, etc.)
- `campaign_goal` (optional): Campaign goal (awareness, conversion, retention, etc.)

**Example Usage:**
```json
{
  "name": "campaign_content_recommendation",
  "arguments": {
    "audience_criteria": {
      "minAge": 25,
      "maxAge": 35,
      "incomeRange": ["middle_income", "high_income"]
    },
    "insurance_type": "life_insurance",
    "channel": "email",
    "campaign_goal": "conversion"
  }
}
```

### 3. inspector_query

Executes queries for Claude Inspector tool integration.

**Input Parameters:**
- `query` (required): Query for Claude Inspector tool

**Example Usage:**
```json
{
  "name": "inspector_query",
  "arguments": {
    "query": "Analyze the effectiveness of recent insurance marketing campaigns"
  }
}
```

## REST API Endpoints

### Health Check
```
GET /api/campaigns/health
```

### List Available Tools
```
GET /api/campaigns/tools
```

### Audience Recommendation
```
POST /api/campaigns/audience/recommend
Content-Type: application/json

{
  "requirements": "Target young professionals for life insurance",
  "insurance_type": "life_insurance",
  "campaign_objective": "acquisition"
}
```

### Content Recommendation
```
POST /api/campaigns/content/recommend
Content-Type: application/json

{
  "audience_criteria": {
    "minAge": 25,
    "maxAge": 35,
    "incomeRange": ["middle_income"]
  },
  "insurance_type": "life_insurance",
  "channel": "email",
  "campaign_goal": "conversion"
}
```

## AI Integration

### Cline (VS Code) Integration

**快速设置**:
```bash
./setup_cline.sh
```

**手动配置**:
1. 在 `~/.cline/config.json` 中添加配置：
```json
{
  "mcpServers": {
    "insurance-campaign": {
      "command": "java",
      "args": ["-jar", "/path/to/mcp-campaign-server-1.0.0.jar"],
      "env": {"SERVER_PORT": "8080"}
    }
  }
}
```

2. 重启VS Code
3. 在Cline中开始对话并使用工具

**使用示例**:
```
请帮我分析一下针对年轻专业人士的人寿保险营销活动的人群定位策略
```

### Claude Desktop Integration

1. Add the MCP server configuration to your Claude Desktop settings:

```json
{
  "mcpServers": {
    "insurance-campaign": {
      "command": "java",
      "args": ["-jar", "path/to/mcp-campaign-server.jar"],
      "env": {
        "SERVER_PORT": "8080"
      }
    }
  }
}
```

### WebSocket Connection

Connect to the MCP server via WebSocket at:
```
ws://localhost:8080/api/mcp
```

## Configuration

The application can be configured via `application.yml`:

```yaml
server:
  port: 8080

mcp:
  server:
    name: "Insurance Campaign MCP Server"
    version: "1.0.0"
    protocol-version: "2024-11-05"
  websocket:
    path: "/mcp"
    allowed-origins: "*"
```

## Development

### Project Structure

```
src/main/java/com/insurance/mcp/
├── McpCampaignServerApplication.java    # Main application class
├── config/
│   └── WebSocketConfig.java             # WebSocket configuration
├── controller/
│   └── CampaignController.java          # REST endpoints
├── handler/
│   └── McpWebSocketHandler.java         # MCP WebSocket handler
├── model/
│   ├── McpRequest.java                  # MCP request model
│   ├── McpResponse.java                 # MCP response model
│   ├── McpTool.java                     # MCP tool model
│   └── campaign/
│       ├── AudienceCriteria.java        # Audience targeting model
│       └── CampaignContent.java         # Campaign content model
└── service/
    ├── McpToolService.java              # MCP tool service
    └── campaign/
        ├── AudienceTargetingService.java # Audience targeting logic
        └── CampaignContentService.java   # Content recommendation logic
```

### Building and Testing

```bash
# Run tests
mvn test

# Build JAR
mvn clean package

# Run with custom profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Insurance Types Supported

- Life Insurance
- Health Insurance
- Auto Insurance
- Property Insurance
- Travel Insurance

## Marketing Channels Supported

- Email
- SMS
- Social Media
- Push Notifications
- Direct Mail

## License

This project is licensed under the MIT License.
# mcp_campaign_server
