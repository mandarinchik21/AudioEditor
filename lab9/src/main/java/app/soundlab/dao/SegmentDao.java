package app.soundlab.dao;

import app.soundlab.db.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SegmentDao {
    public void create(int audioId, int beginningMs, int endMs, String label) {
        String sql = "INSERT INTO Segment (audio_id, beginning_ms, end_ms, label) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, audioId);
            statement.setInt(2, beginningMs);
            statement.setInt(3, endMs);
            statement.setString(4, label);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert segment", e);
        }
    }

    public List<String> getAll() {
        String sql = """
            SELECT Segment.beginning_ms, Segment.end_ms, Segment.label, Audio.title
            FROM Segment JOIN Audio ON Segment.audio_id = Audio.id
            ORDER BY Segment.id DESC
            """;
        List<String> segmentList = new ArrayList<>();
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String label = resultSet.getString("label");
                String labelPrefix = (label == null || label.isBlank()) ? "" : "[" + label + "] ";
                int beginningMs = resultSet.getInt("beginning_ms");
                int endMs = resultSet.getInt("end_ms");
                double beginningSeconds = beginningMs / 1000.0;
                double endSeconds = endMs / 1000.0;
                segmentList.add(labelPrefix
                        + formatSeconds(beginningSeconds) + "s to "
                        + formatSeconds(endSeconds) + "s in "
                        + resultSet.getString("title"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch segments", e);
        }
        return segmentList;
    }

    public void update(int segmentId, int beginningMs, int endMs, String label) {
        String sql = "UPDATE Segment SET beginning_ms = ?, end_ms = ?, label = ? WHERE id = ?";
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, beginningMs);
            statement.setInt(2, endMs);
            statement.setString(3, label);
            statement.setInt(4, segmentId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update segment", e);
        }
    }

    public int getAudioIdByPath(String path) {
        String sql = "SELECT id FROM Audio WHERE path = ?";
        try (Connection connection = DatabaseConnector.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, path);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get audio ID", e);
        }
        return -1;
    }

    private String formatSeconds(double seconds) {
        if (seconds == (long) seconds) {
            return String.valueOf((long) seconds);
        }
        return String.format("%.3f", seconds);
    }
}
