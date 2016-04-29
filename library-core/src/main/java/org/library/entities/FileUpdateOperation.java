package org.library.entities;

import java.util.concurrent.atomic.AtomicBoolean;

public class FileUpdateOperation {
    private final UpdateType updateType;
    private final FileInfo fileInfo;
    private final FileInfo rollbackCopy;
    private AtomicBoolean isSuccess = new AtomicBoolean(false);

    public FileUpdateOperation(UpdateType updateType, FileInfo fileInfo) {
        this.updateType = updateType;
        this.fileInfo = fileInfo;
        this.rollbackCopy = null;
    }

    public FileUpdateOperation(UpdateType updateType, FileInfo fileInfo, FileInfo rollbackCopy) {
        this.updateType = updateType;
        this.fileInfo = fileInfo;
        this.rollbackCopy = rollbackCopy;
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public FileInfo getRollbackCopy() {
        return rollbackCopy;
    }

    public boolean getIsSuccess() {
        return isSuccess.get();
    }

    public void setSuccess() {
        isSuccess.set(true);
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

        return fileInfo.getPath().equals(that.fileInfo.getPath());
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
                ", rollbackCopy=" + rollbackCopy +
                ", isSuccess=" + isSuccess +
                '}';
    }
}
