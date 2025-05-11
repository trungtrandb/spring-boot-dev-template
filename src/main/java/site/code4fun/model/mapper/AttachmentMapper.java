package site.code4fun.model.mapper;

import com.google.api.services.drive.model.File;
import com.google.cloud.storage.Blob;
import io.minio.messages.Item;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import site.code4fun.constant.Status;
import site.code4fun.model.Attachment;
import site.code4fun.model.dto.AttachmentDTO;
import site.code4fun.model.dto.FileManagerDirectoryContent;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.Collections;

import static site.code4fun.util.FileUtils.getFileExtension;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,  componentModel = "spring")
public interface AttachmentMapper extends BaseMapper<Attachment, AttachmentDTO>{

    default Attachment googleCloudStorageToEntity(Blob blob){
        Attachment atm = new Attachment();
        atm.setName(blob.getName());
        atm.setSize(blob.getSize());
        atm.setStatus(Status.ACTIVE);
        atm.setContentType(blob.getContentType());
        atm.setStorageId(blob.getBlobId().getName());
        return atm;
    }

    default FileManagerDirectoryContent entityToFileManagerContent(Attachment blob){
        return FileManagerDirectoryContent.builder()
                .name(blob.getName())
                .id(blob.getId())
                .names(Collections.singletonList(blob.getName()))
                .type(getFileExtension(blob.getContentType()))
                .isFile(true)
                .size(blob.getSize() != null ? blob.getSize() : 0)
                .hasChild(false)
                .filterPath("/")
                .link(blob.getLink())
//                .dateModified(formatDate(blob.getCreated()))
                .build();
    }

    default Attachment amazonS3ObjectToEntity(S3Object s3Object){
        Attachment atm = new Attachment();
        atm.setName(s3Object.key());
        atm.setSize(s3Object.size());
        atm.setStatus(Status.ACTIVE);
        atm.setStorageId(s3Object.key());
        return atm;
    }

    default Attachment minIoItemToEntity(Item item){
        Attachment atm = new Attachment();
        atm.setName(item.objectName());
        atm.setSize(item.size());
        atm.setStatus(Status.ACTIVE);
        atm.setStorageId(item.objectName());
        return atm;
    }

    default Attachment googleDriveToEntity(File file){
        Attachment atm = new Attachment();
        atm.setName(file.getName());
        atm.setSize(file.getSize());
        atm.setStatus(Status.ACTIVE);
        atm.setContentType(file.getMimeType());
        atm.setLink(file.getWebContentLink());
        atm.setStorageId(file.getId());
        atm.setSize(file.getSize());
        return atm;
    }
}