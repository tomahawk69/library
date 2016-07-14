package org.library.common.services;

import org.library.common.entities.FileInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FileService {

    List<Path> getFilesList(List<String> extensions, boolean isRecursive, Path path) throws IOException;
    boolean checkFileInfoIsChangedAndUpdateIt(FileInfo fileInfo, Path filePath) throws IOException;

}
