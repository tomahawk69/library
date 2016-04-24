package org.library.entities;

import org.library.core.FileUtilsImpl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Iurii on 24.04.2016.
 */
public enum FileType {
    FB2(".fb2", false), EPUB(".epub", false), ZIP("zip", true);

    private final String extension;
    private final boolean isArchive;

    FileType(String extension, boolean isArchive) {
        this.extension = extension;
        this.isArchive = isArchive;
    }

    public String getExtension() {
        return extension;
    }

    public boolean isArchive() {
        return isArchive;
    }

    public static List<String> getExtensions() {
        List<String> result = new ArrayList<>();
        for (FileType fileType : values()) {
            result.add(fileType.extension);
        }
        return result;
    }

    public static FileType fileTypeByExtension(Path path) {
        String extension = FileUtilsImpl.getPathExtWithDot(path);
        for (FileType fileType : values()) {
            if (fileType.getExtension().equalsIgnoreCase(extension)) {
                return fileType;
            }
        }
        return null;
    }
}
