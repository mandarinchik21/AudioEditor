package app.domain;

public record AudioAsset(
  String id,
  String projectId,
  String path,
  String format,
  int channels,
  int sampleRate,
  long durationMs
) {}

