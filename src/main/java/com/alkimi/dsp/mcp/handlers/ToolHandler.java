package com.alkimi.dsp.mcp.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Interface for MCP tool handlers
 */
public interface ToolHandler {
    /**
     * Execute the tool with the given arguments
     * 
     * @param arguments The tool arguments as JSON
     * @param objectMapper The ObjectMapper for JSON processing
     * @return The result of the tool execution
     * @throws Exception if execution fails
     */
    JsonNode execute(JsonNode arguments, ObjectMapper objectMapper) throws Exception;
}