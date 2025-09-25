package app.domain;

import java.time.*;

public record Project(
  String id,
  String name,
  int sampleRate,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {}

