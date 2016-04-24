package org.library.core;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Created by Iurii on 17.01.2016.
 */
public interface FileUtils {
    List<Path> getFilesList(List<String> extensions, boolean recursive, Path path) throws IOException;
    Long getFileSize(Path path) throws IOException;
    LocalDateTime getFileLastModifiedDate(Path path) throws IOException;
    String getFileMD5Hash(Path path) throws NoSuchAlgorithmException, IOException;
}
