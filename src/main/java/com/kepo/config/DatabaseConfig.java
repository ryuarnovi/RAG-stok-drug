package com.kepo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseConfig {

    private static DatabaseConfig instance;
    private HikariDataSource dataSource;

    private DatabaseConfig() {
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    public void initialize(AppConfig appConfig) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(appConfig.get("db.url"));
        config.setUsername(appConfig.get("db.username"));
        config.setPassword(appConfig.get("db.password"));
        config.setMaximumPoolSize(Integer.parseInt(appConfig.get("db.pool.size", "10")));
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(10000);
        config.setMaxLifetime(1800000);
        
        // PostgreSQL properties
        config.addDataSourceProperty("reWriteBatchedInserts", "true");
        config.addDataSourceProperty("assumeMinServerVersion", "9.4");

        this.dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource belum diinisialisasi. Panggil initialize() terlebih dahulu.");
        }
        return dataSource.getConnection();
    }

    public void runSchema() {
        String sql = loadResource("/database/schema.sql");
        if (sql == null || sql.isBlank()) {
            System.err.println("schema.sql tidak ditemukan atau kosong.");
            return;
        }
        executeSqlScript(sql);
    }

    public boolean isDatabaseEmpty() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            return true;
        }
        return true;
    }

    public void runSeed() {
        if (!isDatabaseEmpty()) {
            System.out.println("Database sudah terisi data. Seeding dilewati.");
            return;
        }
        String sql = loadResource("/database/seed.sql");
        if (sql == null || sql.isBlank()) {
            System.err.println("seed.sql tidak ditemukan atau kosong.");
            return;
        }
        executeSqlScript(sql);
    }

    private void executeSqlScript(String script) {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            String[] lines = script.split("\n");
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                String trimmedLine = line.trim();
                if (!trimmedLine.startsWith("--") && !trimmedLine.startsWith("/*") && !trimmedLine.startsWith("#")) {
                    sb.append(line).append("\n");
                }
            }

            String[] statements = sb.toString().split(";");
            for (String s : statements) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error menjalankan SQL script: " + e.getMessage());
        }
    }

    private String loadResource(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            System.err.println("Error membaca resource: " + path + " - " + e.getMessage());
            return null;
        }
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
