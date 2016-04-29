package org.library.entities;

import org.library.core.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

public class FileInfoHelper {

    public static FileInfo createFileInfoCopy(FileInfo fileInfo) {
        FileInfo result = new FileInfo(fileInfo.getPath());
        result.setFileSize(fileInfo.getFileSize());
        result.setMd5Hash(fileInfo.getMd5Hash());
        result.setModifiedDate(fileInfo.getModifiedDate());
        result.setUuid(fileInfo.getUUID());
        return result;
    }

    public static void updateFileInfo(Path path, FileInfo fileInfo, Long fileSize, LocalDateTime fileDate) throws IOException, NoSuchAlgorithmException {
        // first set MD5 to avoid false update when MD5 operation return an error
        fileInfo.setMd5Hash(FileUtils.getFileMD5Hash(FileUtils.constructAbsolutePath(path, Paths.get(fileInfo.getPath()))));
        fileInfo.setFileSize(fileSize);
        fileInfo.setModifiedDate(fileDate);
    }


}
