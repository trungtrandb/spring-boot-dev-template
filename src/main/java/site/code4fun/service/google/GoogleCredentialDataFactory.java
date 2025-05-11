package site.code4fun.service.google;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.AbstractDataStore;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import site.code4fun.model.User;
import site.code4fun.repository.jpa.UserRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Component
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class GoogleCredentialDataFactory implements DataStoreFactory {
    private final UserRepository repository;

    @Override
    public DataStore<StoredCredential> getDataStore(String s) {
        return new GoogleCredentialDataStore(this, s, repository);
    }
}

class GoogleCredentialDataStore extends AbstractDataStore<StoredCredential> {

    private final UserRepository repository;

    protected GoogleCredentialDataStore(DataStoreFactory dataStoreFactory, String id, UserRepository repository) {
        super(dataStoreFactory, id);
        this.repository = repository;
    }

    @Override
    public Set<String> keySet() {
        return repository.findAll().stream().map(User::getEmail).collect(Collectors.toSet());
    }

    @Override
    public Collection<StoredCredential> values() {
        return repository.findAll().stream().map(User::toStoredCredential).toList();
    }

    @Override
    public StoredCredential get(String key) {
        Optional<User> googleCredential = repository.findByUsername(key);
        if (googleCredential.isPresent()
                && (isNotEmpty(googleCredential.get().getToken())
                || isNotEmpty(googleCredential.get().getRefreshToken()))) {
            return googleCredential.get().toStoredCredential();
        }
        return null;
    }

    @Override
    public DataStore<StoredCredential> set(String key, StoredCredential value) {
        Optional<User> googleCredentialDetails = repository.findByUsername(key);
        if (googleCredentialDetails.isPresent()){
            User u = googleCredentialDetails.get();
            u.setToken(value.getAccessToken());
            u.setRefreshToken(value.getRefreshToken());
            repository.save(u);
            return this;
        }
        return null;
    }

    @Override
    public DataStore<StoredCredential> clear() {
        return this;
    }

    @Override
    public DataStore<StoredCredential> delete(String key){
        return this;
    }
}