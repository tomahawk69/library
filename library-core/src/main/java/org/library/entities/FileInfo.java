package org.library.entities;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

public class FileInfo {
    private final String path;
    private final String fileName;
    private final FileType fileType;
    private UUID uuid;
    private Long fileSize;
    private LocalDateTime modifiedDate;
    private String md5Hash;

    public FileInfo(String path) {
        this(null, path, Paths.get(path).getFileName().toString(), null, null);
    }

    public FileInfo(UUID uuid, String path, String fileName, Long fileSize, String md5Hash) {
        this.uuid = uuid;
        this.path = path;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.md5Hash = md5Hash;
        this.fileType = FileType.fileTypeByExtension(Paths.get(path));
    }

    public String getPath() {
        return path;
    }

    public String getFileName() {
        return fileName;
    }

    public FileType getFileType() {
        return fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileInfo fileInfo = (FileInfo) o;

        return path.equalsIgnoreCase(fileInfo.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "path='" + path + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileType=" + fileType +
                ", uuid=" + uuid +
                ", fileSize=" + fileSize +
                ", modifiedDate=" + modifiedDate +
                ", md5Hash='" + md5Hash + '\'' +
                '}';
    }
}
