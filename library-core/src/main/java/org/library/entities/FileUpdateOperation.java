package org.library.entities;

public class FileUpdateOperation {
    private final UpdateType updateType;
    private final FileInfo fileInfo;

    public FileUpdateOperation(UpdateType updateType, FileInfo fileInfo) {
        this.updateType = updateType;
        this.fileInfo = fileInfo;
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public enum UpdateType {
        INSERT, UPDATE, DELETE
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileUpdateOperation that = (FileUpdateOperation) o;

        if (updateType != that.updateType) return false;
        return fileInfo.equals(that.fileInfo);

    }

    @Override
    public int hashCode() {
        int result = updateType.hashCode();
        result = 31 * result + fileInfo.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "FileUpdateOperation{" +
                "updateType=" + updateType +
                ", fileInfo=" + fileInfo +
                '}';
    }
}
