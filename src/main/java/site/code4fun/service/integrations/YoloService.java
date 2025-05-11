package site.code4fun.service.integrations;

import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import site.code4fun.ApplicationProperties;
import site.code4fun.service.ObjectDetect;

@Service
@Slf4j
@Lazy
@SuppressWarnings("all")
public class YoloService implements ObjectDetect {
  private final RestTemplate restTemplate;

  public YoloService( ApplicationProperties applicationProperties) {
    this.restTemplate =  new RestTemplateBuilder().rootUri(applicationProperties.getYoloDomain()).build();
  }

  @SneakyThrows
  public Map<?, ?> identifyPersons(MultipartFile inputImage){

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("image", new ByteArrayResource(inputImage.getBytes()) {
      @Override
      public String getFilename() {
        return inputImage.getOriginalFilename();
      }
    });
    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:5001/detect-faces", requestEntity, Map.class);
    if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
      log.info("No faces detected by YOLOv11");
      return new HashMap<>();
    }
    return response.getBody();
  }
}
