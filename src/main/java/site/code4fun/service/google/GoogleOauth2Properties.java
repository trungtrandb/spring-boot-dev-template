package site.code4fun.service.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;


@Data
@ConfigurationProperties(prefix = "google")
public class GoogleOauth2Properties {
    private String clientId;
    private String clientSecret;
    private final List<String> googleScopes = Arrays.asList(
            DriveScopes.DRIVE_FILE,
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email",
            "openid"
    );

    @SneakyThrows
    public GoogleTokenResponse exchangeToken(String code, String redirect) {
        GoogleAuthorizationCodeTokenRequest codeTokenRequest = new GoogleAuthorizationCodeTokenRequest(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                clientId,
                clientSecret,
                code,
                redirect);
        return codeTokenRequest.execute();
    }
}
