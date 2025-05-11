package site.code4fun.service.face;

import org.springframework.web.multipart.MultipartFile;
import site.code4fun.model.request.FormMetaData;

public interface RecognitionService {

  FormMetaData addUserToDataset(final MultipartFile file, FormMetaData info);

  default void deleteUserFromDataset(String faceId){
    throw new UnsupportedOperationException("Not supported yet.");
  }

  void deleteRemoteDataset();

  float compareTwoFace(MultipartFile face1, MultipartFile face2);
  FormMetaData findFaceInDataset(final MultipartFile file);
}