package app.service;

import app.domain.AudioAsset;
import app.domain.Project;
import app.repo.JdbcAssetRepository;
import app.repo.JdbcProjectRepository;

import java.nio.file.*;
import java.util.*;

public class EditorCoreService {
  private final JdbcProjectRepository projects = new JdbcProjectRepository();
  private final JdbcAssetRepository assets = new JdbcAssetRepository();

  public List<Project> listProjects() {
    return projects.findAll();
  }

  public Project createProject(String name, int sampleRate) {
    var id = UUID.randomUUID().toString();
    return projects.save(new Project(id, name, sampleRate, null, null));
  }

  public void deleteProject(String id) {
    projects.delete(id);
  }

  public List<AudioAsset> listAssets(String projectId) {
    return assets.findByProject(projectId);
  }

  public AudioAsset importFile(String projectId, Path file) {
    String format = getExt(file).toLowerCase();
    int channels = 2;
    int sr = 44100;
    long dur = 60_000;

    var a = new AudioAsset(UUID.randomUUID().toString(), projectId,
      file.toAbsolutePath().toString(), format, channels, sr, dur);
    return assets.save(a);
  }

  private String getExt(Path p) {
    var name = p.getFileName().toString();
    int i = name.lastIndexOf('.');
    return i >= 0 ? name.substring(i + 1) : "wav";
  }
}

