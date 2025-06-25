package com.alkimi.dsp.mcp.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.alkimi.dsp.mcp.client.AlkimiDspApiClient;
import com.alkimi.dsp.mcp.models.*;
import com.alkimi.dsp.mcp.protocol.McpProtocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Handler for creating campaigns in Alkimi DSP
 */
public class CreateCampaignHandler implements ToolHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateCampaignHandler.class);
    private final AlkimiDspApiClient apiClient;
    private final McpProtocol protocol;

    public CreateCampaignHandler() {
        this.apiClient = new AlkimiDspApiClient();
        this.protocol = new McpProtocol(new ObjectMapper());
    }

    @Override
    public JsonNode execute(JsonNode arguments, ObjectMapper objectMapper) throws Exception {
        LOGGER.info("Creating campaign with arguments: " + arguments);

        // Build campaign request from arguments
        CampaignRequest request = buildCampaignRequest(arguments, objectMapper);

        // Call API to create campaign
        JsonNode response = apiClient.createCampaign(request);

        // Build MCP response
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode content = objectMapper.createArrayNode();

        ObjectNode textContent = objectMapper.createObjectNode();
        textContent.put("type", "text");

        if (response.has("data")) {
            JsonNode campaignData = response.get("data");
            String campaignId = campaignData.get("id").asText();
            String campaignName = campaignData.get("name").asText();
            String status = campaignData.get("status").asText();

            textContent.put("text", String.format(
                    "Campaign created successfully!\n\n" +
                            "Campaign ID: %s\n" +
                            "Name: %s\n" +
                            "Status: %s\n" +
                            "Start Date: %s\n" +
                            "End Date: %s\n" +
                            "Total Budget: $%s",
                    campaignId,
                    campaignName,
                    status,
                    campaignData.get("startDate").asText(),
                    campaignData.get("endDate").asText(),
                    campaignData.get("totalBudget").asText()
            ));

            // Add metadata
            ObjectNode metadata = objectMapper.createObjectNode();
            metadata.put("campaignId", campaignId);
            metadata.put("status", status);
            metadata.set("fullResponse", campaignData);
            result.set("metadata", metadata);
        } else {
            textContent.put("text", "Campaign created, but response format was unexpected: " + response.toString());
        }

        content.add(textContent);
        result.set("content", content);

        return result;
    }

    private CampaignRequest buildCampaignRequest(JsonNode arguments, ObjectMapper objectMapper) {
        CampaignRequest request = new CampaignRequest();

        // Required fields
        request.setName(arguments.get("name").asText());
        request.setAdvertiserId(arguments.get("advertiserId").asInt());
        request.setTimezone(arguments.get("timezone").asText());
        request.setStartDate(arguments.get("startDate").asText());
        request.setEndDate(arguments.get("endDate").asText());
        request.setTotalBudget(arguments.get("totalBudget").asInt());
        request.setPacing(arguments.get("pacing").asText());
        request.setBiddingStrategy(arguments.get("biddingStrategy").asText());
        request.setFloorPrice(arguments.get("floorPrice").asInt());
        request.setMaxBidPrice(arguments.get("maxBidPrice").asInt());

        // Optional fields
        if (arguments.has("sspIds")) {
            List<Integer> sspIds = new ArrayList<>();
            arguments.get("sspIds").forEach(node -> sspIds.add(node.asInt()));
            request.setSspIds(sspIds);
        }

        if (arguments.has("iabCategory")) {
            List<Map<String, Integer>> categories = new ArrayList<>();
            arguments.get("iabCategory").forEach(node -> {
                Map<String, Integer> category = new HashMap<>();
                category.put("id", node.get("id").asInt());
                categories.add(category);
            });
            request.setIabCategory(categories);
        }

        if (arguments.has("languages")) {
            List<String> languages = new ArrayList<>();
            arguments.get("languages").forEach(node -> languages.add(node.asText()));
            request.setLanguages(languages);
        }

        if (arguments.has("countries")) {
            List<String> countries = new ArrayList<>();
            arguments.get("countries").forEach(node -> countries.add(node.asText()));
            request.setCountries(countries);
        }

        if (arguments.has("devices")) {
            List<String> devices = new ArrayList<>();
            arguments.get("devices").forEach(node -> devices.add(node.asText()));
            request.setDevices(devices);
        }

        if (arguments.has("browsers")) {
            List<String> browsers = new ArrayList<>();
            arguments.get("browsers").forEach(node -> browsers.add(node.asText()));
            request.setBrowsers(browsers);
        }

        if (arguments.has("platforms")) {
            List<String> platforms = new ArrayList<>();
            arguments.get("platforms").forEach(node -> platforms.add(node.asText()));
            request.setPlatforms(platforms);
        }

        // Line items
        if (arguments.has("lineItems")) {
            List<LineItemRequest> lineItems = new ArrayList<>();
            arguments.get("lineItems").forEach(node -> {
                LineItemRequest lineItem = buildLineItemRequest(node);
                lineItems.add(lineItem);
            });
            request.setLineItems(lineItems);
        }

        if (arguments.has("businessGoals")) {
            request.setBusinessGoals(arguments.get("businessGoals").asText());
        }

        if (arguments.has("product")) {
            request.setProduct(arguments.get("product").asText());
        }

        if (arguments.has("impressionsGoal")) {
            request.setImpressionsGoal(arguments.get("impressionsGoal").asInt());
        }

        if (arguments.has("description")) {
            request.setDescription(arguments.get("description").asText());
        }

        return request;
    }

    private LineItemRequest buildLineItemRequest(JsonNode node) {
        LineItemRequest lineItem = new LineItemRequest();

        lineItem.setId(node.get("id").asInt());
        lineItem.setLineItemName(node.get("lineItemName").asText());

        List<String> channels = new ArrayList<>();
        node.get("channel").forEach(ch -> channels.add(ch.asText()));
        lineItem.setChannel(channels);

        List<Integer> creativeIds = new ArrayList<>();
        node.get("creativeIds").forEach(id -> creativeIds.add(id.asInt()));
        lineItem.setCreativeIds(creativeIds);

        lineItem.setLineItemType(node.get("lineItemType").asText());
        lineItem.setPacing(node.get("pacing").asText());

        // Optional line item fields
        if (node.has("description")) {
            lineItem.setDescription(node.get("description").asText());
        }

        if (node.has("budget")) {
            lineItem.setBudget(node.get("budget").asInt());
        }

        if (node.has("startDate")) {
            lineItem.setStartDate(node.get("startDate").asText());
        }

        if (node.has("endDate")) {
            lineItem.setEndDate(node.get("endDate").asText());
        }

        if (node.has("biddingStrategy")) {
            lineItem.setBiddingStrategy(node.get("biddingStrategy").asText());
        }

        if (node.has("floorPrice")) {
            lineItem.setFloorPrice(node.get("floorPrice").asInt());
        }

        if (node.has("maxBidPrice")) {
            lineItem.setMaxBidPrice(node.get("maxBidPrice").asInt());
        }

        return lineItem;
    }
}