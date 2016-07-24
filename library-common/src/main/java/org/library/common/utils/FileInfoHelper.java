package org.library.common.utils;

import org.library.common.entities.FileInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        // first set MD5 to avoid false batchUpdateFileInfo when MD5 operation return an error
        fileInfo.setMd5Hash(FileUtils.getFileMD5Hash(path));
        fileInfo.setFileSize(fileSize);
        fileInfo.setModifiedDate(fileDate);
    }

    public static boolean checkFileInfoChanged(FileInfo fileInfo, Long fileSize, LocalDateTime fileDate) {
        return !(fileSize.equals(fileInfo.getFileSize()) && fileDate.equals(fileInfo.getModifiedDate()));
    }


    /**
     * Convert given list of file info to the map
     * @param path base path
     * @param fileInfoList list of file info
     * @return map of path:file info
     */
    public static Map<Path, FileInfo> listOfFileInfoToMap(final Path path, final List<FileInfo> fileInfoList) {
        return fileInfoList.stream().collect(Collectors.toMap(x -> path.resolve(x.getPath()), x -> x));
    }
}
