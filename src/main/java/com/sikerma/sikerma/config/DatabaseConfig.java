package com.sikerma.sikerma.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    // GANTI PORT KE 3307 (sesuai hasil SHOW VARIABLES)
    private static final String URL = "jdbc:mysql://localhost:3306/db_sikerma?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "Jere123";

    public static Connection connect() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("✅ Berhasil terkoneksi ke Database SIKERMA!");
            return conn;
        } catch (SQLException e) {
            System.out.println("❌ Gagal koneksi: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}