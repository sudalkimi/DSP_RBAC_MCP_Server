package com.alkimi.dsp.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.github.mcpprotocol.mcp.server.McpServer;
import io.github.mcpprotocol.mcp.server.Tool;
import io.github.mcpprotocol.mcp.server.ToolParameter;
import io.github.mcpprotocol.mcp.server.ToolResult;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AlkimiDspMcpServer {
    private static final Logger logger = LoggerFactory.getLogger(AlkimiDspMcpServer.class);
    private static final String BASE_URL = System.getenv("ALKIMI_DSP_BASE_URL");
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final MediaType FORM = MediaType.get("application/x-www-form-urlencoded");
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final McpServer mcpServer;
    
    public AlkimiDspMcpServer() {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.mcpServer = new McpServer("Alkimi DSP MCP Server", "1.0.0");
        
        setupTools();
    }
    
    private void setupTools() {
        // Login Tool
        mcpServer.addTool(Tool.builder()
            .name("alkimi_login")
            .description("Login to Alkimi DSP with username and password")
            .parameters(Arrays.asList(
                new ToolParameter("username", "string", "User email address", true),
                new ToolParameter("password", "string", "User password", true),
                new ToolParameter("client_id", "string", "Client ID (default: dspbk)", false),
                new ToolParameter("grant_type", "string", "Grant type (default: password)", false),
                new ToolParameter("client_secret", "string", "Client secret", false)
            ))
            .handler(this::handleLogin)
            .build());
        
        // Reset Password Tool
        mcpServer.addTool(Tool.builder()
            .name("alkimi_reset_password")
            .description("Reset password for a user")
            .parameters(Arrays.asList(
                new ToolParameter("email", "string", "User email address", true),
                new ToolParameter("authorization", "string", "Bearer token", true),
                new ToolParameter("client_id", "string", "Client ID (default: dspbk)", false),
                new ToolParameter("grant_type", "string", "Grant type (default: password)", false),
                new ToolParameter("client_secret", "string", "Client secret", false)
            ))
            .handler(this::handleResetPassword)
            .build());
        
        // Forgot Password Tool
        mcpServer.addTool(Tool.builder()
            .name("alkimi_forgot_password")
            .description("Send forgot password email")
            .parameters(Arrays.asList(
                new ToolParameter("email", "string", "User email address", true),
                new ToolParameter("client_id", "string", "Client ID (default: dspbk)", false),
                new ToolParameter("grant_type", "string", "Grant type (default: password)", false),
                new ToolParameter("client_secret", "string", "Client secret", false)
            ))
            .handler(this::handleForgotPassword)
            .build());
        
        // Create KC User Account Tool
        mcpServer.addTool(Tool.builder()
            .name("alkimi_create_user")
            .description("Create a new user account in Keycloak")
            .parameters(Arrays.asList(
                new ToolParameter("authorization", "string", "Bearer token", true),
                new ToolParameter("firstName", "string", "User's first name", true),
                new ToolParameter("lastName", "string", "User's last name", true),
                new ToolParameter("emailId", "string", "User's email address", true),
                new ToolParameter("entityType", "string", "Entity type (e.g., HC)", true),
                new ToolParameter("entityId", "integer", "Entity ID", true),
                new ToolParameter("role", "string", "User role (e.g., ROLE_HCA)", true)
            ))
            .handler(this::handleCreateUser)
            .build());
        
        // Create Holding Company Tool
        mcpServer.addTool(Tool.builder()
            .name("alkimi_create_holding_company")
            .description("Create a new holding company")
            .parameters(Arrays.asList(
                new ToolParameter("authorization", "string", "Bearer token", true),
                new ToolParameter("name", "string", "Holding company name", true),
                new ToolParameter("countryName", "string", "Country name", true),
                new ToolParameter("description", "string", "Company description", false),
                new ToolParameter("domain", "string", "Company domain", false),
                new ToolParameter("logo", "string", "Base64 encoded logo image", false)
            ))
            .handler(this::handleCreateHoldingCompany)
            .build());
    }
    
    private CompletableFuture<ToolResult> handleLogin(Map<String, Object> params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                FormBody.Builder formBuilder = new FormBody.Builder()
                    .add("username", (String) params.get("username"))
                    .add("password", (String) params.get("password"))
                    .add("client_id", params.getOrDefault("client_id", "dspbk").toString())
                    .add("grant_type", params.getOrDefault("grant_type", "password").toString());
                
                if (params.containsKey("client_secret")) {
                    formBuilder.add("client_secret", (String) params.get("client_secret"));
                } else {
                    formBuilder.add("client_secret", "niU4R3lI5JgqyCkq4iI4sERVz9SvUG5X");
                }
                
                Request request = new Request.Builder()
                    .url(BASE_URL + "/auth/realms/iamdsp/protocol/openid-connect/token")
                    .post(formBuilder.build())
                    .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    String responseBody = response.body().string();
                    
                    if (response.isSuccessful()) {
                        return ToolResult.success(responseBody);
                    } else {
                        return ToolResult.error("Login failed: " + response.code() + " - " + responseBody);
                    }
                }
            } catch (IOException e) {
                logger.error("Error during login", e);
                return ToolResult.error("Login error: " + e.getMessage());
            }
        });
    }
    
    private CompletableFuture<ToolResult> handleResetPassword(Map<String, Object> params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                FormBody.Builder formBuilder = new FormBody.Builder()
                    .add("email", (String) params.get("email"))
                    .add("client_id", params.getOrDefault("client_id", "dspbk").toString())
                    .add("grant_type", params.getOrDefault("grant_type", "password").toString());
                
                if (params.containsKey("client_secret")) {
                    formBuilder.add("client_secret", (String) params.get("client_secret"));
                } else {
                    formBuilder.add("client_secret", "niU4R3lI5JgqyCkq4iI4sERVz9SvUG5X");
                }
                
                Request request = new Request.Builder()
                    .url(BASE_URL + "/api/account/password-reset")
                    .header("Authorization", (String) params.get("authorization"))
                    .post(formBuilder.build())
                    .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    String responseBody = response.body().string();
                    
                    if (response.isSuccessful()) {
                        return ToolResult.success(responseBody);
                    } else {
                        return ToolResult.error("Reset password failed: " + response.code() + " - " + responseBody);
                    }
                }
            } catch (IOException e) {
                logger.error("Error during password reset", e);
                return ToolResult.error("Reset password error: " + e.getMessage());
            }
        });
    }
    
    private CompletableFuture<ToolResult> handleForgotPassword(Map<String, Object> params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                FormBody.Builder formBuilder = new FormBody.Builder()
                    .add("email", (String) params.get("email"))
                    .add("client_id", params.getOrDefault("client_id", "dspbk").toString())
                    .add("grant_type", params.getOrDefault("grant_type", "password").toString());
                
                if (params.containsKey("client_secret")) {
                    formBuilder.add("client_secret", (String) params.get("client_secret"));
                } else {
                    formBuilder.add("client_secret", "niU4R3lI5JgqyCkq4iI4sERVz9SvUG5X");
                }
                
                Request request = new Request.Builder()
                    .url(BASE_URL + "/api/account/forgot-password")
                    .post(formBuilder.build())
                    .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    String responseBody = response.body().string();
                    
                    if (response.isSuccessful()) {
                        return ToolResult.success(responseBody);
                    } else {
                        return ToolResult.error("Forgot password failed: " + response.code() + " - " + responseBody);
                    }
                }
            } catch (IOException e) {
                logger.error("Error during forgot password", e);
                return ToolResult.error("Forgot password error: " + e.getMessage());
            }
        });
    }
    
    private CompletableFuture<ToolResult> handleCreateUser(Map<String, Object> params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ObjectNode jsonBody = objectMapper.createObjectNode()
                    .put("firstName", (String) params.get("firstName"))
                    .put("lastName", (String) params.get("lastName"))
                    .put("emailId", (String) params.get("emailId"))
                    .put("entityType", (String) params.get("entityType"))
                    .put("entityId", ((Number) params.get("entityId")).intValue())
                    .put("role", (String) params.get("role"));
                
                RequestBody body = RequestBody.create(
                    objectMapper.writeValueAsString(jsonBody), JSON);
                
                Request request = new Request.Builder()
                    .url(BASE_URL + "/api/account/kc-invite-user")
                    .header("Authorization", (String) params.get("authorization"))
                    .post(body)
                    .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    String responseBody = response.body().string();
                    
                    if (response.isSuccessful()) {
                        return ToolResult.success(responseBody);
                    } else {
                        return ToolResult.error("Create user failed: " + response.code() + " - " + responseBody);
                    }
                }
            } catch (IOException e) {
                logger.error("Error creating user", e);
                return ToolResult.error("Create user error: " + e.getMessage());
            }
        });
    }
    
    private CompletableFuture<ToolResult> handleCreateHoldingCompany(Map<String, Object> params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ObjectNode jsonBody = objectMapper.createObjectNode()
                    .put("name", (String) params.get("name"))
                    .put("countryName", (String) params.get("countryName"));
                
                if (params.containsKey("description")) {
                    jsonBody.put("description", (String) params.get("description"));
                }
                if (params.containsKey("domain")) {
                    jsonBody.put("domain", (String) params.get("domain"));
                }
                if (params.containsKey("logo")) {
                    jsonBody.put("logo", (String) params.get("logo"));
                }
                
                RequestBody body = RequestBody.create(
                    objectMapper.writeValueAsString(jsonBody), JSON);
                
                Request request = new Request.Builder()
                    .url(BASE_URL + "/api/holding-company")
                    .header("Authorization", (String) params.get("authorization"))
                    .post(body)
                    .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    String responseBody = response.body().string();
                    
                    if (response.isSuccessful()) {
                        return ToolResult.success(responseBody);
                    } else {
                        return ToolResult.error("Create holding company failed: " + response.code() + " - " + responseBody);
                    }
                }
            } catch (IOException e) {
                logger.error("Error creating holding company", e);
                return ToolResult.error("Create holding company error: " + e.getMessage());
            }
        });
    }
    
    public void start() {
        mcpServer.start();
        logger.info("Alkimi DSP MCP Server started");
    }
    
    public static void main(String[] args) {
        AlkimiDspMcpServer server = new AlkimiDspMcpServer();
        server.start();
    }
}