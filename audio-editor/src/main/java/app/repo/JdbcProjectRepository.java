package app.repo;

import app.config.Database;
import app.domain.Project;

import java.sql.*;
import java.time.*;
import java.util.*;

public class JdbcProjectRepository implements Repository<Project> {

  @Override
  public Project save(Project p) {
    try (Connection c = Database.get()) {
      if (findById(p.id()).isPresent()) {
        try (PreparedStatement ps = c.prepareStatement(
          "UPDATE projects SET name=?, sample_rate=?, updated_at=datetime('now') WHERE id=?")) {
          ps.setString(1, p.name());
          ps.setInt(2, p.sampleRate());
          ps.setString(3, p.id());
          ps.executeUpdate();
        }
        return findById(p.id()).orElseThrow();
      } else {
        try (PreparedStatement ps = c.prepareStatement(
          "INSERT INTO projects(id,name,sample_rate,created_at) VALUES(?,?,?,datetime('now'))")) {
          ps.setString(1, p.id());
          ps.setString(2, p.name());
          ps.setInt(3, p.sampleRate());
          ps.executeUpdate();
        }
        return findById(p.id()).orElseThrow();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<Project> findById(String id) {
    try (Connection c = Database.get();
         PreparedStatement ps = c.prepareStatement("SELECT * FROM projects WHERE id=?")) {
      ps.setString(1, id);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) return Optional.of(map(rs));
      return Optional.empty();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<Project> findAll() {
    try (Connection c = Database.get();
         PreparedStatement ps = c.prepareStatement("SELECT * FROM projects ORDER BY created_at DESC")) {
      ResultSet rs = ps.executeQuery();
      List<Project> out = new ArrayList<>();
      while (rs.next()) out.add(map(rs));
      return out;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void delete(String id) {
    try (Connection c = Database.get();
         PreparedStatement ps = c.prepareStatement("DELETE FROM projects WHERE id=?")) {
      ps.setString(1, id);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private Project map(ResultSet rs) throws SQLException {
    return new Project(
      rs.getString("id"),
      rs.getString("name"),
      rs.getInt("sample_rate"),
      toLdt(rs.getString("created_at")),
      toLdt(rs.getString("updated_at"))
    );
  }

  private LocalDateTime toLdt(String s) {
    return s == null ? null : LocalDateTime.parse(s.replace(' ', 'T'));
  }
}

