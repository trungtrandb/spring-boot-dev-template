package site.code4fun;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static site.code4fun.constant.AppConstants.COMMA;

@Component
@Data
public class ApplicationProperties implements Serializable {

    @Resource
    private Environment environment;

    @Value("${spring.mail.username}")
    private String mailUserName;
    @Value("${shop-domain}")
    private String shopDomain;
    @Value("${api-domain}")
    private String apiDomain;

    @Value("${second-level-domains}")
    private String secondLevelDomains;

    @Value("${face-recognition-domain}")
    private String faceDomain;
    @Value("${yolo-domain}")
    private String yoloDomain;
    @Value("${admin-domain}")
    private String adminDomain;

    @Value("${spring.application.version}")
    private String appVersion;
    @Value("${spring.application.name}")
    private String appName;

    // TWILIO
    @Value("${twilio.user-name}")
    private String userName;
    @Value("${twilio.password}")
    private String password;
    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    // AWS
    @Value("${aws.cloud-watch.access-key-id}")
    private String accessKeyId;

    @Value("${aws.cloud-watch.secret-access-key}")
    private String secretAccessKey;

    @Value("${aws.cloud-watch.region}")
    private String region;

    @Value("${aws.cloud-watch.bucket-name}")
    private String bucketName;

    @Value("${token.signing.key}")
    private String jwtSigningKey;

    // MinIO
    @Value("${minio.access-key}")
    private String accessKey;
    @Value("${minio.secret-key}")
    private String secretKey;
    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.bucket-name}")
    private String minIoBucketName;


    // OnePay
    @Value("${onepay.access-code}")
    private String accessCode;
    @Value("${onepay.secret-key}")
    private String onePaySecretKey;
    @Value("${onepay.merchant}")
    private String merchant;
    @Value("${onepay.url}")
    private String onePayUrl;
    private String returnUrl = apiDomain + "/orders/ipn/one-pay";

    // VnPay
    @Value("${vnpay.terminal-id}")
    private String vnpTmnCode;
    @Value("${vnpay.secret-key}")
    private String vnpSecretKey;
    @Value("${vnpay.version}")
    private String vnpVersion;
    @Value("${vnpay.url}")
    private String vnpUrl;
    private String vnpReturnUrl = apiDomain + "/orders/ipn/vnpay";


    public String getCorsDomains(){
        StringBuilder str = new StringBuilder("http://localhost:*");
        if (isNotEmpty(shopDomain)){
            str.append(",").append(shopDomain);
        }
        if (isNotEmpty(adminDomain)){
            str.append(",").append(adminDomain);
        }
        return str.toString();
    }

    public boolean isLocal() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if (profile.toLowerCase().contains("local")) {
                return true;
            }
        }
        return false;
    }

    public String[] getCorsDomainArray(){
        return getCorsDomains().replace(" ", "").split(COMMA);
    }

    public String getCookieDomain(){
        return isLocal() ? null : secondLevelDomains;
    }
}
