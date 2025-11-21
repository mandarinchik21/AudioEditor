package app.soundlab.dao;

import app.soundlab.db.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AudioDao {
    public void create(String title, String format, String path) {
        String sql = "INSERT INTO Audio (title, format, path) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.setString(2, format);
            statement.setString(3, path);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert audio", e);
        }
    }

    public List<String> getAll() {
        String sql = "SELECT * FROM Audio ORDER BY id DESC";
        List<String> audioList = new ArrayList<>();
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                audioList.add(resultSet.getString("title") + " - " + resultSet.getString("path"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch audio", e);
        }
        return audioList;
    }

    public List<String> getByWorkspace(int workspaceId) {
        String sql = """
            SELECT Audio.title FROM Audio
            JOIN Workspace_Audio ON Audio.id = Workspace_Audio.audio_id
            WHERE Workspace_Audio.workspace_id = ?
            """;
        List<String> audioList = new ArrayList<>();
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, workspaceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    audioList.add(resultSet.getString("title"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch audio for workspace", e);
        }
        return audioList;
    }

    public boolean exists(String path) {
        String sql = "SELECT COUNT(*) FROM Audio WHERE path = ?";
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, path);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check if audio exists", e);
        }
        return false;
    }
}
