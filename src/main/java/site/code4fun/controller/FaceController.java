package site.code4fun.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.constant.CacheName;
import site.code4fun.constant.MimeType;
import site.code4fun.exception.DuplicateResourceException;
import site.code4fun.model.dto.FaceDataDTO;
import site.code4fun.model.mapper.FaceDataMapper;
import site.code4fun.model.request.DeleteRequest;
import site.code4fun.model.request.FormMetaData;
import site.code4fun.service.FaceRecognitionService;

import java.util.List;
import java.util.Map;
// import site.code4fun.service.integrations.YoloService;
import site.code4fun.service.face.GoogleCloudVision;
@RestController
@RequestMapping(AppEndpoints.FACES_ENDPOINT)
@RequiredArgsConstructor
@Lazy
@Transactional(readOnly = true)
public class FaceController {
    private final FaceDataMapper mapper;
    private final FaceRecognitionService service;
    private final GoogleCloudVision yoloService;

    @PostMapping(consumes = {"multipart/form-data"})
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional(rollbackFor = DuplicateResourceException.class)
    public ResponseEntity<FaceDataDTO> addFaceWithNameToDataset(@RequestPart("files") final MultipartFile[] files, @RequestPart FormMetaData info) {
        FaceDataDTO dto = mapper.entityToDto(service.addUserToDataset(files[0], info));
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/preview-cropped")
    public ResponseEntity<String> previewCropped(@RequestPart("files") MultipartFile[] files) {
        List<String> croppedFaces = yoloService.identifyPersons(files[0]); // Modified to return cropped_faces
        StringBuilder html = new StringBuilder("<html><body>");
        for (String base64 : croppedFaces) {
            html.append("<img src='data:image/jpeg;base64,").append(base64).append("'/><br>");
        }
        html.append("</body></html>");
        return ResponseEntity.ok(html.toString());
    }

    @PostMapping("/find")
    public ResponseEntity<FaceDataDTO> findByFaceInDataSet(@RequestPart("files") final MultipartFile[] files) {
        return ResponseEntity.ok(mapper.entityToDto(service.findFaceInDataset(files[0])));
    }

    @PostMapping("/compare")
    public ResponseEntity<?> compareTwoFace(@RequestPart("files") final MultipartFile[] files) {
        return ResponseEntity.ok(service.compareTwoFace(files[0], files[1]));
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> export(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MimeType.APPLICATION_XLSX);
        headers.setAccessControlExposeHeaders(List.of("Content-Disposition"));
        headers.add("Content-Disposition", "attachment; filename=faces.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .body(service.export());
    }

    @GetMapping
    @Transactional
    public Page<FaceDataDTO> getAllPaging(@RequestParam Map<String, String> mapRequest) {
        return service.getPaging(mapRequest).map(mapper::entityToDto);
    }

    @GetMapping("/{id}")
    public FaceDataDTO getById(@PathVariable long id){
        return mapper.entityToDto(service.getById(id));
    }

//    @PostMapping("/{id}/process")
//    @Transactional
//    public FaceDataDTO doProcess(@PathVariable long id){
//        return mapper.entityToDto(service.process(id));
//    }

    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CacheEvict(value = {CacheName.FILE_PAGING_ALL, CacheName.FILE_BY_ID}, allEntries = true)
    @Transactional
    public void deleteAllById(@RequestBody DeleteRequest<Long> request){
        service.deleteByIds(request.getId());
    }

    @DeleteMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void deleteRemoteDataset(){
        service.deleteRemoteDataset();
    }
}
