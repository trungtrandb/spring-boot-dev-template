package site.code4fun.service;

import org.springframework.web.multipart.MultipartFile;

public interface ObjectDetect {
  Object identifyPersons(MultipartFile inputImage);
}
