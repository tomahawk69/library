package org.library.core.utils;

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

    private static final String FILE_SIZE_ATTRIBUTE = "basic:size";

    public static List<Path> getFilesList(List<String> extensions, boolean isRecursive, Path path) throws IOException {
        DirectoryStream.Filter<Path> filter = createFilter(extensions, isRecursive);
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

    @SuppressWarnings("StatementWithEmptyBody")
    public static String getFileMD5Hash(Path path) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = Files.newInputStream(path);
             DigestInputStream ignored = new DigestInputStream(is, md))
        {
            while (is.read() != -1)
                ;
        }
        byte[] digest = md.digest();
        return md5BytesToHex(digest);
    }

    /**
     * Convert bytes to Hex
     * @param bytes array of bytes
     * @return hex presentation of bytes
     */
    private static String md5BytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (Byte oneByte : bytes) {
            String hex = Integer.toHexString(0xff & oneByte);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Extract list of files by a given filter
     * @param path current directory
     * @param filter prepared directory stream filter
     * @return collections of Path objects
     */
    private static List<Path> getFilesListEx(Path path, DirectoryStream.Filter<Path> filter) throws IOException {
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
        }
        for (Path folder : folders) {
            result.addAll(getFilesListEx(folder, filter));
        }
        return result;
    }

    /**
     * Create a filter for files/directories: allowed/disallow recursive and allowed file extensions
     * @param extensions list of extensions
     * @param isRecursive should subdirectories also be accepted
     * @return ready to use directory stream filter
     */
    public static DirectoryStream.Filter<Path> createFilter(List<String> extensions, boolean isRecursive) {
        List filterPrepared =
                extensions.parallelStream()
                    .map(entry -> removeTrailingPeriod(entry).toLowerCase())
                    .collect(Collectors.toList());
        return path -> !Files.isDirectory(path) &&
                filterPrepared.contains("*") ||
                filterPrepared.contains(getPathExt(path).toLowerCase()) ||
                Files.isDirectory(path) && isRecursive;
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
