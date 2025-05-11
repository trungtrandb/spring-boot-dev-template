package site.code4fun.service.storage;

import org.springframework.web.multipart.MultipartFile;
import site.code4fun.model.Attachment;

import java.util.List;

public interface CloudStorage {

  Attachment upload(MultipartFile file, String fileName, String contentType);

  default Attachment upload(MultipartFile file, String fileName, String folderPath, String contentType){
    throw new UnsupportedOperationException();
  }

  List<Attachment> getAll();

  void delete(Attachment item);

  default String getStorageProvider() {
    return this.getClass().getSimpleName();
  }

  default String createFolder(String folderPath){
    throw new UnsupportedOperationException();
  }
}
