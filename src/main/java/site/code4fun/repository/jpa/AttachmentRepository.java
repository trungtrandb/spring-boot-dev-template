package site.code4fun.repository.jpa;

import org.springframework.data.jpa.repository.Query;
import site.code4fun.model.Attachment;

import java.util.List;

public interface AttachmentRepository extends BaseRepository<Attachment, Long> {

    @Query(value = "Select a.contentType from Attachment a group by a.contentType")
    List<String> findAllContentType();

    List<Attachment> findAllByStorageProvider(String provider);

    List<Attachment> findByName(String name);
}

