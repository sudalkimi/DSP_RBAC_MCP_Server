package com.alkimi.dsp.mcp.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Helper class for MCP protocol operations
 */
public class McpProtocol {
    private final ObjectMapper objectMapper;

    public McpProtocol(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ObjectNode createError(int code, String message) {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("code", code);
        error.put("message", message);
        return error;
    }

    public ObjectNode createStringProperty(String description, boolean required) {
        ObjectNode property = objectMapper.createObjectNode();
        property.put("type", "string");
        property.put("description", description);
        return property;
    }

    public ObjectNode createIntegerProperty(String description, boolean required) {
        ObjectNode property = objectMapper.createObjectNode();
        property.put("type", "integer");
        property.put("description", description);
        return property;
    }

    public ObjectNode createArrayProperty(String description, boolean required) {
        ObjectNode property = objectMapper.createObjectNode();
        property.put("type", "array");
        property.put("description", description);
        return property;
    }

    public ObjectNode createEnumProperty(String description, String[] values, boolean required) {
        ObjectNode property = objectMapper.createObjectNode();
        property.put("type", "string");
        property.put("description", description);
        ArrayNode enumValues = objectMapper.createArrayNode();
        for (String value : values) {
            enumValues.add(value);
        }
        property.set("enum", enumValues);
        return property;
    }

    public ObjectNode createToolResult(String text, ObjectNode metadata) {
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode content = objectMapper.createArrayNode();

        ObjectNode textContent = objectMapper.createObjectNode();
        textContent.put("type", "text");
        textContent.put("text", text);
        content.add(textContent);

        result.set("content", content);
        if (metadata != null) {
            result.set("metadata", metadata);
        }

        return result;
    }

    public ObjectNode createErrorResult(String error) {
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode content = objectMapper.createArrayNode();

        ObjectNode errorContent = objectMapper.createObjectNode();
        errorContent.put("type", "text");
        errorContent.put("text", "Error: " + error);
        content.add(errorContent);

        result.set("content", content);
        result.put("isError", true);

        return result;
    }
}