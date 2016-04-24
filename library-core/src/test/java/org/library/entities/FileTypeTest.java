package org.library.entities;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class FileTypeTest {

    @Test
    public void testFileTypeByExtension() throws Exception {
        FileType fileType = FileType.FB2;
        Path path = Paths.get("test" + fileType.getExtension());
        FileType result = FileType.fileTypeByExtension(path);
        assertEquals(fileType, result);
    }
}