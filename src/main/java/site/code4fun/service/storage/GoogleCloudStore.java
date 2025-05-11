package site.code4fun.service.storage;

import com.google.api.gax.paging.Page;
import com.google.cloud.Identity;
import com.google.cloud.Policy;
import com.google.cloud.storage.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import site.code4fun.constant.Status;
import site.code4fun.model.mapper.AttachmentMapper;
import site.code4fun.model.Attachment;
import site.code4fun.service.google.GoogleServiceAccountProperties;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@Primary
@Order(1)
@RequiredArgsConstructor
@Lazy
@SuppressWarnings("unused")
public class GoogleCloudStore implements CloudStorage {

  private final GoogleServiceAccountProperties config;
  private Bucket bucket;
  private final AttachmentMapper mapper;

  @Override
  @SneakyThrows
  public Attachment upload(MultipartFile file, String name, String contentType) {
    Blob blob = getBucket().create(name, file.getBytes(), contentType);

    return getAttachment(file, blob);
  }

  @SneakyThrows
  @Override
  public Attachment upload(MultipartFile file, String name, String folderPath, String contentType) {

    if (folderPath == null) {
      folderPath = "";
    }

    // Ensure folder path ends with a slash and is properly formatted
    String normalizedFolder =
        folderPath.isEmpty() ? "" : folderPath.endsWith("/") ? folderPath : folderPath + "/";
    String gcsObjectName = normalizedFolder + name;

    // Create BlobId and BlobInfo
    BlobId blobId = BlobId.of(config.getBucketName(), gcsObjectName);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
        .setContentType(contentType)
        .build();

    Blob blob = getStorage().create(blobInfo, file.getBytes());

    return getAttachment(file, blob);
  }

  @NotNull
  private Attachment getAttachment(MultipartFile file, Blob blob) {
    Attachment attachment = new Attachment();
    attachment.setStorageProvider(getStorageProvider());
    attachment.setStorageId(blob.getBlobId().getName());
    attachment.setContentType(blob.getContentType());
    attachment.setSize(blob.getSize());
    attachment.setName(file.getOriginalFilename());
    attachment.setLink(blob.getMediaLink());
    attachment.setStatus(Status.ACTIVE);
    return attachment;
  }

  @Override
  public List<Attachment> getAll() {
    Page<Blob> blobs = getBucket().list();
    List<Attachment> lst = new ArrayList<>();
    for (Blob blob : blobs.iterateAll()) {
      Attachment atm = mapper.googleCloudStorageToEntity(blob);
      atm.setLink(blob.getMediaLink());
      atm.setStorageProvider(getStorageProvider());
      lst.add(atm);
    }
    return lst;
  }

  @Override
  public void delete(Attachment item) {
    Blob blob = getBucket().get(item.getStorageId());
    if (blob == null) {
      log.error("The object {} with id {} wasn't found in {}", item.getName(), item.getStorageId(),
          config.getBucketName());
      return;
    }
    blob.delete();
  }

  public String generateSignedUrlForDownload(Attachment attachment) {
    Storage storage = getStorage();

    BlobInfo blobInfo = BlobInfo.newBuilder(
        BlobId.of(config.getBucketName(), attachment.getStorageId())).build();

    URL url = storage.signUrl(blobInfo, 15, TimeUnit.MINUTES,
        Storage.SignUrlOption.withV4Signature());

    log.info("Generated GET signed URL: {}", url);
    return url.toString();
  }


  public String generateSignedUrlForUpload(String objectName, String contentType) { //PresignedURL
    Storage storage = getStorage();
    BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(config.getBucketName(), objectName)).build();
    Map<String, String> extensionHeaders = new HashMap<>();
    extensionHeaders.put("Content-Type", contentType);

    URL url = storage.signUrl(
        blobInfo,
        15,
        TimeUnit.MINUTES,
        Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
        Storage.SignUrlOption.withExtHeaders(extensionHeaders),
        Storage.SignUrlOption.withV4Signature());

    log.info("curl -X PUT -H 'Content-Type: application/octet-stream' --upload-file my-file '{}'",
        url);
    return url.toString();
  }

  @SneakyThrows
  @Override
  public String createFolder(String folderPath) {
    if (folderPath == null || folderPath.trim().isEmpty()) {
      throw new IllegalArgumentException("Folder path cannot be null or empty");
    }

    String normalizedFolderPath = folderPath.endsWith("/") ? folderPath : folderPath + "/";

    BlobId blobId = BlobId.of(config.getBucketName(), normalizedFolderPath);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
        .setContentType("application/x-directory")
        .build();

    getStorage().create(blobInfo, new byte[0]);
    return folderPath;
  }


  @SneakyThrows
  private Bucket getBucket() {
      if (bucket != null) {
          return bucket;
      }
    Storage storage = getStorage();

    Page<Bucket> buckets = storage.list();
    for (Bucket bk : buckets.iterateAll()) {
      if (bk.getName().equalsIgnoreCase(config.getBucketName())) {
        bucket = bk;
        return bucket;
      }
    }

    buildBucket(storage);
    return bucket;
  }

  private void buildBucket(Storage storage) {
    final String bucketName = config.getBucketName();
    log.warn("Not found google storage bucket name {}, creating new one", bucketName);

    BucketInfo.IamConfiguration iamConfiguration = BucketInfo.IamConfiguration.newBuilder()
        .setIsUniformBucketLevelAccessEnabled(true)
        .build();
    this.bucket = storage.create(
        BucketInfo.newBuilder(bucketName)
            .setLocation(config.getDefaultRegionName())
            .setIamConfiguration(iamConfiguration)
            .build()
    );

    Policy originalPolicy = storage.getIamPolicy(bucketName);
    storage.setIamPolicy(
        bucketName,
        originalPolicy
            .toBuilder()
            .addIdentity(StorageRoles.legacyObjectReader(),
                Identity.allUsers()) // All users can view
            .build());
  }

  private Storage getStorage() {
    return StorageOptions.newBuilder()
        .setCredentials(config.getCredential())
        .setProjectId(config.getProjectId()).build().getService();
  }
}
