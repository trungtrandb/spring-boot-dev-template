package site.code4fun.service.face;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.code4fun.ApplicationProperties;
import site.code4fun.model.request.FormMetaData;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.Attribute;
import software.amazon.awssdk.services.rekognition.model.BoundingBox;
import software.amazon.awssdk.services.rekognition.model.CompareFacesMatch;
import software.amazon.awssdk.services.rekognition.model.CompareFacesRequest;
import software.amazon.awssdk.services.rekognition.model.CompareFacesResponse;
import software.amazon.awssdk.services.rekognition.model.ComparedFace;
import software.amazon.awssdk.services.rekognition.model.CreateCollectionRequest;
import software.amazon.awssdk.services.rekognition.model.CreateCollectionResponse;
import software.amazon.awssdk.services.rekognition.model.DeleteCollectionRequest;
import software.amazon.awssdk.services.rekognition.model.DeleteCollectionResponse;
import software.amazon.awssdk.services.rekognition.model.DeleteFacesRequest;
import software.amazon.awssdk.services.rekognition.model.FaceMatch;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.IndexFacesRequest;
import software.amazon.awssdk.services.rekognition.model.IndexFacesResponse;
import software.amazon.awssdk.services.rekognition.model.ListCollectionsRequest;
import software.amazon.awssdk.services.rekognition.model.ListCollectionsResponse;
import software.amazon.awssdk.services.rekognition.model.QualityFilter;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;
import software.amazon.awssdk.services.rekognition.model.SearchFacesByImageRequest;
import software.amazon.awssdk.services.rekognition.model.SearchFacesByImageResponse;

@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("unused")
@Lazy
public class AwsRekognition implements RecognitionService {

  private final ApplicationProperties properties;
  private static final String COLLECTION_ID = "test-collection";
  private static final AtomicBoolean isExistCollection = new AtomicBoolean(false);

  @PostConstruct
  public void init() {
    checkExistCollection();
    if (!isExistCollection.get()) {
      isExistCollection.set(createCollection());
    }
  }


  public void deleteCollection(String collectionId) {
    try {
      DeleteCollectionRequest request = DeleteCollectionRequest.builder()
          .collectionId(collectionId)
          .build();

      DeleteCollectionResponse deleteCollectionResponse = getRekClient().deleteCollection(request);
      log.info("Delete collection {} with http status: {}", collectionId,
          deleteCollectionResponse.statusCode().toString());

    } catch (RekognitionException e) {
      log.error(e.getMessage());
    }
  }

  @Override
  @SneakyThrows
  public FormMetaData addUserToDataset(final MultipartFile file, FormMetaData info) {
    Image targetImage = Image.builder().bytes(SdkBytes.fromByteArray(file.getBytes())).build();

    IndexFacesRequest facesRequest = IndexFacesRequest.builder()
        .collectionId(COLLECTION_ID)
        .image(targetImage)
        .maxFaces(1)
        .qualityFilter(QualityFilter.AUTO)
        .detectionAttributes(Attribute.DEFAULT)
        .build();

    IndexFacesResponse facesResponse = getRekClient().indexFaces(facesRequest);
    log.info("Faces indexed: {}", facesResponse.faceRecords());
    info.setProviderId(facesResponse.faceRecords().get(0).face().faceId());
    return info;
  }

  @Override
  public void deleteUserFromDataset(String faceId) {
    try {
      DeleteFacesRequest deleteFacesRequest = DeleteFacesRequest.builder()
          .collectionId(COLLECTION_ID)
          .faceIds(faceId)
          .build();

      getRekClient().deleteFaces(deleteFacesRequest);
      log.info("The face was deleted from the collection.");

    } catch (RekognitionException e) {
      log.error(e.getMessage());
    }
  }

  @Override
  @SneakyThrows
  public void deleteRemoteDataset() {
    deleteCollection(COLLECTION_ID);
  }

  @Override
  @SneakyThrows
  public float compareTwoFace(MultipartFile face1, MultipartFile face2) {
    Image sourceImage = Image.builder().bytes(SdkBytes.fromByteArray(face1.getBytes())).build();
    Image targetImage = Image.builder().bytes(SdkBytes.fromByteArray(face2.getBytes())).build();
    Float similarityThreshold = 80F;
    CompareFacesRequest facesRequest = CompareFacesRequest.builder()
        .sourceImage(sourceImage)
        .targetImage(targetImage)
        .similarityThreshold(similarityThreshold)
        .build();

    CompareFacesResponse compareFacesResult = getRekClient().compareFaces(facesRequest);
    List<CompareFacesMatch> faceDetails = compareFacesResult.faceMatches();

    for (CompareFacesMatch match : faceDetails) {
      ComparedFace face = match.face();
      BoundingBox position = face.boundingBox();
      log.info("Face at {} {} matches with {}% confidence.", position.left().toString(),
          position.top(), face.confidence().toString());
      return match.face().confidence();

    }
    return 0f;
  }

  @Override
  @SneakyThrows
  public FormMetaData findFaceInDataset(MultipartFile file) {
    var res = new FormMetaData();
    try {
      SdkBytes sourceBytes = SdkBytes.fromByteArray(file.getBytes());
      Image souImage = Image.builder()
          .bytes(sourceBytes)
          .build();

      SearchFacesByImageRequest request = SearchFacesByImageRequest.builder()
          .image(souImage)
          .maxFaces(10)
          .faceMatchThreshold(70F)
          .collectionId(COLLECTION_ID)
          .build();

      SearchFacesByImageResponse imageResponse = getRekClient().searchFacesByImage(request);
      List<FaceMatch> faceImageMatches = imageResponse.faceMatches();
      for (FaceMatch face : faceImageMatches) {
        log.info(face.toString());
        res.setProviderId(face.face().faceId());
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return res;
  }


  // -------------   PRIVATE -------------
  private RekognitionClient getRekClient() {
    AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
       AwsBasicCredentials.create(properties.getAccessKeyId(), properties.getSecretAccessKey())
    );
    return RekognitionClient
        .builder()
        .region(Region.of(properties.getRegion()))
        .credentialsProvider(credentialsProvider)
        .build();
  }

  private void checkExistCollection() {
    try {
      ListCollectionsRequest listCollectionsRequest = ListCollectionsRequest.builder()
          .maxResults(10)
          .build();

      ListCollectionsResponse response = getRekClient().listCollections(listCollectionsRequest);
      List<String> collectionIds = response.collectionIds();
      if (collectionIds.contains(COLLECTION_ID)) {
        isExistCollection.set(true);
      }
    } catch (RekognitionException e) {
      System.out.println(e.getMessage());
      System.exit(1);
    }
  }

  private boolean createCollection() {
    try {
      CreateCollectionRequest request = CreateCollectionRequest.builder()
          .collectionId(AwsRekognition.COLLECTION_ID)
          .build();

      CreateCollectionResponse collectionResponse = getRekClient().createCollection(request);
      log.info("New CollectionArn: {}", collectionResponse.collectionArn());
      return true;
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return false;
  }
}

