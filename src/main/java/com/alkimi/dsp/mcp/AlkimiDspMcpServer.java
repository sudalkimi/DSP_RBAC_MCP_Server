package com.alkimi.dsp.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.alkimi.dsp.mcp.handlers.*;
import com.alkimi.dsp.mcp.models.*;
import com.alkimi.dsp.mcp.protocol.*;
import com.alkimi.dsp.mcp.config.AlkimiDspConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * MCP Server implementation for Alkimi DSP
 * Implements the Model Context Protocol for campaign management
 */
public class AlkimiDspMcpServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlkimiDspMcpServer.class);
    private static final String PROTOCOL_VERSION = "0.1.0";

    private final ObjectMapper objectMapper;
    private final BufferedReader input;
    private final PrintWriter output;
    private final Map<String, ToolHandler> toolHandlers;
    private final ExecutorService executor;
    private final McpProtocol protocol;
    private final AlkimiDspConfig config;

    public AlkimiDspMcpServer() {
        this.config = AlkimiDspConfig.getInstance();
        this.objectMapper = new ObjectMapper();
        this.input = new BufferedReader(new InputStreamReader(System.in));
        this.output = new PrintWriter(System.out, true);
        this.toolHandlers = new HashMap<>();
        this.executor = Executors.newFixedThreadPool(config.getServer().getThreadPoolSize());
        this.protocol = new McpProtocol(objectMapper);

        // Initialize tool handlers
        initializeToolHandlers();
    }

    private void initializeToolHandlers() {
        // Campaign management tools
        toolHandlers.put("create_campaign", new CreateCampaignHandler());
        toolHandlers.put("get_campaign", new GetCampaignHandler());
        toolHandlers.put("list_campaigns", new ListCampaignsHandler());
        toolHandlers.put("filter_campaigns", new FilterCampaignsHandler());
        toolHandlers.put("get_advertiser_campaigns", new GetAdvertiserCampaignsHandler());
    }

    public void start() {
        LOGGER.info("Starting {} v{}", config.getServer().getName(), config.getServer().getVersion());
        LOGGER.info("API URL: {}", config.getApiUrl());
        LOGGER.info("Thread pool size: {}", config.getServer().getThreadPoolSize());

        try {
            while (true) {
                String line = input.readLine();
                if (line == null) {
                    break;
                }

                try {
                    JsonNode request = objectMapper.readTree(line);
                    CompletableFuture.supplyAsync(() -> handleRequest(request), executor)
                            .thenAccept(response -> {
                                try {
                                    output.println(objectMapper.writeValueAsString(response));
                                } catch (Exception e) {
                                    LOGGER.error("Error sending response", e);
                                }
                            });
                } catch (Exception e) {
                    LOGGER.error("Error processing request", e);
                    sendError(null, -32700, "Parse error");
                }
            }
        } catch (IOException e) {
            LOGGER.error("IO error", e);
        } finally {
            executor.shutdown();
        }
    }

    private JsonNode handleRequest(JsonNode request) {
        String method = request.get("method").asText();
        JsonNode id = request.get("id");
        JsonNode params = request.get("params");

        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        if (id != null) {
            response.set("id", id);
        }

        try {
            switch (method) {
                case "initialize":
                    response.set("result", handleInitialize(params));
                    break;

                case "tools/list":
                    response.set("result", handleToolsList());
                    break;

                case "tools/call":
                    response.set("result", handleToolCall(params));
                    break;

                default:
                    response.set("error", protocol.createError(-32601, "Method not found"));
            }
        } catch (Exception e) {
            LOGGER.error("Error handling method: " + method, e);
            response.set("error", protocol.createError(-32603, "Internal error: " + e.getMessage()));
        }

        return response;
    }

    private JsonNode handleInitialize(JsonNode params) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("protocolVersion", PROTOCOL_VERSION);

        ObjectNode capabilities = objectMapper.createObjectNode();
        ObjectNode tools = objectMapper.createObjectNode();
        tools.put("list", true);
        tools.put("call", true);
        capabilities.set("tools", tools);

        result.set("capabilities", capabilities);

        ObjectNode serverInfo = objectMapper.createObjectNode();
        serverInfo.put("name", config.getServer().getName());
        serverInfo.put("version", config.getServer().getVersion());
        result.set("serverInfo", serverInfo);

        return result;
    }

    private JsonNode handleToolsList() {
        ArrayNode tools = objectMapper.createArrayNode();

        // Create Campaign Tool
        tools.add(createToolDefinition(
                "create_campaign",
                "Create a new advertising campaign",
                createCampaignSchema()
        ));

        // Get Campaign Tool
        tools.add(createToolDefinition(
                "get_campaign",
                "Get campaign details by ID",
                getCampaignSchema()
        ));

        // List Campaigns Tool
        tools.add(createToolDefinition(
                "list_campaigns",
                "List all campaigns with pagination",
                listCampaignsSchema()
        ));

        // Filter Campaigns Tool
        tools.add(createToolDefinition(
                "filter_campaigns",
                "Filter campaigns by various criteria",
                filterCampaignsSchema()
        ));

        // Get Advertiser Campaigns Tool
        tools.add(createToolDefinition(
                "get_advertiser_campaigns",
                "Get campaigns for a specific advertiser",
                getAdvertiserCampaignsSchema()
        ));

        ObjectNode result = objectMapper.createObjectNode();
        result.set("tools", tools);

        return result;
    }

    private ObjectNode createToolDefinition(String name, String description, ObjectNode schema) {
        ObjectNode tool = objectMapper.createObjectNode();
        tool.put("name", name);
        tool.put("description", description);
        tool.set("inputSchema", schema);
        return tool;
    }

    private ObjectNode createCampaignSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("name", protocol.createStringProperty("Campaign name", true));
        properties.set("advertiserId", protocol.createIntegerProperty("Advertiser ID", true));
        properties.set("timezone", protocol.createStringProperty("Timezone", true));
        properties.set("startDate", protocol.createStringProperty("Start date (YYYY-MM-DD)", true));
        properties.set("endDate", protocol.createStringProperty("End date (YYYY-MM-DD)", true));
        properties.set("totalBudget", protocol.createIntegerProperty("Total budget", true));
        properties.set("pacing", protocol.createEnumProperty("Pacing strategy", new String[]{"Even", "ASAP"}, true));
        properties.set("biddingStrategy", protocol.createStringProperty("Bidding strategy", true));
        properties.set("floorPrice", protocol.createIntegerProperty("Floor price", true));
        properties.set("maxBidPrice", protocol.createIntegerProperty("Max bid price", true));
        properties.set("lineItems", protocol.createArrayProperty("Line items", true));

        // Optional fields
        properties.set("sspIds", protocol.createArrayProperty("SSP IDs", false));
        properties.set("iabCategory", protocol.createArrayProperty("IAB categories", false));
        properties.set("languages", protocol.createArrayProperty("Languages", false));
        properties.set("countries", protocol.createArrayProperty("Countries", false));
        properties.set("devices", protocol.createArrayProperty("Devices", false));
        properties.set("browsers", protocol.createArrayProperty("Browsers", false));
        properties.set("platforms", protocol.createArrayProperty("Platforms", false));
        properties.set("businessGoals", protocol.createStringProperty("Business goals", false));
        properties.set("product", protocol.createStringProperty("Product", false));
        properties.set("impressionsGoal", protocol.createIntegerProperty("Impressions goal", false));
        properties.set("description", protocol.createStringProperty("Description", false));

        schema.set("properties", properties);

        ArrayNode required = objectMapper.createArrayNode();
        required.add("name").add("advertiserId").add("timezone").add("startDate")
                .add("endDate").add("totalBudget").add("pacing").add("biddingStrategy")
                .add("floorPrice").add("maxBidPrice").add("lineItems");
        schema.set("required", required);

        return schema;
    }

    private ObjectNode getCampaignSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("campaignId", protocol.createIntegerProperty("Campaign ID", true));

        schema.set("properties", properties);

        ArrayNode required = objectMapper.createArrayNode();
        required.add("campaignId");
        schema.set("required", required);

        return schema;
    }

    private ObjectNode listCampaignsSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("page", protocol.createIntegerProperty("Page number", false));
        properties.set("size", protocol.createIntegerProperty("Page size", false));

        schema.set("properties", properties);

        return schema;
    }

    private ObjectNode filterCampaignsSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("status", protocol.createEnumProperty("Campaign status",
                new String[]{"ANY", "ACTIVE", "INACTIVE", "COMPLETED", "CREATED", "DRAFT"}, false));
        properties.set("advertiserIds", protocol.createArrayProperty("Advertiser IDs", false));
        properties.set("mediaAgencyIds", protocol.createArrayProperty("Media Agency IDs", false));
        properties.set("search", protocol.createStringProperty("Search term (name or ID)", false));
        properties.set("sortBy", protocol.createEnumProperty("Sort field",
                new String[]{"id", "name", "modified"}, false));
        properties.set("orderBy", protocol.createEnumProperty("Sort order",
                new String[]{"asc", "desc"}, false));
        properties.set("page", protocol.createIntegerProperty("Page number", false));
        properties.set("size", protocol.createIntegerProperty("Page size", false));

        schema.set("properties", properties);

        return schema;
    }

    private ObjectNode getAdvertiserCampaignsSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("advertiserId", protocol.createIntegerProperty("Advertiser ID", true));
        properties.set("page", protocol.createIntegerProperty("Page number", false));
        properties.set("size", protocol.createIntegerProperty("Page size", false));

        schema.set("properties", properties);

        ArrayNode required = objectMapper.createArrayNode();
        required.add("advertiserId");
        schema.set("required", required);

        return schema;
    }

    private JsonNode handleToolCall(JsonNode params) throws Exception {
        String toolName = params.get("name").asText();
        JsonNode arguments = params.get("arguments");

        ToolHandler handler = toolHandlers.get(toolName);
        if (handler == null) {
            throw new IllegalArgumentException("Unknown tool: " + toolName);
        }

        return handler.execute(arguments, objectMapper);
    }

    private void sendError(JsonNode id, int code, String message) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        if (id != null) {
            response.set("id", id);
        }
        response.set("error", protocol.createError(code, message));
        output.println(response.toString());
    }

    public static void main(String[] args) {
        AlkimiDspMcpServer server = new AlkimiDspMcpServer();
        server.start();
    }
}