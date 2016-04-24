package org.library.entities;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Date;

public class FileInfo {
    private final Path path;
    private final String fileName;
    private final FileType fileType;
    private Long fileSize;
    private LocalDateTime modifiedDate;
    private String md5Hash;

    public FileInfo(Path path) {
        this.path = path;
        fileName = path.getFileName().toString();
        fileType = FileType.fileTypeByExtension(path);
    }

    public Path getPath() {
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

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileInfo fileInfo = (FileInfo) o;

        return path.equals(fileInfo.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "path=" + path +
                ", fileName='" + fileName + '\'' +
                ", fileType=" + fileType +
                ", fileSize=" + fileSize +
                ", modifiedDate=" + modifiedDate +
                ", md5Hash='" + md5Hash + '\'' +
                '}';
    }
}
