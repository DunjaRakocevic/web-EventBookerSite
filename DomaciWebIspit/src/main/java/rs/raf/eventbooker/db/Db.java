package rs.raf.eventbooker.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Properties;
import java.util.stream.Collectors;

public final class Db {

    private static HikariDataSource DS;

    private Db() {}

    public static void initAndMigrate() {
        try {

            Properties p = new Properties();
            try (InputStream in = Db.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in == null) throw new RuntimeException("db.properties not found");
                p.load(in);
            }


            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl(p.getProperty("db.url"));
            cfg.setUsername(p.getProperty("db.user"));
            cfg.setPassword(p.getProperty("db.password"));
            cfg.setMaximumPoolSize(Integer.parseInt(p.getProperty("db.pool.maxSize", "10")));
            cfg.setMinimumIdle(Integer.parseInt(p.getProperty("db.pool.minIdle", "2")));
            cfg.setDriverClassName("com.mysql.cj.jdbc.Driver");
            cfg.addDataSourceProperty("cachePrepStmts", "true");
            cfg.addDataSourceProperty("prepStmtCacheSize", "250");
            cfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            DS = new HikariDataSource(cfg);

            System.out.println("[Db] URL: " + cfg.getJdbcUrl());


            try (Connection con = getConnection()) {
                con.setAutoCommit(false); // ručno upravljanje transakcijama da bi try catch radio
                runSchema(con);
                con.commit();
            }
            System.out.println("[Db] migration OK");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("DB migration failed", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DS.getConnection();
    }

    private static void runSchema(Connection con) throws Exception {
        try (InputStream in = Db.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (in == null) {
                System.out.println("[Db] schema.sql not found");
                return;
            }
            String raw = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));


            for (String stmt : raw.split(";")) {
                String sql = stmt.trim();
                if (sql.isEmpty()) continue;
                try (Statement st = con.createStatement()) {
                    st.execute(sql);
                }
            }
        }
    }

    public static void shutdown() {
        if (DS != null) DS.close();
    }
}
