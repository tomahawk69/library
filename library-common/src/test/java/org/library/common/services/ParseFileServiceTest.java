package org.library.common.services;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.library.common.entities.FileInfo;
import org.library.common.entities.FileType;
import org.library.common.entities.ParsedFile;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.Assert.*;

public class ParseFileServiceTest {
    private Path filePath;

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Before
    public void setUp() throws URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("_Test.fb2").getFile());
        filePath = Paths.get(file.getAbsolutePath());
    }

    @org.junit.Test
    public void parseXml() throws Exception {
        ParseFileService parsedFileService = new ParseFileService();

        FileInfo fileInfo = new FileInfo(filePath.toString());
        ParsedFile parsedFile = new ParsedFile(filePath, fileInfo);

        parsedFileService.parseFile(parsedFile);

        assertNotNull(parsedFile.getHeader());
        assertNotEquals(parsedFile.getHeader(), ParsedFile.Element.Empty);
        assertNotEquals(parsedFile.getSection(), ParsedFile.Section.Empty);
        assertNotNull(parsedFile.getCover().getBytes());
        assertEquals("cover.jpg", parsedFile.getCover().getCoverName());
        assertEquals("image/jpg", parsedFile.getCover().getCoverType());
        assertEquals(20, parsedFile.getNotesCount());
        assertEquals(3, parsedFile.getCommentsCount());

//        System.out.println(parsedFile.getHeader());
//        System.out.println(parsedFile.getSection());
//        System.out.println(parsedFile.getNotesCount());
//        System.out.println(parsedFile.getCommentsCount());
//
//
//        try (FileOutputStream stream = new FileOutputStream("c:\\temp\\111")) {
//            stream.write(parsedFile.getCover().getBytes());
//            stream.flush();
//        }
//

    }

}