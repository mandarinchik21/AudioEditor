package app.repo;

import java.util.*;

public interface Repository<T> {
  T save(T t);
  Optional<T> findById(String id);
  List<T> findAll();
  void delete(String id);
}

