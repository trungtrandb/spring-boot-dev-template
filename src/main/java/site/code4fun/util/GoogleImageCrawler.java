package site.code4fun.util;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import site.code4fun.service.AttachmentService;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Service để crawl ảnh từ Google Images
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleImageCrawler {
    private final AttachmentService imageDownloadService;

    private static final class Constants {
        private static final int NUMBER_IMAGE = 40;
        private static final List<String> SEARCH_STRINGS = List.of("cake");
        private static final boolean HEADLESS = true;
        private static final String GOOGLE_IMAGE_URL = "https://www.google.com/search?q=%s&source=lnms&tbm=isch&sa=X&ved=2ahUKEwie44_AnqLpAhUhBWMBHUFGD90Q_AUoAXoECBUQAw&biw=1920&bih=947";
        private static final String IMAGE_SELECTOR = "div[jsname='dTDiAc']";
        private static final String DIALOG_IMAGE_SELECTOR = "div[role='dialog'] a > img";
        private static final int SCROLL_THRESHOLD = 3;
        private static final int SCROLL_OFFSET = 60;
    }

    /**
     * Khởi tạo và chạy crawler cho tất cả các từ khóa tìm kiếm
     */
    // @PostConstruct
    public void init() {
        Constants.SEARCH_STRINGS.forEach(this::crawl);
    }

    /**
     * Crawl ảnh cho một từ khóa tìm kiếm
     * @param searchString Từ khóa tìm kiếm
     */
    @SneakyThrows
    public void crawl(String searchString) {
        log.info("Bắt đầu crawl ảnh cho từ khóa: {}", searchString);
        
        String url = String.format(Constants.GOOGLE_IMAGE_URL, 
            URLEncoder.encode(searchString, StandardCharsets.UTF_8));
        
        WebDriver driver = null;
        try {
            driver = initializeWebDriver();
            crawlImages(driver, searchString, url);
        } catch (Exception e) {
            log.error("Lỗi khi crawl ảnh: {}", e.getMessage(), e);
            throw e;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private WebDriver initializeWebDriver() {
        ChromeDriverAutoDownloader.setupChromeDriver();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1400,1050");
        
        if (Constants.HEADLESS) {
            options.addArguments("--headless", "--disable-gpu", "--ignore-certificate-errors");
        }

        return new ChromeDriver(options);
    }

    private void crawlImages(WebDriver driver, String searchString, String url) throws InterruptedException {
        AtomicInteger count = new AtomicInteger();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.navigate().to(url);
        Thread.sleep(1000);

        int thumbnailIdx = 0;
        while (count.get() < Constants.NUMBER_IMAGE) {
            try {
                processImageThumbnail(driver, js, wait, thumbnailIdx++, count, searchString);
            } catch (Exception e) {
                log.error("Lỗi khi xử lý thumbnail {}: {}", thumbnailIdx, e.getMessage());
            }
        }
    }

    private void processImageThumbnail(WebDriver driver, JavascriptExecutor js, WebDriverWait wait, 
            int thumbnailIdx, AtomicInteger count, String searchString) throws InterruptedException {
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(Constants.IMAGE_SELECTOR)));
        driver.findElements(By.cssSelector(Constants.IMAGE_SELECTOR)).get(thumbnailIdx).click();

        Thread.sleep(1000);
        processImages(driver, count, searchString);

        if (thumbnailIdx % Constants.SCROLL_THRESHOLD == 0) {
            scrollPage(js, thumbnailIdx);
        }
    }

    private void processImages(WebDriver driver, AtomicInteger count, String searchString) {
        List<WebElement> images = driver.findElements(By.cssSelector(Constants.DIALOG_IMAGE_SELECTOR));
        images.forEach(image -> {
            try {
                String imageUrl = image.getAttribute("src");
                if (isValidImageUrl(imageUrl)) {
                    downloadImage(imageUrl, searchString, count);
                }
            } catch (Exception e) {
                log.error("Lỗi khi xử lý ảnh: {}", e.getMessage());
            }
        });
    }

    private boolean isValidImageUrl(String imageUrl) {
        return isNotEmpty(imageUrl) && imageUrl.startsWith("http");
    }

    private void downloadImage(String imageUrl, String searchString, AtomicInteger count) {
        try {
            imageDownloadService.downloadImage(imageUrl, generateFileName(searchString));
            count.getAndIncrement();
        } catch (Exception e) {
            log.error("Lỗi khi tải ảnh {}: {}", imageUrl, e.getMessage());
        }
    }

    private void scrollPage(JavascriptExecutor js, int thumbnailIdx) {
        js.executeScript("window.scrollTo(0, " + thumbnailIdx * Constants.SCROLL_OFFSET + ");");
        log.info("Đã cuộn trang");
    }

    private String generateFileName(String prefix) {
        return prefix + "/" + System.currentTimeMillis() + ".jpeg";
    }
}
