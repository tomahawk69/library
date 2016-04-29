package org.library.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtils {

    public static final String FILE_SIZE_ATTRIBUTE = "basic:size";

    public static List<Path> getFilesList(List<String> extensions, boolean isRecursive, Path path) throws IOException {
        DirectoryStream.Filter filter = createFilter(extensions, isRecursive);
        return getFilesListEx(path, filter);
    }

    public static Long getFileSize(Path path) throws IOException {
        return (Long) Files.getAttribute(path, FILE_SIZE_ATTRIBUTE, LinkOption.NOFOLLOW_LINKS);
    }

    public static LocalDateTime getFileLastModifiedDate(Path path) throws IOException {
        FileTime fileTime = Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS);
        return fileTimeToLocalDateTime(fileTime);
    }

    public static LocalDateTime fileTimeToLocalDateTime(FileTime fileTime) {
        return LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
    }

    public static FileTime localDateTimeToFileTime(LocalDateTime localDateTime) {
        return FileTime.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static String getFileMD5Hash(Path path) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = Files.newInputStream(path);
             DigestInputStream dis = new DigestInputStream(is, md))
        {
            while (is.read() != -1)
                ;
        }
        byte[] digest = md.digest();
        return md5BytesToHex(digest);
    }

    /**
     * Convert bytes to Hex
     * @param bytes
     * @return
     */
    public static String md5BytesToHex(byte[] bytes) {
        StringBuffer hexString = new StringBuffer();
        for (int i=0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xff & bytes[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Extract list of files by a given filter
     * @param path
     * @param filter
     * @return
     */
    public static List<Path> getFilesListEx(Path path, DirectoryStream.Filter filter) throws IOException {
        List<Path> result = new ArrayList<>();
        List<Path> folders = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, filter)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    folders.add(entry);
                } else {
                    result.add(entry);
                }
            }
        } catch (IOException e) {
            throw e;
        }
        for (Path folder : folders) {
            result.addAll(getFilesListEx(folder, filter));
        }
        return result;
    }

    /**
     * Create a filter for files/directories: allowed/disallow recursive and allowed file extensions
     * @param extensions
     * @param isRecursive
     * @return
     */
    public static DirectoryStream.Filter createFilter(List<String> extensions, boolean isRecursive) {
        List filterPrepared =
                extensions.parallelStream()
                    .map(entry -> removeTrailingPeriod(entry).toLowerCase())
                    .collect(Collectors.toList());
        DirectoryStream.Filter filter = new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path path) throws IOException {
                return !Files.isDirectory(path) &&
                        filterPrepared.contains("*") ||
                        filterPrepared.contains(getPathExt(path).toLowerCase()) ||
                        Files.isDirectory(path) && isRecursive;
            }
        };

        return filter;
    }

    private static String removeTrailingPeriod(final String extension) {
        int i = 0;
        while (extension.length() - i > 0 &&
                extension.substring(i, i + 1).equals(".")) {
            i++;
        }
        if (i > 0) {
            return extension.substring(i);
        } else {
            return extension;
        }
    }

    public static String getPathExt(final Path path) {
        String pathStr = path.toString();
        int period = pathStr.lastIndexOf(".");
        if (period > 0) {
            return pathStr.substring(period + 1);
        } else {
            return "";
        }
    }

    public static String getPathExtWithDot(final Path path) {
        String pathStr = path.toString();
        int period = pathStr.lastIndexOf(".");
        if (period > 0) {
            return pathStr.substring(period);
        } else {
            return "";
        }
    }

    public static Path constructAbsolutePath(Path basePath, Path path) {
        return basePath.resolve(path).normalize();
    }

    public static String constructRelativePath(Path basePath, Path path) {
        return basePath.relativize(path).toString();
    }


}
