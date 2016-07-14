package org.library.common.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.common.entities.FileInfo;
import org.library.common.utils.FileInfoHelper;
import org.library.common.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

public class FileServiceImpl implements FileService {
    private static final Logger LOGGER = LogManager.getLogger(FileServiceImpl.class);

    @Override
    public List<Path> getFilesList(List<String> extensions, boolean isRecursive, Path path) throws IOException {
        return FileUtils.getFilesList(extensions, true, path);
    }

    @Override
    public boolean checkFileInfoIsChangedAndUpdateIt(final FileInfo fileInfo, Path filePath) throws IOException {
        Long fileSize = FileUtils.getFileSize(filePath);
        LocalDateTime fileDate = FileUtils.getFileLastModifiedDate(filePath);
        boolean result = FileInfoHelper.checkFileInfoChanged(fileInfo, fileSize, fileDate);
        if (result) {
            synchronized (fileInfo.getPath()) {
                fileInfo.setFileSize(fileSize);
                fileInfo.setModifiedDate(fileDate);
                try {
                    FileInfoHelper.updateFileInfo(filePath, fileInfo, fileSize, fileDate);
                } catch (NoSuchAlgorithmException e) {
                    LOGGER.error("Cant get MD5 hash algorithm ((", e);
                }
            }
        }
        return result;
    }

}
