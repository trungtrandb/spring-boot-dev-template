package site.code4fun.service.google;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Lazy
public class FirebaseAdminService {

    private final GoogleServiceAccountProperties config;

    @SneakyThrows
    private FirebaseAuth getInstance(){
        try{
            return FirebaseAuth.getInstance();
        }catch (Exception e){
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(config.getCredential())
                    .build();
            FirebaseApp.initializeApp(options);
            return FirebaseAuth.getInstance();
        }
    }

    @SneakyThrows
    @SuppressWarnings("unused")
    public String createCustomToken(String uuid, Map<String, Object> claims){
        return getInstance().createCustomToken(uuid, claims);
    }
}
