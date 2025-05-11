package site.code4fun.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import site.code4fun.model.dto.ProductDTO;
import site.code4fun.model.mapper.ProductMapper;
import site.code4fun.service.ProductService;

import java.util.Map;

@RestController
@RequestMapping("/notify-logs")
@RequiredArgsConstructor
@Lazy
@Transactional(readOnly = true)
public class NotificationController {
    private final ProductMapper mapper;
    private final ProductService service;


    @GetMapping
    @Transactional
    public Page<ProductDTO> getAllPaging(@RequestParam Map<String, String> mapRequest) {
        return service.getPaging(mapRequest).map(mapper::entityToDto);
    }

    @GetMapping("/{id}")
    public ProductDTO getById(@PathVariable long id){
        return mapper.entityToDto(service.getById(id));
    }
}
