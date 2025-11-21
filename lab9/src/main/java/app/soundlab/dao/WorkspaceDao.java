package app.soundlab.dao;

import app.soundlab.db.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WorkspaceDao {
    public void addWorkspace(String title) {
        String sql = "INSERT INTO Workspace (title) VALUES (?)";
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert workspace", e);
        }
    }

    public List<String> getAllWorkspaces() {
        String sql = "SELECT * FROM Workspace ORDER BY id DESC";
        List<String> workspaces = new ArrayList<>();
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                workspaces.add("\"" + resultSet.getString("title") + "\" - last save: "
                        + resultSet.getString("created_at"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch workspaces", e);
        }
        return workspaces;
    }

    public void addAudioToWorkspace(int workspaceId, int audioId) {
        String sql = "INSERT INTO Workspace_Audio (workspace_id, audio_id) VALUES (?, ?)";
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, workspaceId);
            statement.setInt(2, audioId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to link audio to workspace", e);
        }
    }

    public int getLastInsertedWorkspaceId() {
        String sql = "SELECT id FROM Workspace ORDER BY id DESC LIMIT 1";
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get last inserted workspace ID", e);
        }
        return -1;
    }
}
