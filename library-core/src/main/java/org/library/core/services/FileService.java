package org.library.core.services;

import org.library.entities.FileInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

interface FileService {

    List<Path> getFilesList(List<String> extensions, boolean isRecursive, Path path) throws IOException;
    boolean checkFileInfoIsChangedAndUpdateIt(FileInfo fileInfo, Path filePath) throws IOException;

}