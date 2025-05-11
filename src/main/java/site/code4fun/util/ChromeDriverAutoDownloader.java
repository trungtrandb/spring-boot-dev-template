package site.code4fun.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChromeDriverAutoDownloader {
    private static final String CHROMEDRIVER_BASE_URL = "https://storage.googleapis.com/chrome-for-testing-public/";
    private static final String LATEST_RELEASE_URL = "https://googlechromelabs.github.io/chrome-for-testing/LATEST_RELEASE_STABLE";
    private static final String DRIVER_DIR = "./";
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final String ARCH = System.getProperty("os.arch").toLowerCase();

    public static void setupChromeDriver() {
        try {
            Path driverDir = Paths.get(DRIVER_DIR);
            if (!Files.exists(driverDir)) {
                Files.createDirectories(driverDir);
            }

            String latestVersion = getLatestChromeDriverVersion();
            String driverPath = DRIVER_DIR + getDriverFileName();

            if (!(new File(driverPath).exists())) { // if not up to date, just delete file chromedriver
                downloadAndExtractDriver(latestVersion);
            }

            System.setProperty("webdriver.chrome.driver", driverPath);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Failed to setup ChromeDriver: " + e.getMessage());
        }
    }

    private static String getLatestChromeDriverVersion() throws IOException {
        URL url = new URL(LATEST_RELEASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            return reader.readLine().trim();
        }
    }

    private static String getDriverFileName() {
        if (OS.contains("win")) {
            return "chromedriver.exe";
        } else if (OS.contains("mac") || OS.contains("linux")) {
            return "chromedriver";
        }
        throw new RuntimeException("Unsupported operating system: " + OS);
    }

    private static String getDriverZipName() {
        if (OS.contains("win")) {
            return "win64/chromedriver-win64.zip";
        } else if (OS.contains("mac")) {
            return ARCH.contains("aarch64") || ARCH.contains("arm") ? 
                   "mac-arm64/chromedriver-mac-arm64.zip" : 
                   "mac-x64/chromedriver-mac-x64.zip";
        } else if (OS.contains("linux")) {
            return "linux64/chromedriver-linux64.zip";
        }
        throw new RuntimeException("Unsupported operating system: " + OS);
    }
    private static void downloadAndExtractDriver(String version) throws IOException {
        String zipFileName = getDriverZipName();
        String downloadUrl = CHROMEDRIVER_BASE_URL + version + "/" + zipFileName;
        String zipPath = DRIVER_DIR + zipFileName.substring(zipFileName.lastIndexOf('/') + 1);

        URL url = new URL(downloadUrl);
        try (InputStream in = url.openStream();
             FileOutputStream out = new FileOutputStream(zipPath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        unzipFile(zipPath);
        Files.delete(Paths.get(zipPath));

        if (!OS.contains("win")) {
            File driverFile = new File(DRIVER_DIR + getDriverFileName());
            boolean executable = driverFile.setExecutable(true);
            if (!executable) {
              log.warn("Failed to set executable permissions on {}", driverFile.getAbsolutePath());
            }
        }
    }

    private static void unzipFile(String zipPath) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                // Adjust for nested structure in ChromeDriver zips
                if (entryName.endsWith(getDriverFileName())) {
                    File newFile = new File(DRIVER_DIR + getDriverFileName());
                    boolean created = newFile.getParentFile().mkdirs();
                    if (!created) {
                      log.warn("Failed to create directory: {}", newFile.getParent());
                    }
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }
}