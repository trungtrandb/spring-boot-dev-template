package site.code4fun.service.face;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.BoundingPoly;
import com.google.cloud.vision.v1.FaceAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.Vertex;
import com.google.protobuf.ByteString;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.code4fun.service.ObjectDetect;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCloudVision implements ObjectDetect {

  @SneakyThrows
  @Override
  public List<String> identifyPersons(MultipartFile file){
    List<String> base64CroppedFaces = new ArrayList<>();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (ImageAnnotatorClient visionClient = ImageAnnotatorClient.create()) {
      // Builds the image annotation request
      List<AnnotateImageRequest> requests = new ArrayList<>();

      ByteString imgBytes = ByteString.copyFrom(file.getBytes());
      Image img = Image.newBuilder().setContent(imgBytes).build();
      Feature feat = Feature.newBuilder().setType(Feature.Type.FACE_DETECTION).build();
      AnnotateImageRequest request =
          AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
      requests.add(request);

      // Performs label detection on the image file
      BatchAnnotateImagesResponse response = visionClient.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = response.getResponsesList();

      if (responses.isEmpty() || responses.get(0).getError().getCode() != 0) {
        System.err.println("Error during Vision API call: " +
            (responses.isEmpty() ? "No response" : responses.get(0).getError().getMessage()));
        return base64CroppedFaces; // Return empty list on error
      }

      AnnotateImageResponse res = responses.get(0);
      if (res.getFaceAnnotationsList().isEmpty()) {
        System.out.println("No faces detected in the image.");
        return base64CroppedFaces;
      }

      System.out.println("Detected " + res.getFaceAnnotationsCount() + " face(s).");

      // Load the original image for cropping
      BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
      if (originalImage == null) {
        System.err.println("Could not read original image for cropping.");
        return base64CroppedFaces;
      }

      int faceIndex = 0;
      for (FaceAnnotation annotation : res.getFaceAnnotationsList()) {
        BoundingPoly poly = annotation.getBoundingPoly();
        List<Vertex> vertices = poly.getVerticesList();

        // Get bounding box coordinates
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

        for (Vertex vertex : vertices) {
          minX = Math.min(minX, vertex.getX());
          minY = Math.min(minY, vertex.getY());
          maxX = Math.max(maxX, vertex.getX());
          maxY = Math.max(maxY, vertex.getY());
        }

        // Ensure coordinates are within image bounds and valid
        minX = Math.max(0, minX);
        minY = Math.max(0, minY);
        maxX = Math.min(originalImage.getWidth(), maxX);
        maxY = Math.min(originalImage.getHeight(), maxY);

//        int faceWidth = Math.min(originalImage.getWidth(), maxX) - minX;
//        int faceHeight = Math.min(originalImage.getHeight(), maxY) - minY;

        // Ensure width and height are positive
//        faceWidth = Math.max(0, faceWidth);
//        faceHeight = Math.max(0, faceHeight);

//        FaceInfo currentFaceInfo = new FaceInfo(
//            minX,
//            minY,
//            faceWidth,
//            faceHeight,
//            annotation.getDetectionConfidence()
//        );

        int cropWidth = maxX - minX;
        int cropHeight = maxY - minY;

        if (cropWidth <= 0 || cropHeight <= 0) {
          System.err.println("Skipping invalid crop dimensions for face " + (faceIndex + 1) +
              ": width=" + cropWidth + ", height=" + cropHeight);
          faceIndex++;
          continue;
        }

        try {
          // Crop the face
          BufferedImage croppedFace = originalImage.getSubimage(minX, minY, cropWidth, cropHeight);

          // Convert cropped image to byte array (PNG format)
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          ImageIO.write(croppedFace, "png", baos); // Can use "jpeg" as well
          byte[] croppedImageBytes = baos.toByteArray();

          // Encode to Base64
          String base64String = Base64.getEncoder().encodeToString(croppedImageBytes);
          base64CroppedFaces.add(base64String);

          System.out.println("  Cropped face " + (faceIndex + 1) + " and encoded to Base64.");

          // Optional: Save cropped face to a file for verification
           File outputfile = new File("cropped_face_" + faceIndex + ".png");
           ImageIO.write(croppedFace, "png", outputfile);

        } catch (java.awt.image.RasterFormatException e) {
          log.error("Error cropping face {}. Subimage coordinates might be out of bounds.",
              faceIndex + 1);
          log.error("Crop details: x={}, y={}, width={}, height={}, Image dims: w={}, h={}", minX,
              minY, cropWidth, cropHeight, originalImage.getWidth(), originalImage.getHeight());
          // e.printStackTrace(); // Uncomment for full stack trace
        } catch (Exception e) {
          log.error("Error writing cropped face {} to byte stream: {}", faceIndex + 1,
              e.getMessage());
        }
        faceIndex++;
      }
    }

    return base64CroppedFaces;
  }
}