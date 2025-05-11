package site.code4fun.controller.admin;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.constant.CacheName;
import site.code4fun.model.Attachment;
import site.code4fun.model.dto.AttachmentDTO;
import site.code4fun.model.mapper.AttachmentMapper;
import site.code4fun.model.request.DeleteRequest;
import site.code4fun.service.AttachmentService;

import java.util.List;

@Getter
@RestController
@RequestMapping(AppEndpoints.ADMIN_API_PREFIX + AppEndpoints.FILES_ENDPOINT)
@RequiredArgsConstructor
@Lazy
public class AdminFileController extends AdminAbstractBaseController<Attachment, AttachmentDTO, Long> {

    private final AttachmentService service;
    private final AttachmentMapper mapper;
    @PutMapping()
    @CacheEvict(value = {CacheName.FILE_PAGING_ALL, CacheName.FILE_BY_ID}, allEntries = true)
    @Transactional
    @Override
    public AttachmentDTO create(@RequestBody AttachmentDTO request) {
        Attachment entity = service.update(mapper.dtoToEntity(request));
        return mapper.entityToDto(entity);
    }

    @Override
    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CacheEvict(value = {CacheName.FILE_PAGING_ALL, CacheName.FILE_BY_ID}, allEntries = true)
    @Transactional
    public void deleteAllById(@RequestBody DeleteRequest<Long> request){
        service.deleteByIds(request.getId());
    }

    @GetMapping("/content-types")
    public List<String> getAllContentType(){
        return service.getAllContentType();
    }

    @GetMapping("/storage-providers")
    public List<String> getCloudStorageProvider(){
        return service.getAllStorageProvider();
    }

    @GetMapping("/storage-providers/{provider}/sync")
    @Transactional
    public ResponseEntity<List<Attachment>> sync(@PathVariable String provider){
        return ResponseEntity.ok().body(service.sync(provider));
    }

}
