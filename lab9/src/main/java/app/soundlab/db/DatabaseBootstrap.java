package app.soundlab.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseBootstrap {
    public static void initializeDatabase() {
        String createAudioTable = """
            CREATE TABLE IF NOT EXISTS Audio (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT,
                format TEXT,
                path TEXT
            );
        """;

        String createSegmentTable = """
            CREATE TABLE IF NOT EXISTS Segment (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                audio_id INTEGER,
                beginning_ms INTEGER,
                end_ms INTEGER,
                label TEXT,
                FOREIGN KEY (audio_id) REFERENCES Audio(id)
            );
        """;

        String createWorkspaceTable = """
            CREATE TABLE IF NOT EXISTS Workspace (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT,
                created_at DATETIME DEFAULT (datetime('now', 'localtime'))
            );
        """;

        String createWorkspaceAudioTable = """
            CREATE TABLE IF NOT EXISTS Workspace_Audio (
                workspace_id INTEGER,
                audio_id INTEGER,
                PRIMARY KEY (workspace_id, audio_id),
                FOREIGN KEY (workspace_id) REFERENCES Workspace(id),
                FOREIGN KEY (audio_id) REFERENCES Audio(id)
            );
        """;

        try (Connection connection = DatabaseConnector.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createAudioTable);
            renameTrackTable(statement);
            statement.execute(createSegmentTable);
            statement.execute(createWorkspaceTable);
            statement.execute(createWorkspaceAudioTable);
            migrateSegmentTimingColumns(statement);
            ensureSegmentLabelColumn(statement);
            renameNameColumnsToTitle(statement);
            System.out.println("Tables initialized!");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }

    private static void renameTrackTable(Statement statement) {
        try {
            statement.execute("ALTER TABLE Track RENAME TO Segment");
            System.out.println("Track table renamed to Segment.");
        } catch (SQLException e) {
            String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (message.contains("no such table")) {
                // Nothing to rename
                return;
            }
            if (message.contains("already exists")) {
                // Segment table already present
                return;
            }
            throw new RuntimeException("Failed to rename Track table to Segment", e);
        }
    }

    private static void migrateSegmentTimingColumns(Statement statement) {
        boolean renamedBeginning = renameColumn(statement, "Segment", "start_time", "beginning_ms");
        boolean renamedEnd = renameColumn(statement, "Segment", "end_time", "end_ms");
        if (renamedBeginning || renamedEnd) {
            try {
                statement.execute("UPDATE Segment SET beginning_ms = beginning_ms * 1000, end_ms = end_ms * 1000");
                System.out.println("Segment timing columns converted to milliseconds.");
            } catch (SQLException e) {
                throw new RuntimeException("Failed to convert Segment timings to milliseconds", e);
            }
        }
    }

    private static boolean renameColumn(Statement statement, String tableName, String oldName, String newName) {
        String sql = "ALTER TABLE " + tableName + " RENAME COLUMN " + oldName + " TO " + newName;
        try {
            statement.execute(sql);
            System.out.printf("%s column %s renamed to %s.%n", tableName, oldName, newName);
            return true;
        } catch (SQLException e) {
            String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (message.contains("no such column")) {
                return false;
            }
            if (message.contains("duplicate column name")) {
                return false;
            }
            if (message.contains("no such table")) {
                return false;
            }
            throw new RuntimeException("Failed to rename " + tableName + " column " + oldName + " to " + newName, e);
        }
    }

    private static void ensureSegmentLabelColumn(Statement statement) {
        String addLabelColumn = "ALTER TABLE Segment ADD COLUMN label TEXT";
        try {
            statement.execute(addLabelColumn);
            System.out.println("Segment.label column added.");
        } catch (SQLException e) {
            if (!e.getMessage().toLowerCase().contains("duplicate column name")) {
                throw new RuntimeException("Failed to ensure Segment.label column", e);
            }
        }
    }

    private static void renameNameColumnsToTitle(Statement statement) {
        renameColumn(statement, "Audio", "name", "title");
        renameColumn(statement, "Workspace", "name", "title");
    }
}

