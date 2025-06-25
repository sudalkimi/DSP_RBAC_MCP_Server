package com.alkimi.dsp.mcp.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.alkimi.dsp.mcp.models.*;
import com.alkimi.dsp.mcp.config.AlkimiDspConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.logging.*;

/**
 * HTTP client for interacting with Alkimi DSP API
 */
@Component
public class AlkimiDspApiClient {
    private static final Logger LOGGER = Logger.getLogger(AlkimiDspApiClient.class.getName());
    
    private final String baseUrl;
    private final String authToken;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public AlkimiDspApiClient(AlkimiDspConfig config) {
        this.baseUrl = config.getApiUrl();
        this.authToken = config.getAuthToken();
        
        if (this.baseUrl == null || this.baseUrl.isEmpty()) {
            throw new IllegalStateException("Alkimi DSP API URL not configured. Please set alkimi.dsp.api-url in application.yml");
        }
        
        if (this.authToken == null || this.authToken.isEmpty()) {
            throw new IllegalStateException("Alkimi DSP auth token not configured. Please set alkimi.dsp.auth-token in application.yml");
        }
        
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
            
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Create a new campaign
     */
    public JsonNode createCampaign(CampaignRequest request) throws Exception {
        String url = baseUrl + "/api/campaign";
        String requestBody = objectMapper.writeValueAsString(request);
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + authToken)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(Duration.ofSeconds(60))
            .build();
            
        return executeRequest(httpRequest);
    }
    
    /**
     * Get campaign by ID
     */
    public JsonNode getCampaign(int campaignId) throws Exception {
        String url = baseUrl + "/api/campaign/" + campaignId;
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + authToken)
            .GET()
            .timeout(Duration.ofSeconds(30))
            .build();
            
        return executeRequest(httpRequest);
    }
    
    /**
     * List all campaigns with pagination
     */
    public JsonNode listCampaigns(int page, int size) throws Exception {
        String url = baseUrl + "/api/campaign";
        if (page > 0 || size > 0) {
            url += "?page=" + page + "&size=" + size;
        }
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + authToken)
            .GET()
            .timeout(Duration.ofSeconds(30))
            .build();
            
        return executeRequest(httpRequest);
    }
    
    /**
     * Filter campaigns by various criteria
     */
    public JsonNode filterCampaigns(Map<String, String> filters) throws Exception {
        StringBuilder url = new StringBuilder(baseUrl + "/api/filter/campaigns");
        
        if (!filters.isEmpty()) {
            url.append("?");
            List<String> params = new ArrayList<>();
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                params.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + 
                          URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
            url.append(String.join("&", params));
        }
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url.toString()))
            .header("Authorization", "Bearer " + authToken)
            .GET()
            .timeout(Duration.ofSeconds(30))
            .build();
            
        return executeRequest(httpRequest);
    }
    
    /**
     * Get campaigns for a specific advertiser
     */
    public JsonNode getAdvertiserCampaigns(int advertiserId, int page, int size) throws Exception {
        String url = baseUrl + "/api/advertiser/" + advertiserId + "/campaign";
        if (page > 0 || size > 0) {
            url += "?page=" + page + "&size=" + size;
        }
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + authToken)
            .GET()
            .timeout(Duration.ofSeconds(30))
            .build();
            
        return executeRequest(httpRequest);
    }
    
    /**
     * Execute HTTP request and handle response
     */
    private JsonNode executeRequest(HttpRequest request) throws Exception {
        LOGGER.info("Executing request: " + request.method() + " " + request.uri());
        
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            LOGGER.info("Response status: " + response.statusCode());
            LOGGER.fine("Response body: " + response.body());
            
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readTree(response.body());
            } else {
                // Handle error response
                JsonNode errorBody = null;
                try {
                    errorBody = objectMapper.readTree(response.body());
                } catch (Exception e) {
                    // Response body might not be JSON
                }
                
                String errorMessage = "API request failed with status " + response.statusCode();
                if (errorBody != null && errorBody.has("errors")) {
                    errorMessage += ": " + errorBody.get("errors").toString();
                } else if (errorBody != null && errorBody.has("error")) {
                    errorMessage += ": " + errorBody.get("error").asText();
                }
                
                throw new ApiException(errorMessage, response.statusCode(), errorBody);
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Request failed", e);
            throw new ApiException("Request failed: " + e.getMessage(), -1, null);
        }
    }
    
    /**
     * Custom exception for API errors
     */
    public static class ApiException extends Exception {
        private final int statusCode;
        private final JsonNode errorBody;
        
        public ApiException(String message, int statusCode, JsonNode errorBody) {
            super(message);
            this.statusCode = statusCode;
            this.errorBody = errorBody;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public JsonNode getErrorBody() {
            return errorBody;
        }
    }
}