package org.library.core.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.core.utils.FileUtils;
import org.library.entities.FileInfo;
import org.library.entities.FileInfoHelper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

@Component()
@Scope("prototype")
public class FileServiceImpl implements FileService {
    private static final Logger LOGGER = LogManager.getLogger(FileServiceImpl.class);

    @Override
    public List<Path> getFilesList(List<String> extensions, boolean isRecursive, Path path) throws IOException {
        return FileUtils.getFilesList(extensions, true, path);
    }

    @Override
    public boolean proceedFileInfo(final FileInfo fileInfo, Path filePath) throws IOException {
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
