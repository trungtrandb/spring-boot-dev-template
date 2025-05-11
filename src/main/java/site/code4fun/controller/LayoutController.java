package site.code4fun.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.code4fun.model.dto.CouponDTO;
import site.code4fun.model.dto.LayoutTypeDTO;
import site.code4fun.model.dto.ShopConfigDTO;
import site.code4fun.model.dto.TagDTO;
import site.code4fun.model.mapper.CouponMapper;
import site.code4fun.model.mapper.LayoutTypeMapper;
import site.code4fun.model.mapper.TagMapper;
import site.code4fun.service.CouponService;
import site.code4fun.service.LayoutService;
import site.code4fun.service.TagService;

import java.util.List;
import java.util.Map;

import static site.code4fun.constant.CacheName.*;

@RestController
@RequiredArgsConstructor
@Transactional
public class LayoutController {
    private final LayoutService service;
    private final LayoutTypeMapper mapper;
    private final CouponMapper couponMapper;
    private final CouponService couponService;
    private final TagService tagService;
    private final TagMapper tagMapper;
    @Cacheable(SITE_TYPES)
    @GetMapping("/types")
    public List<LayoutTypeDTO> getType(){
        return service.getAllActive().stream().map(mapper::entityToDto).toList();
    }

    @Cacheable(value = SITE_TYPE, key = "#slug")
    @GetMapping("/types/{slug}")
    public LayoutTypeDTO getBySlug(@PathVariable String slug){
        return mapper.entityToDto(service.findBySlug(slug));
    }

//    @Cacheable(SITE_SETTINGS)
    @GetMapping("/settings")
    public ShopConfigDTO getSetting(){
        return service.getSetting();
    }

    @GetMapping("/coupons")
    public List<CouponDTO> getCoupons(){
        return couponService.getAll().stream().map(couponMapper::entityToDto).toList();
    }

    @GetMapping("/tags")
    public Page<TagDTO> getTags(@RequestParam Map<String, String> mapRequest) {
        return tagService.getPaging(mapRequest).map(tagMapper::entityToDto);
    }
}
