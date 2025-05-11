package site.code4fun.service.storage;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import site.code4fun.ApplicationProperties;
import site.code4fun.constant.Status;
import site.code4fun.model.Attachment;
import site.code4fun.model.mapper.AttachmentMapper;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Lazy
@Order(2)
public class AmazonS3 implements CloudStorage {

    private final ApplicationProperties properties;
    private final AttachmentMapper mapper;
    private S3Client s3Client;

    @Override
    @SneakyThrows
    public Attachment upload(MultipartFile file, String name, String contentType) {
        try {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(properties.getBucketName())
                    .contentType(contentType)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .key(name)
                    .build();

            getS3Client().putObject(objectRequest, RequestBody.fromBytes(file.getBytes()));
        }catch (S3Exception s3Exception){
            log.error(s3Exception.getMessage());
            tryCreateBucket();
        }

        Attachment attachment = new Attachment();
        attachment.setStorageId(name);
        attachment.setStorageProvider(getStorageProvider());
        attachment.setContentType(contentType);
        attachment.setSize(file.getSize());
        attachment.setName(file.getOriginalFilename());
        attachment.setLink(buildObjectUrl(name));
        attachment.setStatus(Status.ACTIVE);
        return attachment;
    }

    @Override
    public List<Attachment> getAll() {
        ListObjectsRequest listObjects = ListObjectsRequest
                .builder()
                .bucket(properties.getBucketName())
                .build();
        List<Attachment> lst = new ArrayList<>();

        try {
            ListObjectsResponse res = getS3Client().listObjects(listObjects);
            for (S3Object object : res.contents()) {
                Attachment atm = mapper.amazonS3ObjectToEntity(object);
                atm.setLink(buildObjectUrl(atm.getName()));
                atm.setStorageProvider(getStorageProvider());
                lst.add(atm);
            }
        }catch (NoSuchBucketException noSuchBucketException){
            tryCreateBucket();
        }

        return lst;
    }

    @Override
    public void delete(Attachment item) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(properties.getBucketName())
                .key(item.getStorageId())
                .build();

        getS3Client().deleteObject(deleteObjectRequest);
    }
    public void tryCreateBucket() {
        try(S3Waiter s3Waiter = getS3Client().waiter()){

            CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                    .bucket(properties.getBucketName())
                    .build();

            s3Client.createBucket(bucketRequest);
            HeadBucketRequest bucketRequestWait = HeadBucketRequest.builder()
                    .bucket(properties.getBucketName())
                    .build();

            WaiterResponse<HeadBucketResponse> waiterResponse = s3Waiter.waitUntilBucketExists(bucketRequestWait);
            waiterResponse.matched().response().ifPresent(x -> log.info(x.toString()));
            log.info("{} is ready", properties.getBucketName());

        } catch (S3Exception e) {
            log.error(e.awsErrorDetails().errorMessage());
        }
    }

    private S3Client getS3Client(){
        if (s3Client == null) {
            buildS3Client();
        }
        return s3Client;
    }

    private void buildS3Client(){
        AwsCredentialsProvider credentialsProvider =  StaticCredentialsProvider.create(AwsBasicCredentials.create(properties.getAccessKeyId(), properties.getSecretAccessKey()));
        s3Client = S3Client
                .builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    private String buildObjectUrl(String fileName){
        return String.format("https://%s.s3.amazonaws.com/%s", properties.getBucketName(), fileName);
    }
}
