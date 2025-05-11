package site.code4fun.util;

import java.io.File;
import java.net.URL;
import java.util.Base64;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
@SuppressWarnings("unused")
public class FileUtils {
    private static final int BUFFER_SIZE = 8192;
    private static final int CONNECTION_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "pdf", "doc", "docx");
    
    @Value("${app.upload.dir:target/tmp/}")
    private static String localFolder;

    public static MultipartFile urlToMultipartFile(String fileUrl, String fileName, String contentType) {
        validateInput(fileUrl, fileName, contentType);
        try {
            byte[] fileBytes = downloadFileAsByteArray(fileUrl);
            validateFileSize(fileBytes.length);
            return new ByteArrayMultipartFile(fileBytes, fileName, contentType);
        } catch (Exception e) {
            log.error("Error converting URL to MultipartFile: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert URL to MultipartFile", e);
        }
    }

    public static MultipartFile base64ToMultipartFile(String base64String, String fileName, String contentType) {
        validateInput(base64String, fileName, contentType);
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);
            validateFileSize(decodedBytes.length);
            return new ByteArrayMultipartFile(decodedBytes, fileName, contentType);
        } catch (Exception e) {
            log.error("Error converting Base64 to MultipartFile: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert Base64 to MultipartFile", e);
        }
    }

    public static void downLoadToLocal(byte[] content, String fileName) {
        validateInput(content, fileName);
        try {
            Path filePath = Paths.get(localFolder, sanitizeFileName(fileName));
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, content);
            log.info("Successfully downloaded file: {}", fileName);
        } catch (Exception e) {
            log.error("Error downloading file to local: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download file to local", e);
        }
    }

    public static void downLoadToLocal(String url, String fileName) {
        validateInput(url, fileName);
        try {
            URL fileUrl = new URL(url);
            Path filePath = Paths.get(localFolder, sanitizeFileName(fileName));
            Files.createDirectories(filePath.getParent());
            try (InputStream in = fileUrl.openStream()) {
                Files.copy(in, filePath);
            }
            log.info("Successfully downloaded file from URL: {}", url);
        } catch (Exception e) {
            log.error("Error downloading file from URL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download file from URL", e);
        }
    }

    public static String getFileExtension(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return "";
        }
        try {
            String extension = contentType.split("/")[1].toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                log.warn("Unsupported file extension: {}", extension);
                return "";
            }
            return "." + extension;
        } catch (Exception e) {
            log.error("Error getting file extension: {}", e.getMessage(), e);
            return "";
        }
    }

    private static void validateInput(Object... inputs) {
        for (Object input : inputs) {
            if (input == null || (input instanceof String && ((String) input).trim().isEmpty())) {
                throw new IllegalArgumentException("Input parameters cannot be null or empty");
            }
        }
    }

    private static void validateFileSize(long size) {
        if (size > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " + MAX_FILE_SIZE + " bytes");
        }
    }

    private static String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    static class ByteArrayMultipartFile implements MultipartFile {

        private final byte[] content;
        private final String name;
        private final String contentType;

        ByteArrayMultipartFile(byte[] content, String name, String contentType) {
            this.content = content;
            this.name = name;
            this.contentType = contentType;
        }

        @NotNull
        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {//NOSONAR
            return name;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @NotNull
        @Override
        public byte[] getBytes() {
            return content;
        }

        @NotNull
        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            try (InputStream in = getInputStream()) {
                java.nio.file.Files.copy(in, dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static byte[] downloadFileAsByteArray(String fileUrl) throws IOException {
        URI uri = URI.create(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

        try (InputStream inputStream = connection.getInputStream();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long totalBytesRead = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalBytesRead += bytesRead;
                if (totalBytesRead > MAX_FILE_SIZE) {
                    throw new IOException("File size exceeds maximum allowed size");
                }
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            return byteArrayOutputStream.toByteArray();
        } finally {
            connection.disconnect();
        }
    }
}

