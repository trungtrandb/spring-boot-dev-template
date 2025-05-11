package site.code4fun.service.google;

import com.google.auth.oauth2.GoogleCredentials;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.ByteArrayInputStream;
import java.util.Base64;

@Data
@ConfigurationProperties(prefix = "google-service-account")
public class GoogleServiceAccountProperties {

    private String firebaseAdminKey;
    private String defaultRegionName;
    private String projectId;
    private String bucketName;

    @SneakyThrows
    public GoogleCredentials getCredential(){
        return GoogleCredentials.fromStream(new ByteArrayInputStream(Base64.getDecoder().decode(firebaseAdminKey)));
    }
}