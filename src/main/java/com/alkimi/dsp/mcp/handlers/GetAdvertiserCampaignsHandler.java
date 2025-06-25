package com.alkimi.dsp.mcp.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.alkimi.dsp.mcp.client.AlkimiDspApiClient;
import com.alkimi.dsp.mcp.protocol.McpProtocol;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Handler for retrieving campaigns for a specific advertiser
 */
@Component
public class GetAdvertiserCampaignsHandler implements ToolHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetAdvertiserCampaignsHandler.class);
    private final AlkimiDspApiClient apiClient;
    private final McpProtocol protocol;
    
    @Autowired
    public GetAdvertiserCampaignsHandler(AlkimiDspApiClient apiClient) {
        this.apiClient = apiClient;
        this.protocol = new McpProtocol(new ObjectMapper());
    }
    
    @Override
    public JsonNode execute(JsonNode arguments, ObjectMapper objectMapper) throws Exception {
        LOGGER.info("Getting advertiser campaigns with arguments: " + arguments);
        
        int advertiserId = arguments.get("advertiserId").asInt();
        int page = arguments.has("page") ? arguments.get("page").asInt() : 1;
        int size = arguments.has("size") ? arguments.get("size").asInt() : 10;
        
        // Call API to get advertiser campaigns
        JsonNode response = apiClient.getAdvertiserCampaigns(advertiserId, page, size);
        
        // Build MCP response
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode content = objectMapper.createArrayNode();
        
        ObjectNode textContent = objectMapper.createObjectNode();
        textContent.put("type", "text");
        
        StringBuilder sb = new StringBuilder();
        
        // Check if response has expected structure
        if (response.has("data") || (response.isArray() && response.size() >= 0)) {
            JsonNode campaigns = response.has("data") ? response.get("data") : response;
            
            sb.append("Campaigns for Advertiser ID: ").append(advertiserId).append("\n\n");
            
            if (campaigns.isArray() && campaigns.size() > 0) {
                int campaignCount = 0;
                String advertiserName = null;
                
                for (JsonNode campaign : campaigns) {
                    // Handle nested structure if present
                    JsonNode campaignData = campaign.has("data") ? campaign.get("data") : campaign;
                    
                    campaignCount++;
                    
                    // Get advertiser name from first campaign
                    if (advertiserName == null && campaignData.has("advertiserName")) {
                        advertiserName = campaignData.get("advertiserName").asText();
                        sb.insert(0, "Advertiser: " + advertiserName + " (ID: " + advertiserId + ")\n");
                    }
                    
                    sb.append("Campaign #").append(campaignCount).append(": ");
                    sb.append(campaignData.get("name").asText()).append("\n");
                    sb.append("- ID: ").append(campaignData.get("id").asText()).append("\n");
                    sb.append("- Status: ").append(campaignData.get("status").asText()).append("\n");
                    
                    if (campaignData.has("totalBudget")) {
                        sb.append("- Total Budget: $").append(campaignData.get("totalBudget").asText());
                        if (campaignData.has("budgetSpent")) {
                            sb.append(" (Spent: $").append(campaignData.get("budgetSpent").asText()).append(")");
                        }
                        sb.append("\n");
                    }
                    
                    sb.append("- Date Range: ").append(campaignData.get("startDate").asText());
                    sb.append(" to ").append(campaignData.get("endDate").asText()).append("\n");
                    
                    if (campaignData.has("impressionsGoal") && !campaignData.get("impressionsGoal").isNull()) {
                        sb.append("- Impressions Goal: ").append(campaignData.get("impressionsGoal").asText());
                        if (campaignData.has("impressionsDelivered")) {
                            sb.append(" (Delivered: ").append(campaignData.get("impressionsDelivered").asText()).append(")");
                        }
                        sb.append("\n");
                    }
                    
                    if (campaignData.has("lineItems") && campaignData.get("lineItems").isArray()) {
                        sb.append("- Line Items: ").append(campaignData.get("lineItems").size()).append("\n");
                    }
                    
                    sb.append("\n");
                }
                
                // Add pagination info if available
                if (response.has("pagination")) {
                    JsonNode pagination = response.get("pagination");
                    sb.append("---\n");
                    sb.append("Page ").append(pagination.get("currentPage").asText());
                    sb.append(" of ").append(pagination.get("totalPages").asText());
                    sb.append(" (Total campaigns: ").append(pagination.get("total").asText()).append(")\n");
                }
                
            } else {
                sb.append("No campaigns found for this advertiser.\n");
            }
            
            textContent.put("text", sb.toString());
            
            // Add metadata
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("advertiserId", advertiserId);
            metadata.put("campaignCount", campaigns.size());
            if (response.has("pagination")) {
                metadata.set("pagination", response.get("pagination"));
            }
            result.set("metadata", metadata);
        } else {
            textContent.put("text", "Unable to retrieve advertiser campaigns. The advertiser may not exist or you may not have access. Response: " + response.toString());
        }
        
        content.add(textContent);
        result.set("content", content);
        
        return result;
    }
}