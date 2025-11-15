# MCP Server Adapter

## Status: 🚧 In Progress (Phase 2a)

Model Context Protocol (MCP) Server implementation for Responsible Vibe Engineering.

## SDK Integration

### Dependency
```xml
<dependency>
    <groupId>io.modelcontextprotocol</groupId>
    <artifactId>kotlin-sdk-jvm</artifactId>
    <version>0.7.6</version>
</dependency>
```

**Source**: [modelcontextprotocol/kotlin-sdk](https://github.com/modelcontextprotocol/kotlin-sdk)

### Next Steps

1. **Study SDK API**
   - Review official samples: https://github.com/modelcontextprotocol/kotlin-sdk/tree/main/samples
   - Understand correct API usage for:
     - `ServerCapabilities.Tools()` constructor
     - `ServerCapabilities.Resources()` constructor
     - `StdioServerTransport` with Okio `Source`/`Sink`
     - `Tool` definition with proper `inputSchema` and return types

2. **Implement 5 MCP Tools**
   - `list_processes` - List available engineering processes ✅ (started)
   - `start_process` - Start a new process execution
   - `execute_phase` - Execute a process phase with Koog workflow
   - `complete_phase` - Complete phase and move to next
   - `get_context` - Retrieve execution context for a project

3. **Implement 2 MCP Resources**
   - `context://<project>/<branch>` - ExecutionContext as resource
   - `process://<process-id>` - Process definition as resource

4. **Integration with Domain Services**
   - Tools call existing Domain Services (StartProcessExecutionService, etc.)
   - Map Domain Model to MCP responses (JSON-serializable)
   - Handle errors gracefully

5. **Testing**
   - Unit tests for each tool
   - Integration test with stdio transport
   - Test with Claude Desktop or MCP Inspector

## Architecture

```
ResponsibleVibeMcpServer (Entry Point)
    ↓ uses
Server (from kotlin-sdk)
    ↓ exposes
Tools & Resources
    ↓ call
Domain Services
    ↓ use
Domain Model
```

## References

- MCP Specification: https://modelcontextprotocol.io
- Kotlin SDK: https://github.com/modelcontextprotocol/kotlin-sdk
- Kotlin SDK Samples: https://github.com/modelcontextprotocol/kotlin-sdk/tree/main/samples
