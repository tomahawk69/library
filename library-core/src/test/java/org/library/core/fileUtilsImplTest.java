package org.library.core;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class fileUtilsImplTest {

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void testCreateFilterDirectory() throws Exception {
        List<String> extensions = asList(new String("test"), new String("test1"), new String("..test3"));
        DirectoryStream.Filter filter = FileUtils.createFilter(extensions, true);

        Path dirPath = tempDir.newFolder().toPath();
        assertEquals(filter.accept(dirPath), true);
    }

    @Test
    public void testCreateFilterDirectoryNegative() throws Exception {
        List<String> extensions = asList(new String("test"), new String("test1"), new String("..test3"));
        DirectoryStream.Filter filter = FileUtils.createFilter(extensions, false);

        Path dirPath = tempDir.newFolder().toPath();
        assertEquals(filter.accept(dirPath), false);
    }

    @Test
    public void testCreateFilterFilePositive() throws Exception {
        String ext = ".tesT1";
        List<String> extensions = asList(new String("test"), new String("....Test1"), new String("..test3"));
        DirectoryStream.Filter filter = FileUtils.createFilter(extensions, false);

        Path dirPath = tempDir.newFile("test1......" + ext).toPath();
        assertEquals(filter.accept(dirPath), true);
    }

    @Test
    public void testCreateFilterFilePositiveAllFilesInFilter() throws Exception {
        String ext = ".tesT1";
        List<String> extensions = asList(new String("test"), new String("*"));
        DirectoryStream.Filter filter = FileUtils.createFilter(extensions, false);

        Path dirPath = tempDir.newFile("test1......" + ext).toPath();
        assertEquals(filter.accept(dirPath), true);
    }

    @Test
    public void testCreateFilterFileNegativeNoExt() throws Exception {
        List<String> extensions = asList(new String("test"), new String("test1"), new String("..test3"));
        DirectoryStream.Filter filter = FileUtils.createFilter(extensions, false);

        Path dirPath = tempDir.newFile("test1").toPath();
        assertEquals(filter.accept(dirPath), false);
    }

    @Test
    public void testCreateFilterFileNegativeNoFilter() throws Exception {
        String ext = ".test11";
        List<String> extensions = asList(new String("test"), new String("test1"), new String("..test3"));
        DirectoryStream.Filter filter = FileUtils.createFilter(extensions, false);

        Path dirPath = tempDir.newFile("test1" + ext).toPath();
        assertEquals(filter.accept(dirPath), false);
    }

    @Test
    public void testGetPathExt() throws Exception {
        String ext = ".test11";
        String expectedExt = "test11";
        Path dirPath = tempDir.newFile("test1......" + ext).toPath();
        String resultExt = FileUtils.getPathExt(dirPath);
        assertEquals(expectedExt, resultExt);
    }

    @Test
    public void testGetPathExtNoExt() throws Exception {
        String expectedExt = "";
        Path dirPath = tempDir.newFile("test1......" ).toPath();
        String resultExt = FileUtils.getPathExt(dirPath);
        assertEquals(expectedExt, resultExt);
    }

    @Test
    public void testGetFilesListNotRecursive() throws IOException {
        String ext = ".test";
        String ext1 = ".TEST";
        String ext2 = ".test1";
        List<String> extensions = asList(ext);
        Path path1 = Paths.get(tempDir.newFile("first" + ext).getAbsolutePath());
        Path path2 = Paths.get(tempDir.newFile("second" + ext).getAbsolutePath());
        Path path3 = Paths.get(tempDir.newFile("third" + ext1).getAbsolutePath());
        Path path4 = Paths.get(tempDir.newFile("fourth" + ext2).getAbsolutePath());
        List<Path> expectedResult = asList(path1, path2, path3);
        List<Path> result = FileUtils.getFilesList(extensions, false, tempDir.getRoot().toPath());
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetFilesListRecursive() throws IOException {
        String ext = ".test1";
        String folderName = "testFolder";
        List<String> extensions = asList(ext);
        tempDir.newFolder(folderName);
        Path path1 = Paths.get(tempDir.newFile("first" + ext).getAbsolutePath());
        Path path2 = Paths.get(tempDir.newFile(folderName + "/second" + ext).getAbsolutePath());
        List<Path> expectedResult = asList(path1, path2);
        List<Path> result = FileUtils.getFilesList(extensions, true, tempDir.getRoot().toPath());
        assertEquals(expectedResult, result);
    }

    @Test
    public void testConvertFileTimeToLocalDateTime() throws Exception {
        Path path = Paths.get(tempDir.newFile("testDate").getAbsolutePath());
        FileTime fileTime = Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS);
        LocalDateTime localDateTime = FileUtils.fileTimeToLocalDateTime(fileTime);
        FileTime resultFileTime = FileUtils.localDateTimeToFileTime(localDateTime);
        assertEquals(fileTime, resultFileTime);
    }

    @Test
    public void testGetFileSize() throws Exception {
        String testString = "test";
        Path path = Paths.get(tempDir.newFile("testSize").getAbsolutePath());
        Files.write(path, testString.getBytes(), StandardOpenOption.APPEND);
        long fileSize = FileUtils.getFileSize(path);
        assertEquals(testString.length(), fileSize);
    }

    @Test
    public void testMD5() throws Exception {
        String testString = "testing is really nice thing";
        Path path = Paths.get(tempDir.newFile("testSize").getAbsolutePath());
        Files.write(path, testString.getBytes(), StandardOpenOption.APPEND);
        String md5Hash = FileUtils.getFileMD5Hash(path);
        assertTrue(md5Hash.length() == 32);
    }

}