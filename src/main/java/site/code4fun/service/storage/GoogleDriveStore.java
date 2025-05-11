package site.code4fun.service.storage;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.code4fun.ApplicationProperties;
import site.code4fun.exception.ServiceException;
import site.code4fun.model.User;
import site.code4fun.constant.Status;
import site.code4fun.model.mapper.AttachmentMapper;
import site.code4fun.model.Attachment;
import site.code4fun.service.google.GoogleOauth2Properties;
import site.code4fun.util.SecurityUtils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@Order(3)
@RequiredArgsConstructor
@SuppressWarnings("all")
@Lazy
public class GoogleDriveStore implements CloudStorage {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final AttachmentMapper mapper;
    private final DataStoreFactory dataStoreFactory;
    private final GoogleOauth2Properties googleOauth2Properties;
    private final ApplicationProperties properties;

    @Override
    @SneakyThrows
    public Attachment upload(MultipartFile file, String name, String contentType) {
        File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(name);
        File uploadFile = getClient().files().create(fileMetadata, new InputStreamContent(file.getContentType(), new ByteArrayInputStream(file.getBytes()))).setFields("*").execute();

        String fileType = file.getContentType() != null ? file.getContentType() : "";
        Permission permission;
        if (fileType.contains("image")){
            permission = setPermission(PERMISSION_TYPE.ANYONE, PERMISSION_ROLE.READER);
        }else{
            permission = setPermission(PERMISSION_TYPE.DOMAIN, PERMISSION_ROLE.READER);
        }
        getClient().permissions().create(uploadFile.getId(), permission).execute();
        Attachment attachment = new Attachment();
        attachment.setContentType(file.getContentType());
        attachment.setSize(file.getSize());
        attachment.setName(file.getOriginalFilename());
        attachment.setLink(uploadFile.getWebContentLink());
        attachment.setStatus(Status.ACTIVE);
        attachment.setThumbnail(uploadFile.getThumbnailLink());
        attachment.setStorageId(uploadFile.getId());
        attachment.setStorageProvider(getStorageProvider());
        return attachment;
    }

    @SneakyThrows
    public File findById(String fileId) {
        return getClient().files().get(fileId).setFields("*").execute();
    }

    @Override
    public List<Attachment> getAll() {
        List<Attachment> res = new ArrayList<>();
        try {
            FileList result = getClient().files().list().setFields("nextPageToken, files(*)").execute();
            result.getFiles().forEach(file -> {
                Attachment attachment = mapper.googleDriveToEntity(file);
                attachment.setStorageProvider(getStorageProvider());
                res.add(attachment);
            });
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return res;
    }

    @Override
    public void delete(Attachment item) {
        try {
            getClient().files().delete(item.getStorageId()).execute();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private Permission setPermission(String type, String role) {
        Permission permission = new Permission();
        permission.setType(type);
        permission.setRole(role);
        if (type.equalsIgnoreCase(PERMISSION_TYPE.DOMAIN)){
            permission.setDomain(properties.getAdminDomain());
        }
        return permission;
    }

    @SneakyThrows
    private Credential getCredentials(final NetHttpTransport httpTransport, String userId) {
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY,
                googleOauth2Properties.getClientId(),
                googleOauth2Properties.getClientSecret(),
                Collections.singletonList( DriveScopes.DRIVE_FILE))
                .setDataStoreFactory(dataStoreFactory)
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();
        return flow.loadCredential(userId);
    }

    @SneakyThrows
    private Drive buildDriveForUserId(final String userId) { // NOSONAR
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport, userId)).setApplicationName("Appication Test").build();
    }

    @SneakyThrows
    private Drive getClient() {
        User currentUser = SecurityUtils.getUser();
        if (currentUser != null){
            String email = currentUser.getUsername();
            return buildDriveForUserId(email);
        }
        throw new ServiceException("User not found");
    }

    interface PERMISSION_ROLE {
        String OWNER = "owner";
        String ORGANIZER = "organizer";
        String FILE_ORGANIZER = "fileOrganizer";
        String WRITER = "writer";
        String COMMENTER = "commenter";
        String READER = "reader";
    }

    /*
     if type is 'user' or 'group', you must provide an emailAddress for the user or group.
     When type is 'domain', you must provide a domain.
    * */
    interface PERMISSION_TYPE {
        String USER = "user";
        String GROUP = "group";
        String DOMAIN = "domain";
        String ANYONE = "anyone";
    }

}