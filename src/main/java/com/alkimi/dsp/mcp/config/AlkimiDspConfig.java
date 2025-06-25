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