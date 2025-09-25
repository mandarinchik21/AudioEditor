package app.config;

import java.nio.file.*;
import java.sql.*;

public class Database {
  private static final String DB_FILE = "audio_editor.db";
  private static final String URL = "jdbc:sqlite:" + DB_FILE;

  public static Connection get() throws SQLException {
    return DriverManager.getConnection(URL);
  }

  public static void init() {
    try {
      if (!Files.exists(Path.of(DB_FILE))) {
        Files.createFile(Path.of(DB_FILE));
      }
      try (Connection c = get(); Statement s = c.createStatement()) {
        s.executeUpdate("""
          CREATE TABLE IF NOT EXISTS users(
            id TEXT PRIMARY KEY,
            name TEXT NOT NULL,
            role TEXT NOT NULL DEFAULT 'user'
          );
        """);
        s.executeUpdate("""
          CREATE TABLE IF NOT EXISTS projects(
            id TEXT PRIMARY KEY,
            name TEXT NOT NULL,
            sample_rate INTEGER NOT NULL,
            created_at TEXT NOT NULL DEFAULT (datetime('now')),
            updated_at TEXT
          );
        """);
        s.executeUpdate("""
          CREATE TABLE IF NOT EXISTS audio_assets(
            id TEXT PRIMARY KEY,
            project_id TEXT NOT NULL,
            path TEXT NOT NULL,
            format TEXT NOT NULL,
            channels INTEGER NOT NULL,
            sample_rate INTEGER NOT NULL,
            duration_ms INTEGER NOT NULL,
            FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE CASCADE
          );
        """);
      }
    } catch (Exception e) {
      throw new RuntimeException("DB init error", e);
    }
  }
}

