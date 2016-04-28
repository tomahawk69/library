package org.library.core;

import org.library.entities.FileInfo;

import java.util.List;
import java.util.UUID;

public interface DataBaseStorage {
    void save(FileInfo fileInfo);
    void update(FileInfo fileInfo);
    void delete(FileInfo fileInfo);
    FileInfo get(UUID uuid);
    List<FileInfo> getList();
}
