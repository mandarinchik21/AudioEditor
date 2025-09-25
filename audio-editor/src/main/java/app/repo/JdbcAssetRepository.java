package app.repo;

import app.config.Database;
import app.domain.AudioAsset;

import java.sql.*;
import java.util.*;

public class JdbcAssetRepository implements Repository<AudioAsset> {

  @Override
  public AudioAsset save(AudioAsset a) {
    try (Connection c = Database.get()) {
      if (findById(a.id()).isPresent()) {
        try (PreparedStatement ps = c.prepareStatement(
          "UPDATE audio_assets SET path=?, format=?, channels=?, sample_rate=?, duration_ms=? WHERE id=?")) {
          ps.setString(1, a.path());
          ps.setString(2, a.format());
          ps.setInt(3, a.channels());
          ps.setInt(4, a.sampleRate());
          ps.setLong(5, a.durationMs());
          ps.setString(6, a.id());
          ps.executeUpdate();
        }
      } else {
        try (PreparedStatement ps = c.prepareStatement(
          "INSERT INTO audio_assets(id,project_id,path,format,channels,sample_rate,duration_ms) VALUES(?,?,?,?,?,?,?)")) {
          ps.setString(1, a.id());
          ps.setString(2, a.projectId());
          ps.setString(3, a.path());
          ps.setString(4, a.format());
          ps.setInt(5, a.channels());
          ps.setInt(6, a.sampleRate());
          ps.setLong(7, a.durationMs());
          ps.executeUpdate();
        }
      }
      return findById(a.id()).orElseThrow();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override public Optional<AudioAsset> findById(String id) {
    try (Connection c = Database.get();
         PreparedStatement ps = c.prepareStatement("SELECT * FROM audio_assets WHERE id=?")) {
      ps.setString(1, id);
      var rs = ps.executeQuery();
      if (rs.next()) return Optional.of(map(rs));
      return Optional.empty();
    } catch (SQLException e) { throw new RuntimeException(e); }
  }

  @Override public List<AudioAsset> findAll() {
    try (Connection c = Database.get();
         PreparedStatement ps = c.prepareStatement("SELECT * FROM audio_assets")) {
      var rs = ps.executeQuery();
      List<AudioAsset> out = new ArrayList<>();
      while (rs.next()) out.add(map(rs));
      return out;
    } catch (SQLException e) { throw new RuntimeException(e); }
  }

  @Override public void delete(String id) {
    try (Connection c = Database.get();
         PreparedStatement ps = c.prepareStatement("DELETE FROM audio_assets WHERE id=?")) {
      ps.setString(1, id);
      ps.executeUpdate();
    } catch (SQLException e) { throw new RuntimeException(e); }
  }

  public List<AudioAsset> findByProject(String projectId) {
    try (Connection c = Database.get();
         PreparedStatement ps = c.prepareStatement("SELECT * FROM audio_assets WHERE project_id=?")) {
      ps.setString(1, projectId);
      var rs = ps.executeQuery();
      List<AudioAsset> out = new ArrayList<>();
      while (rs.next()) out.add(map(rs));
      return out;
    } catch (SQLException e) { throw new RuntimeException(e); }
  }

  private AudioAsset map(ResultSet rs) throws SQLException {
    return new AudioAsset(
      rs.getString("id"),
      rs.getString("project_id"),
      rs.getString("path"),
      rs.getString("format"),
      rs.getInt("channels"),
      rs.getInt("sample_rate"),
      rs.getLong("duration_ms")
    );
  }
}

