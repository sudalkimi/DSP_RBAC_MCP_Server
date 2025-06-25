package com.alkimi.dsp.mcp.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.alkimi.dsp.mcp.client.AlkimiDspApiClient;

import java.util.logging.*;

/**
 * Handler for listing all campaigns with pagination
 */
public class ListCampaignsHandler implements ToolHandler {
    private static final Logger LOGGER = Logger.getLogger(ListCampaignsHandler.class.getName());
    private final AlkimiDspApiClient apiClient;
    
    public ListCampaignsHandler() {
        this.apiClient = new AlkimiDspApiClient();
    }
    
    @Override
    public JsonNode execute(JsonNode arguments, ObjectMapper objectMapper) throws Exception {
        LOGGER.info("Listing campaigns with arguments: " + arguments);
        
        int page = arguments.has("page") ? arguments.get("page").asInt() : 1;
        int size = arguments.has("size") ? arguments.get("size").asInt() : 10;
        
        // Call API to list campaigns
        JsonNode response = apiClient.listCampaigns(page, size);
        
        // Build MCP response
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode content = objectMapper.createArrayNode();
        
        ObjectNode textContent = objectMapper.createObjectNode();
        textContent.put("type", "text");
        
        StringBuilder sb = new StringBuilder();
        
        if (response.has("data") && response.has("pagination")) {
            JsonNode campaigns = response.get("data");
            JsonNode pagination = response.get("pagination");
            
            sb.append("Campaigns (Page ").append(pagination.get("currentPage").asText());
            sb.append(" of ").append(pagination.get("totalPages").asText());
            sb.append(", Total: ").append(pagination.get("total").asText()).append(")\n\n");
            
            if (campaigns.isArray() && campaigns.size() > 0) {
                for (JsonNode campaignWrapper : campaigns) {
                    JsonNode campaign = campaignWrapper.get("data");
                    
                    sb.append("Campaign: ").append(campaign.get("name").asText()).append("\n");
                    sb.append("- ID: ").append(campaign.get("id").asText()).append("\n");
                    sb.append("- Status: ").append(campaign.get("status").asText()).append("\n");
                    sb.append("- Advertiser: ").append(campaign.get("advertiserName").asText()).append("\n");
                    sb.append("- Budget: $").append(campaign.get("totalBudget").asText());
                    
                    if (campaign.has("budgetSpent")) {
                        sb.append(" (Spent: $").append(campaign.get("budgetSpent").asText()).append(")");
                    }
                    sb.append("\n");
                    
                    sb.append("- Date Range: ").append(campaign.get("startDate").asText());
                    sb.append(" to ").append(campaign.get("endDate").asText()).append("\n");
                    
                    if (campaign.has("impressionsGoal") && !campaign.get("impressionsGoal").isNull()) {
                        sb.append("- Impressions Goal: ").append(campaign.get("impressionsGoal").asText());
                        if (campaign.has("impressionsDelivered")) {
                            sb.append(" (Delivered: ").append(campaign.get("impressionsDelivered").asText()).append(")");
                        }
                        sb.append("\n");
                    }
                    
                    sb.append("\n");
                }
                
                // Pagination info
                if (pagination.get("totalPages").asInt() > 1) {
                    sb.append("---\n");
                    sb.append("Use page parameter to navigate: page=").append(page + 1);
                    sb.append(" for next page\n");
                }
            } else {
                sb.append("No campaigns found.\n");
            }
            
            textContent.put("text", sb.toString());
            
            // Add metadata
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.set("pagination", pagination);
            metadata.put("campaignCount", campaigns.size());
            result.set("metadata", metadata);
        } else {
            textContent.put("text", "Unable to retrieve campaigns. Unexpected response format: " + response.toString());
        }
        
        content.add(textContent);
        result.set("content", content);
        
        return result;
    }
}