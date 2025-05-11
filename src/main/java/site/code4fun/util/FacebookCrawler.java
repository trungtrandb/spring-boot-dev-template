package site.code4fun.util;

import java.util.Base64;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import site.code4fun.model.FaceDataEntity;
import site.code4fun.model.request.FormMetaData;
import site.code4fun.repository.jpa.FaceDataRepository;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import site.code4fun.service.FaceRecognitionService;
import site.code4fun.service.integrations.YoloService;

import static site.code4fun.util.RandomUtils.getRandomNumberInRange;
import static site.code4fun.util.UrlParserUtils.getFbId;

@Component
@Slf4j
@AllArgsConstructor
@SuppressWarnings("all")
public class FacebookCrawler {
    private static final boolean HEADLESS = false;
    private static final Queue<String> queue = new LinkedList<>();
    private final FaceRecognitionService service;
    private final FaceDataRepository repository;
    private final YoloService yoloService;


    @SneakyThrows
    // @PostConstruct
    public void rootCrawl(){
        queue.add("https://www.facebook.com/someone.else");
        // https://googlechromelabs.github.io/chrome-for-testing/#stable  --> Download the newest driver
        ChromeDriverAutoDownloader.setupChromeDriver();
       
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1400,1050");
        options.addArguments("--disable-extensions");
        if (HEADLESS){
            options.addArguments("--headless", "--disable-gpu", "--ignore-certificate-errors");
        }
        List<FaceDataEntity> lst = repository.findAll();
        WebDriver driver = new ChromeDriver(options);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        while (!lst.isEmpty()) {
            String url;
            if (!queue.isEmpty()) {
                url = queue.poll();
            } else {
                FaceDataEntity entity = (lst.size() > 1)
                    ? lst.get(getRandomNumberInRange(0, lst.size() - 1))
                    : lst.get(0);
                url = entity.getContent();
            }
            doCrawl(driver, js, url);
        }
    }
    @SneakyThrows
    void trySleep(Integer second){
        long s = second != null ? second : getRandomNumberInRange(1,10);
        log.info("Sleeping {}s...", s);

        Thread.sleep(s * 1000);
    }

    void doCrawl(WebDriver driver, JavascriptExecutor js, String fbUrl ){
        if (StringUtils.isBlank(fbUrl) ) return; // Not valid url or url already process

        String url;
        if (fbUrl.contains("profile.php")){
            url = fbUrl.concat("&sk=friends");
//            sk=photos
//            sk=about
        }else{
            url = fbUrl.concat("/friends");
        }
        driver.get(url);
        js.executeScript("scrollTo(0, 500)");
        trySleep(2);
        int i = 1;
        do {
            try {
                WebElement element = driver.findElement(By.xpath("/html/body/div[1]/div/div[1]/div/div[3]/div/div/div[1]/div[1]/div/div/div[4]/div/div/div/div[1]/div/div/div/div/div[3]/div[" +i+"]"));

                WebElement nameElement = element.findElement(By.xpath("div[2]/div[1]/span/a"));
                String name = nameElement.getText();
                String href =  nameElement.getAttribute("href");

                String avtHref = element.findElement(By.xpath("div[1]/span/a/img")).getAttribute("src");

                js.executeScript("scrollBy(0, 62)");

                String fbId = getFbId(href);
                if(repository.findByFbId(fbId).isEmpty()){
                    log.info("{} {} {}", name, href, avtHref);
                    saveContact(name, href, avtHref, fbUrl, fbId);
                    trySleep(2);
                }else{
                    log.info("Existed, skipping {} - {}", name, href);
                    trySleep(1);
                }

                i++;
            }catch (Exception e){
                log.error(e.getMessage());
                break;
            }
        }while (i > 0);
    }
    private void saveContact(String name, String href, String avtSrc, String srcUrl, String fbId){
        try {
            MultipartFile file = FileUtils.urlToMultipartFile(avtSrc,  System.currentTimeMillis() + ".jpeg", ContentType.IMAGE_JPEG.getMimeType());

            FormMetaData info = new FormMetaData();
            info.setName(name);
            info.setContent(href);
            info.setSrcUrl(srcUrl);
            info.setFbId(fbId);
            Map<String, List<String>> detectResult = (Map<String, List<String>>) yoloService.identifyPersons(file);
            var lst = detectResult.get("cropped_faces");

            FileUtils.downLoadToLocal( avtSrc, fbId + "/" + fbId + ".jpeg");
            FileUtils.downLoadToLocal(  Base64.getDecoder().decode(lst.get(0)), fbId + "/" + System.currentTimeMillis() + ".jpeg");
            service.addUserToDataset(file, info);
        }catch (Exception e){
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }
}
