package com.alkimi.dsp.mcp.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.alkimi.dsp.mcp.client.AlkimiDspApiClient;
import com.alkimi.dsp.mcp.protocol.McpProtocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Handler for retrieving campaign details by ID
 */
public class GetCampaignHandler implements ToolHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetCampaignHandler.class);
    private final AlkimiDspApiClient apiClient;
    private final McpProtocol protocol;
    private final ObjectMapper objectMapper;

    public GetCampaignHandler() {
        this.apiClient = new AlkimiDspApiClient();
        this.objectMapper = new ObjectMapper();
        this.protocol = new McpProtocol(objectMapper);
    }

    @Override
    public JsonNode execute(JsonNode arguments, ObjectMapper objectMapper) throws Exception {
        LOGGER.info("Getting campaign with arguments: " + arguments);

        int campaignId = arguments.get("campaignId").asInt();

        // Call API to get campaign
        JsonNode response = apiClient.getCampaign(campaignId);

        // Build MCP response
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode content = objectMapper.createArrayNode();

        ObjectNode textContent = objectMapper.createObjectNode();
        textContent.put("type", "text");

        if (response.has("data")) {
            JsonNode campaignData = response.get("data");

            StringBuilder sb = new StringBuilder();
            sb.append("Campaign Details:\n\n");
            sb.append("ID: ").append(campaignData.get("id").asText()).append("\n");
            sb.append("Name: ").append(campaignData.get("name").asText()).append("\n");
            sb.append("Status: ").append(campaignData.get("status").asText()).append("\n");
            sb.append("Advertiser: ").append(campaignData.get("advertiserName").asText())
                    .append(" (ID: ").append(campaignData.get("advertiserId").asText()).append(")\n");
            sb.append("Media Agency: ").append(campaignData.get("mediaAgencyName").asText())
                    .append(" (ID: ").append(campaignData.get("mediaAgencyId").asText()).append(")\n\n");

            sb.append("Schedule:\n");
            sb.append("- Start Date: ").append(campaignData.get("startDate").asText()).append("\n");
            sb.append("- End Date: ").append(campaignData.get("endDate").asText()).append("\n");
            sb.append("- Timezone: ").append(campaignData.get("timezone").asText()).append("\n\n");

            sb.append("Budget & Performance:\n");
            sb.append("- Total Budget: $").append(campaignData.get("totalBudget").asText()).append("\n");
            sb.append("- Budget Spent: $").append(campaignData.get("budgetSpent").asText()).append("\n");
            sb.append("- Impressions Goal: ").append(campaignData.get("impressionsGoal").asText()).append("\n");
            sb.append("- Impressions Delivered: ").append(campaignData.get("impressionsDelivered").asText()).append("\n\n");

            sb.append("Bidding:\n");
            sb.append("- Strategy: ").append(campaignData.get("biddingStrategy").asText()).append("\n");
            sb.append("- Floor Price: $").append(campaignData.get("floorPrice").asText()).append("\n");
            sb.append("- Max Bid Price: $").append(campaignData.get("maxBidPrice").asText()).append("\n");
            sb.append("- Pacing: ").append(campaignData.get("pacing").asText()).append("\n\n");

            // Line items summary
            if (campaignData.has("lineItems") && campaignData.get("lineItems").isArray()) {
                JsonNode lineItems = campaignData.get("lineItems");
                sb.append("Line Items (").append(lineItems.size()).append(" total):\n");
                for (JsonNode lineItem : lineItems) {
                    sb.append("- ").append(lineItem.get("lineItemName").asText());
                    sb.append(" (ID: ").append(lineItem.get("id").asText()).append(")");
                    sb.append(" - Status: ").append(lineItem.get("lineItemStatus").asText());
                    sb.append(" - Budget: $").append(lineItem.get("budget").asText());
                    sb.append("\n");
                }
            }

            textContent.put("text", sb.toString());

            // Add metadata
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("campaignId", campaignData.get("id").asText());
            metadata.put("status", campaignData.get("status").asText());
            metadata.set("fullResponse", campaignData);
            result.set("metadata", metadata);
        } else {
            textContent.put("text", "Campaign not found or unexpected response format: " + response.toString());
        }

        content.add(textContent);
        result.set("content", content);

        return result;
    }
}