package com.alkimi.dsp.mcp.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.alkimi.dsp.mcp.client.AlkimiDspApiClient;

import java.util.*;
import java.util.logging.*;

/**
 * Handler for filtering campaigns by various criteria
 */
public class FilterCampaignsHandler implements ToolHandler {
    private static final Logger LOGGER = Logger.getLogger(FilterCampaignsHandler.class.getName());
    private final AlkimiDspApiClient apiClient;
    
    public FilterCampaignsHandler() {
        this.apiClient = new AlkimiDspApiClient();
    }
    
    @Override
    public JsonNode execute(JsonNode arguments, ObjectMapper objectMapper) throws Exception {
        LOGGER.info("Filtering campaigns with arguments: " + arguments);
        
        // Build filter parameters
        Map<String, String> filters = new HashMap<>();
        
        if (arguments.has("status")) {
            filters.put("status", arguments.get("status").asText());
        }
        
        if (arguments.has("advertiserIds")) {
            List<String> ids = new ArrayList<>();
            arguments.get("advertiserIds").forEach(node -> ids.add(node.asText()));
            filters.put("advertiserIds", String.join(",", ids));
        }
        
        if (arguments.has("mediaAgencyIds")) {
            List<String> ids = new ArrayList<>();
            arguments.get("mediaAgencyIds").forEach(node -> ids.add(node.asText()));
            filters.put("mediaAgencyIds", String.join(",", ids));
        }
        
        if (arguments.has("holdingCompanyIds")) {
            List<String> ids = new ArrayList<>();
            arguments.get("holdingCompanyIds").forEach(node -> ids.add(node.asText()));
            filters.put("holdingCompanyIds", String.join(",", ids));
        }
        
        if (arguments.has("search")) {
            filters.put("search", arguments.get("search").asText());
        }
        
        if (arguments.has("sortBy")) {
            filters.put("sortBy", arguments.get("sortBy").asText());
        }
        
        if (arguments.has("orderBy")) {
            filters.put("orderBy", arguments.get("orderBy").asText());
        }
        
        if (arguments.has("page")) {
            filters.put("page", arguments.get("page").asText());
        }
        
        if (arguments.has("size")) {
            filters.put("size", arguments.get("size").asText());
        }
        
        // Call API to filter campaigns
        JsonNode response = apiClient.filterCampaigns(filters);
        
        // Build MCP response
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode content = objectMapper.createArrayNode();
        
        ObjectNode textContent = objectMapper.createObjectNode();
        textContent.put("type", "text");
        
        StringBuilder sb = new StringBuilder();
        
        if (response.has("data") && response.has("pagination")) {
            JsonNode campaigns = response.get("data");
            JsonNode pagination = response.get("pagination");
            
            // Display active filters
            sb.append("Filtered Campaigns");
            if (!filters.isEmpty()) {
                sb.append(" (");
                List<String> filterStrings = new ArrayList<>();
                filters.forEach((key, value) -> {
                    if (!key.equals("page") && !key.equals("size")) {
                        filterStrings.add(key + "=" + value);
                    }
                });
                sb.append(String.join(", ", filterStrings));
                sb.append(")");
            }
            sb.append("\n");
            
            sb.append("Page ").append(pagination.get("currentPage").asText());
            sb.append(" of ").append(pagination.get("totalPages").asText());
            sb.append(" (Total: ").append(pagination.get("total").asText()).append(")\n\n");
            
            if (campaigns.isArray() && campaigns.size() > 0) {
                for (JsonNode campaignWrapper : campaigns) {
                    JsonNode campaign = campaignWrapper.get("data");
                    
                    sb.append("- Status: ").append(campaign.get("campaignStatus").asText()).append("\n");
                    sb.append("- Advertiser: ").append(campaign.get("advertiserName").asText())
                      .append(" (ID: ").append(campaign.get("advertiserId").asText()).append(")\n");
                    
                    // Budget info
                    sb.append("- Budget: $").append(campaign.get("totalBudget").asText());
                    int budgetSpentPercentage = campaign.get("budgetSpentInPercentage").asInt();
                    sb.append(" (").append(budgetSpentPercentage).append("% spent)\n");
                    
                    // Date range
                    sb.append("- Date Range: ").append(campaign.get("startDate").asText());
                    sb.append(" to ").append(campaign.get("endDate").asText()).append("\n");
                    
                    // Goals and performance
                    if (campaign.has("impressionsGoal") && !campaign.get("impressionsGoal").isNull()) {
                        sb.append("- Impressions Goal: ").append(campaign.get("impressionsGoal").asText());
                        sb.append(" (Delivered: ").append(campaign.get("goalsDelivered").asText()).append(")\n");
                    }
                    
                    // Pacing status
                    if (campaign.has("pacingColorCode")) {
                        String pacingColor = campaign.get("pacingColorCode").asText();
                        sb.append("- Pacing Status: ").append(pacingColor);
                        if ("RED".equals(pacingColor)) {
                            sb.append(" (Under-pacing)");
                        } else if ("YELLOW".equals(pacingColor)) {
                            sb.append(" (Slightly under-pacing)");
                        } else if ("GREEN".equals(pacingColor)) {
                            sb.append(" (On track)");
                        }
                        sb.append("\n");
                    }
                    
                    sb.append("\n");
                }
                
                // Pagination info
                if (pagination.get("totalPages").asInt() > 1) {
                    sb.append("---\n");
                    int currentPage = pagination.get("currentPage").asInt();
                    if (currentPage < pagination.get("totalPages").asInt()) {
                        sb.append("Next page: page=").append(currentPage + 1).append("\n");
                    }
                    if (currentPage > 1) {
                        sb.append("Previous page: page=").append(currentPage - 1).append("\n");
                    }
                }
            } else {
                sb.append("No campaigns found matching the filter criteria.\n");
            }
            
            textContent.put("text", sb.toString());
            
            // Add metadata
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.set("pagination", pagination);
            metadata.put("campaignCount", campaigns.size());
            metadata.set("filters", objectMapper.valueToTree(filters));
            result.set("metadata", metadata);
        } else {
            textContent.put("text", "Unable to filter campaigns. Unexpected response format: " + response.toString());
        }
        
        content.add(textContent);
        result.set("content", content);
        
        return result;
    }
}