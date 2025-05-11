package site.code4fun.controller.admin;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.constant.MimeType;
import site.code4fun.model.Product;
import site.code4fun.model.dto.InventoryDTO;
import site.code4fun.model.dto.ProductDTO;
import site.code4fun.model.mapper.InventoryMapper;
import site.code4fun.model.mapper.ProductMapper;
import site.code4fun.service.ProductService;

import java.util.List;

import static site.code4fun.constant.AppConstants.HEADER_CONTENT_DISPOSITION;

@RestController
@RequestMapping(AppEndpoints.ADMIN_API_PREFIX + AppEndpoints.PRODUCTS_ENDPOINT)
@RequiredArgsConstructor
@Lazy
@Getter
public class AdminProductController extends AdminAbstractBaseController<Product, ProductDTO, Long> {
    private final ProductService service;
    private final ProductMapper mapper;
    private final InventoryMapper inventoryMapper;


    @PutMapping("/{id}/inventory")
    @Transactional
    public ProductDTO updateInventory(@PathVariable Long id, @RequestBody InventoryDTO request) {
        return mapper.entityToDto(service.addToInventory(id, inventoryMapper.dtoToEntity(request)));
    }

    @GetMapping("/{id}/export-qr")
    public ResponseEntity<ByteArrayResource> exportQRCode(@PathVariable Long id){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MimeType.APPLICATION_XLSX);
        String headerName = String.format("attachment; filename=product_%s.png", id);
        headers.setAccessControlExposeHeaders(List.of(HEADER_CONTENT_DISPOSITION));
        headers.add(HEADER_CONTENT_DISPOSITION, headerName);
        return ResponseEntity.ok()
                .headers(headers)
                .body(service.exportProductQR(id));
    }
}
