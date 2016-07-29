package org.library.common.utils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.library.common.entities.FileInfo;
import org.library.common.entities.ParsedFile;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class Fb2ParseHandlerTest {
    private Path filePath;

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("_Test.fb2").getFile());
        filePath = Paths.get(file.getAbsolutePath());
    }

    @Test
    public void checkIsCoverPositive() throws Exception {
        FileInfo fileInfo = new FileInfo(filePath.toString());
        ParsedFile parsedFile = new ParsedFile(fileInfo);
        Fb2ParserHandler handler = new Fb2ParserHandler(parsedFile);
        handler.setStage(ParseStage.Head);
        ParsedFile.Element element = new ParsedFile.Element("image");
        String imageLink = "link to the image";
        element.addAttribute("href", imageLink);
        assertTrue(handler.checkIsCover(element));
        assertEquals(handler.getCoverName(), imageLink);
    }

    @Test
    public void checkIsCoverPositiveNegative() throws Exception {
        FileInfo fileInfo = new FileInfo(filePath.toString());
        ParsedFile parsedFile = new ParsedFile(fileInfo);
        Fb2ParserHandler handler = new Fb2ParserHandler(parsedFile);
        handler.setStage(ParseStage.Head);
        ParsedFile.Element element = new ParsedFile.Element("image");
        assertFalse(handler.checkIsCover(element));
    }

    @Test
    public void checkIsCoverNegative() throws Exception {
        FileInfo fileInfo = new FileInfo(filePath.toString());
        ParsedFile parsedFile = new ParsedFile(fileInfo);
        Fb2ParserHandler handler = new Fb2ParserHandler(parsedFile);
        handler.setStage(ParseStage.Head);
        ParsedFile.Element element = new ParsedFile.Element("other then");
        assertFalse(handler.checkIsCover(element));
    }

    @Test
    public void checkIsCoverNullElement() throws Exception {
        FileInfo fileInfo = new FileInfo(filePath.toString());
        ParsedFile parsedFile = new ParsedFile(fileInfo);
        Fb2ParserHandler handler = new Fb2ParserHandler(parsedFile);
        expectedException.expect(NullPointerException.class);
        handler.checkIsCover(null);
    }

}