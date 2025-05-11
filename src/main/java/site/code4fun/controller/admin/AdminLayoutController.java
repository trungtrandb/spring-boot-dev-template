package site.code4fun.controller.admin;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import site.code4fun.model.LayoutTypeEntity;
import site.code4fun.model.dto.LayoutTypeDTO;
import site.code4fun.model.dto.ShopConfigDTO;
import site.code4fun.model.mapper.LayoutTypeMapper;
import site.code4fun.model.request.DeleteRequest;
import site.code4fun.service.LayoutService;

import static site.code4fun.constant.CacheName.SITE_TYPE;
import static site.code4fun.constant.CacheName.SITE_TYPES;

@Getter
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/layouts")
@Lazy
public class AdminLayoutController extends AdminAbstractBaseController<LayoutTypeEntity, LayoutTypeDTO, Long>{
    private final LayoutService service;
    private final LayoutTypeMapper mapper;

    @Transactional
    @PutMapping("/{id}/status")
    @CacheEvict(value = {SITE_TYPE, SITE_TYPES}, allEntries = true)
    public LayoutTypeDTO changeStatus(@PathVariable Long id) {
        return mapper.entityToDto(service.changeStatus(id));
    }

    @Override
    @PostMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @CacheEvict(value = {SITE_TYPE, SITE_TYPES}, allEntries = true)
    public void deleteAllById(@RequestBody DeleteRequest<Long> request) {
        super.deleteAllById(request);
    }


    @Override
    @PostMapping
    @Transactional
    @CacheEvict(value = {SITE_TYPE, SITE_TYPES}, allEntries = true)
    public LayoutTypeDTO create(@RequestBody @Valid LayoutTypeDTO request) {
        return super.create(request);
    }

    @GetMapping("/settings")
    public ShopConfigDTO getShopSettings() {
        return service.getSetting();
    }

    @PutMapping("/settings")
    @Transactional
    public ShopConfigDTO saveSettings(@RequestBody ShopConfigDTO dto) {
        return service.saveSettings(dto);
    }
}
