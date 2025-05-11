package site.code4fun.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static site.code4fun.constant.AppConstants.DEFAULT_STORAGE_PROVIDER;

@Component
@Slf4j
public class CloudStorageFactory { //TODO move to interface and implement
    private final Map<String, CloudStorage> cloudStorageProviders = new HashMap<>();

    public CloudStorageFactory(List<CloudStorage> providers){
        providers.forEach(payment -> cloudStorageProviders.put(payment.getClass().getSimpleName(), payment));
    }

    public CloudStorage getProvider(String provider) {
        for (Map.Entry<String, CloudStorage> entry : cloudStorageProviders.entrySet()){
            if (entry.getKey().toUpperCase().equalsIgnoreCase(provider)){
                return entry.getValue();
            }
        }
        log.warn("provider {} not found, using default {}", provider, DEFAULT_STORAGE_PROVIDER);
        return cloudStorageProviders.get(DEFAULT_STORAGE_PROVIDER);
    }

    public List<String> getProviders(){
        return new ArrayList<>(cloudStorageProviders.keySet());
    }
}