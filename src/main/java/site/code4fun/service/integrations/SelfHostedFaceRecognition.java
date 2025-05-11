package site.code4fun.service.integrations;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import site.code4fun.ApplicationProperties;
import site.code4fun.model.request.FormMetaData;
import site.code4fun.service.face.RecognitionService;

@Service
@RequiredArgsConstructor
@Slf4j
@Lazy
public class SelfHostedFaceRecognition implements RecognitionService {
  private final ApplicationProperties properties;
  private final Gson gson;

  @Override
  public FormMetaData addUserToDataset(MultipartFile file, FormMetaData info) {
    try {
      log.info("Sending request to {} with data : {}", "/", info);
      LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
      map.add("file", file.getResource());
      map.add("id", info.getId());
      String res = executeRequest("/", map);
      gson.fromJson(res, FormMetaData.class);
      info.setProvider(info.getId().toString());
    }catch (Exception e){
      log.error("could not add user to dataset");
    }
    return info;
  }

  @Override
  public void deleteRemoteDataset() {
    getTemplate().delete("/", buildRequestEntity(new LinkedMultiValueMap<>()), String.class);
  }

  @Override
  public float compareTwoFace(MultipartFile face1, MultipartFile face2) {
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("file", face1.getResource());
    map.add("file1", face2.getResource());
    log.info("Sending request to {} with files {}, {}", "/compare", face1.getOriginalFilename(), face2.getOriginalFilename());
    String res = executeRequest("/compare", map); // {isMatch: true}
    log.info("Compare result: {}", res);
    return 0f;
  }

  @Override
  public FormMetaData findFaceInDataset(MultipartFile file) {
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("file", file.getResource());
    log.info("Sending request to {} with file name: {}", "/find", file.getOriginalFilename());
    String res = executeRequest("/find", map);
    return gson.fromJson(res, FormMetaData.class);
  }


  private synchronized String executeRequest(String url, LinkedMultiValueMap<String, Object> map) {
    String response;
    try {
      response = getTemplate().postForObject(url, buildRequestEntity(map), String.class);
      log.info("URL {} response: {}", url, response);
    } catch (HttpStatusCodeException e) {
      log.error(e.getMessage());
      response = e.getResponseBodyAsString();
    } catch (Exception e) {
      log.error(e.getMessage());
      response = e.getMessage();
    }
    return response;
  }

  private HttpEntity<LinkedMultiValueMap<String, Object>> buildRequestEntity(LinkedMultiValueMap<String, Object> map) {
    HttpHeaders headers = new HttpHeaders();
    return new HttpEntity<>(map, headers);
  }

  private RestTemplate getTemplate(){
    return new RestTemplateBuilder()
        .rootUri(properties.getFaceDomain())
        .defaultHeader("Content-Type", MediaType.MULTIPART_FORM_DATA.getType())
        .build();
  }
}
