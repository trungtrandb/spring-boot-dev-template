package site.code4fun.service.storage;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.code4fun.ApplicationProperties;
import site.code4fun.constant.Status;
import site.code4fun.model.Attachment;
import site.code4fun.model.mapper.AttachmentMapper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class MinIo implements CloudStorage{
    private final ApplicationProperties properties;
    private MinioClient minioClient;
    private final AttachmentMapper mapper;

    private MinioClient getClient() {
        if (minioClient == null){
            minioClient = MinioClient.builder()
                    .endpoint(properties.getEndpoint())
                    .credentials(properties.getAccessKey(), properties.getSecretKey())
                    .build();
        }
        try{
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(properties.getMinIoBucketName()).build())) {
                log.info("Creating new bucket with name '{}'", properties.getMinIoBucketName());
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(properties.getMinIoBucketName()).build());
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return minioClient;
    }

    @Override
    @SneakyThrows
    public Attachment upload(MultipartFile file, String name, String contentType) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 7);

        getClient().putObject(
                PutObjectArgs.builder()
                        .bucket(properties.getMinIoBucketName())
                        .object(name)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .build());

        Attachment attachment = new Attachment();
        attachment.setStorageId(name);
        attachment.setStorageProvider(getStorageProvider());
        attachment.setContentType(contentType);
        attachment.setSize(file.getSize());
        attachment.setName(file.getOriginalFilename());
        attachment.setLink(getTempUrl(name));
        attachment.setStatus(Status.ACTIVE);
        attachment.setExpired(c.getTime());
        return attachment;
    }

    @Override
    @SneakyThrows
    public List<Attachment> getAll() {
        List<Attachment> lst = new ArrayList<>();
        try {
            Iterable<Result<Item>> results =
                    getClient().listObjects(ListObjectsArgs.builder().bucket(properties.getMinIoBucketName()).build());

            for (Result<Item> result : results) {
                Attachment attachment = mapper.minIoItemToEntity(result.get());
                attachment.setLink(getTempUrl(attachment.getStorageId()));
                attachment.setStorageProvider(getStorageProvider());
                lst.add(attachment);

            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return lst;
    }

    @Override
    @SneakyThrows
    public void delete(Attachment item) {
        getClient().removeObject(RemoveObjectArgs.builder().bucket(properties.getMinIoBucketName()).object(item.getName()).build());
    }

    @SneakyThrows
    private String getTempUrl(String objectName) {
        if (isEmpty(objectName)){
            log.warn("Skip create minIo Url, objectName is empty");
            return "";
        }
        return getClient().getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(properties.getMinIoBucketName())
                        .object(objectName)
                        .expiry(7, TimeUnit.DAYS) //max is 7 day
                        .build());
    }
}