package site.code4fun.service;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.code4fun.constant.ProcessingStatus;
import site.code4fun.constant.SearchOperator;
import site.code4fun.model.Attachment;
import site.code4fun.model.FaceDataEntity;
import site.code4fun.model.dto.SearchCriteria;
import site.code4fun.model.request.FormMetaData;
import site.code4fun.repository.SearchSpecification;
import site.code4fun.repository.jpa.FaceDataRepository;

import java.util.List;
import site.code4fun.service.face.RecognitionService;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@Slf4j
@RequiredArgsConstructor
@Lazy
public class FaceRecognitionService extends AbstractBaseService<FaceDataEntity, Long> {

  private final AttachmentService attachmentService;

  @Qualifier("awsRekognition")
  private final RecognitionService cloudRecognitionService;

  @Getter(AccessLevel.PROTECTED)
  private final FaceDataRepository repository;

  @Transactional(rollbackFor = Exception.class)
  public FaceDataEntity addUserToDataset(final MultipartFile file, FormMetaData info) {
//    FormMetaData res = cloudRecognitionService.addUserToDataset(file, info);

    List<Attachment> lst = attachmentService.upload(new MultipartFile[]{file});
    FaceDataEntity faceData = new FaceDataEntity();
    faceData.setThumbnail(lst.get(0).getLink());
    faceData.setName(info.getName());
    faceData.setContent(info.getContent());
    faceData.setStatus(ProcessingStatus.SUCCESS);
    faceData.setSrcUrl(info.getSrcUrl());
    faceData.setFbId(info.getFbId());
//    faceData.setProviderId(res.getProviderId());

    return getRepository().save(faceData);
  }

  @SneakyThrows
  public synchronized void deleteRemoteDataset() {
    getRepository().deleteAll();
    cloudRecognitionService.deleteRemoteDataset();
  }

  @SneakyThrows
  public synchronized Object compareTwoFace(MultipartFile face1, MultipartFile face2) {
    return Map.ofEntries(
        Map.entry("isMatch", cloudRecognitionService.compareTwoFace(face1, face2))
    );
  }

  @SneakyThrows
  public synchronized FaceDataEntity findFaceInDataset(final MultipartFile file) {
    FormMetaData info = cloudRecognitionService.findFaceInDataset(file);
    if (isNotBlank(info.getProviderId())) {
      return getRepository().findByProviderId(info.getProviderId()).get(0);
    }
    return new FaceDataEntity();
  }

  @Override
  public void delete(Long id) {
    FaceDataEntity faceData = getById(id);
    cloudRecognitionService.deleteUserFromDataset(faceData.getProviderId());
    super.delete(id);
  }


  @Override
  protected Specification<FaceDataEntity> createSpecification(List<SearchCriteria> criteriaList,
      String queryString) {
    Specification<FaceDataEntity> spec = null;

    if (isNotBlank(queryString)) {
      spec = new SearchSpecification<>(
          new SearchCriteria("name", SearchOperator.EQUAL, queryString));
      spec = spec.or(new SearchSpecification<>(
          new SearchCriteria("providerId", SearchOperator.EQUAL, queryString)));
    }

    for (SearchCriteria criteria : criteriaList) {
      spec = (spec == null) ? new SearchSpecification<>(criteria)
          : spec.and(new SearchSpecification<>(criteria));
    }

    return spec;
  }

}
