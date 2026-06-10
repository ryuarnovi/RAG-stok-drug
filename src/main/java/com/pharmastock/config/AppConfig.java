package com.pharmastock.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AppConfig {

    private final Properties properties;

    private static final Map<String, String> ENV_MAPPING = new HashMap<>();

    static {
        ENV_MAPPING.put("db.url", "DB_URL");
        ENV_MAPPING.put("db.username", "DB_USERNAME");
        ENV_MAPPING.put("db.password", "DB_PASSWORD");
        ENV_MAPPING.put("db.pool.size", "DB_POOL_SIZE");
        ENV_MAPPING.put("ai.provider", "AI_PROVIDER");
        ENV_MAPPING.put("ai.openai.api_key", "AI_OPENAI_API_KEY");
        ENV_MAPPING.put("ai.openai.model", "AI_OPENAI_MODEL");
        ENV_MAPPING.put("ai.gemini.api_key", "AI_GEMINI_API_KEY");
        ENV_MAPPING.put("ai.gemini.model", "AI_GEMINI_MODEL");
        ENV_MAPPING.put("app.name", "APP_NAME");
        ENV_MAPPING.put("app.version", "APP_VERSION");
        ENV_MAPPING.put("app.near_expiry_days", "APP_NEAR_EXPIRY_DAYS");
        ENV_MAPPING.put("reports.output_dir", "REPORTS_OUTPUT_DIR");
    }

    public AppConfig() {
        this.properties = new Properties();
        loadDefaults();
        loadFromEnvFile();
        loadFromSystemEnv();
    }

    private void loadDefaults() {
        properties.setProperty("db.url", "jdbc:mysql://localhost:3306/pharmastock?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        properties.setProperty("db.username", "root");
        properties.setProperty("db.password", "");
        properties.setProperty("db.pool.size", "10");
        properties.setProperty("ai.provider", "LOCAL");
        properties.setProperty("ai.openai.api_key", "");
        properties.setProperty("ai.openai.model", "gpt-3.5-turbo");
        properties.setProperty("ai.gemini.api_key", "");
        properties.setProperty("ai.gemini.model", "gemini-pro");
        properties.setProperty("app.name", "PharmaStock");
        properties.setProperty("app.version", "1.0.0");
        properties.setProperty("app.near_expiry_days", "30");
        properties.setProperty("reports.output_dir", "reports");
    }

    private void loadFromEnvFile() {
        Path envPath = Paths.get(".env");
        if (!Files.exists(envPath)) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(envPath)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int equalsIndex = line.indexOf('=');
                if (equalsIndex > 0) {
                    String envKey = line.substring(0, equalsIndex).trim();
                    String value = line.substring(equalsIndex + 1).trim();
                    if (!value.isEmpty()) {
                        // Map env var key to property key (e.g., AI_GEMINI_API_KEY → ai.gemini.api_key)
                        String propKey = findPropKey(envKey);
                        if (propKey != null) {
                            properties.setProperty(propKey, value);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Tidak dapat membaca .env file: " + e.getMessage());
        }
    }

    private String findPropKey(String envKey) {
        for (Map.Entry<String, String> mapping : ENV_MAPPING.entrySet()) {
            if (mapping.getValue().equals(envKey)) {
                return mapping.getKey();
            }
        }
        return null;
    }

    private void loadFromSystemEnv() {
        for (Map.Entry<String, String> mapping : ENV_MAPPING.entrySet()) {
            String propKey = mapping.getKey();
            String envKey = mapping.getValue();
            String envValue = System.getenv(envKey);
            if (envValue != null && !envValue.isEmpty()) {
                properties.setProperty(propKey, envValue);
            }
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public String getAppName() {
        return get("app.name", "PharmaStock");
    }

    public String getAppVersion() {
        return get("app.version", "1.0.0");
    }

    public int getNearExpiryDays() {
        return getInt("app.near_expiry_days", 30);
    }

    public String getAIProvider() {
        return get("ai.provider", "LOCAL");
    }

    public String getReportsOutputDir() {
        return get("reports.output_dir", "reports");
    }
}
