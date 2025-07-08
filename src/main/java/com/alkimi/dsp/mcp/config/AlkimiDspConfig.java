package com.alkimi.dsp.mcp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration for Alkimi DSP
 */
public class AlkimiDspConfig {
    @JsonProperty("api-url")
    private String apiUrl;

    @JsonProperty("auth-token")
    private String authToken;

    private Server server = new Server();

    private static AlkimiDspConfig instance;

    public static AlkimiDspConfig getInstance() {
        if (instance == null) {
            instance = loadConfiguration();
        }
        return instance;
    }

    private static AlkimiDspConfig loadConfiguration() {
        AlkimiDspConfig config = new AlkimiDspConfig();

        // Try to load from application.yml
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            InputStream input = null;

            // Try external file first
            File externalFile = new File("application.yml");
            if (externalFile.exists()) {
                input = new FileInputStream(externalFile);
            } else {
                // Try classpath
                input = AlkimiDspConfig.class.getClassLoader().getResourceAsStream("application.yml");
            }

            if (input != null) {
                ConfigRoot root = mapper.readValue(input, ConfigRoot.class);
                if (root != null && root.alkimi != null && root.alkimi.dsp != null) {
                    config = root.alkimi.dsp;
                }
                input.close();
            }
        } catch (Exception e) {
            // Log to stderr to avoid interfering with MCP protocol
            System.err.println("Could not load application.yml: " + e.getMessage());
        }

        // Override with environment variables if present
        String envApiUrl = System.getenv("ALKIMI_DSP_API_URL");
        if (envApiUrl != null && !envApiUrl.isEmpty()) {
            config.apiUrl = envApiUrl;
        }

        String envAuthToken = System.getenv("ALKIMI_DSP_AUTH_TOKEN");
        if (envAuthToken != null && !envAuthToken.isEmpty()) {
            config.authToken = envAuthToken;
        }

        // Validate required fields
        if (config.apiUrl == null || config.apiUrl.isEmpty()) {
            throw new IllegalStateException("Alkimi DSP API URL not configured. Please set alkimi.dsp.api-url in application.yml or ALKIMI_DSP_API_URL environment variable");
        }

        if (config.authToken == null || config.authToken.isEmpty()) {
            throw new IllegalStateException("Alkimi DSP auth token not configured. Please set alkimi.dsp.auth-token in application.yml or ALKIMI_DSP_AUTH_TOKEN environment variable");
        }
        config.authToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ4Snh6cThlZUVYNlktMC1hVWtwU3JHZ29KS0Q3VzBranR4ZlkzSEZ3THRVIn0.eyJleHAiOjE3NTA5MzE0OTgsImlhdCI6MTc1MDkyOTY5OCwianRpIjoiMjllN2U3ZjQtYjU0NC00ZGY3LWIyMzYtYWNjYzUzOTJhOTdkIiwiaXNzIjoiaHR0cHM6Ly9kZXYuaWFtLmFsa2ltaS5vcmcvYXV0aC9yZWFsbXMvaWFtZHNwIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjUwNzcyOWJjLWJhOGEtNGQyZS1hNzJmLWY5NDcxOTIyOTlmYSIsInR5cCI6IkJlYXJlciIsImF6cCI6ImRzcGJrIiwic2lkIjoiYzMyMThhNzYtN2NiZi00ZDA4LTk1NjctYjBlY2QyZTBjYzA5IiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJST0xFX01BQSIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLWlhbWRzcCJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoiZW1haWwgcHJvZmlsZSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJuYW1lIjoiVGltIEJlcm5lcnMiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ0aW1AZXhhbXBsZS5jb20iLCJnaXZlbl9uYW1lIjoiVGltIiwiZmFtaWx5X25hbWUiOiJCZXJuZXJzIiwiZW1haWwiOiJ0aW1AZXhhbXBsZS5jb20ifQ.IzgqS8TE9-bkuP0vOfq2n9FZzhSnieZYUIoxm1WKJ5t0LoW7PU1UGazSIgKKxL5M8E2InUxFTd_ESw50PolvVSiSGltDCbo3-NMw1P_hOX2o1EKNBjtjwGkQ5b2UOkXoOhCnmN1ShH8baTFPVI4BmCPAgbnispp20qWl8cmhV58ZJP0QcxRvybuVjt327bJLhUjErRu4cnZ5mubyAgg8UZjYaPYr5nT8Q-PGCcNvZNcfNo-iy2QoT8MybWeJEehdvXZr_BaACCLYy-f7GwfkeVK5z4d2ElY5hyBD7S8nEV2IYkBaG-3sXLJrygks36X9JJNo2DKY8xtXiZ-mQkEM2g";
        return config;
    }

    // Configuration root structure
    public static class ConfigRoot {
        public AlkimiRoot alkimi;
    }

    public static class AlkimiRoot {
        public AlkimiDspConfig dsp;
    }

    // Getters and setters
    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    /**
     * Server configuration
     */
    public static class Server {
        private String name = "alkimi-dsp-server";
        private String version = "1.0.0";

        @JsonProperty("thread-pool-size")
        private int threadPoolSize = 10;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public int getThreadPoolSize() {
            return threadPoolSize;
        }

        public void setThreadPoolSize(int threadPoolSize) {
            this.threadPoolSize = threadPoolSize;
        }
    }
}