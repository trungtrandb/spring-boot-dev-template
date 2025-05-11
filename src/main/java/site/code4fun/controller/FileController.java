package site.code4fun.controller;


import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.dto.AttachmentDTO;
import site.code4fun.model.dto.FileOperationRequest;
import site.code4fun.model.dto.FileOperationResponse;
import site.code4fun.model.mapper.AttachmentMapper;
import site.code4fun.model.request.FormMetaData;
import site.code4fun.service.AttachmentService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(AppEndpoints.FILES_ENDPOINT)
@RequiredArgsConstructor
@Lazy
@Transactional(readOnly = true)
public class FileController {
    private final AttachmentService service;
    private final AttachmentMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public List<AttachmentDTO> uploadFiles(@RequestPart("files") final MultipartFile[] files, @RequestPart(required = false) FormMetaData info) {
        return service.upload(files, info != null ? info.getProvider() : null).stream().map(mapper::entityToDto).toList();
    }

    @PostMapping("/upload")// Don't edit, this is CKEditor upload, can't change field name
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public List<AttachmentDTO> uploadFile(@RequestParam("upload") MultipartFile file) {
        return service.upload(new MultipartFile[]{file}).stream().map(mapper::entityToDto).toList();
    }

    @PostMapping("/syncfusion")
    @Transactional
    public FileOperationResponse amazonS3FileOperations(@RequestBody FileOperationRequest request) {
        return service.getFiles(request);
    }

    @GetMapping("/syncfusion")
    public ResponseEntity<?> getImage(@RequestParam String path, HttpServletResponse response) throws IOException {
        response.sendRedirect(service.getImage(path));
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).build();
    }

//    @PostMapping("/download")
//    public ResponseEntity<Resource> download(@RequestBody DownloadRequest request) {
//        try {
//            FileDownloadResponse response = blobService.download(request);
//            return ResponseEntity.ok()
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.getFileName() + "\"")
//                    .contentType(response.getContentType())
//                    .body(response.getResource());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

    @PostMapping("/syncfusion/upload")
    @SneakyThrows
    @Transactional
    public ResponseEntity<?> upload(@RequestParam("uploadFiles") MultipartFile[] files,
                                    @RequestParam String path,
                                    @RequestParam String action,
                                    @RequestParam(required = false) String filename) {
        return ResponseEntity.ok(service.upload(files[0]));
    }

}
