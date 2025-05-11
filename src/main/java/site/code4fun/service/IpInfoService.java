package site.code4fun.service;

import com.maxmind.geoip2.WebServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Component
@Lazy
@Slf4j
public class IpInfoService {
    private static WebServiceClient client;
    public String getIpInfo(String ip){
        try{

            InetAddress ipAddress = InetAddress.getByName(ip);

            String country = getClient().country(ipAddress).getCountry().getName();
            String city = getClient().city(ipAddress).getCity().getName();
            return country + ", " + city;
        }catch (Exception e){
            log.warn("Can't get ip info, cause {}", e.getMessage());
        }
        return "Unknown";
    }

    private static WebServiceClient getClient(){
        if (client == null){
            int accountId = Integer.parseInt(System.getenv("MAXMIND_ACCOUNT_ID"));
            String licenseKey = System.getenv("MAXMIND_LICENSE_KEY");
            client = new WebServiceClient.Builder(accountId, licenseKey)
                    .host("geolite.info")
                    .build();
        }
        return client;
    }
}
