package site.code4fun.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import site.code4fun.model.InventoryEntity;
import site.code4fun.model.Product;
import site.code4fun.model.dto.ProductDTO;
import site.code4fun.repository.jpa.InventoryRepository;
import site.code4fun.repository.jpa.ProductRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Lazy
public class InventoryService extends AbstractBaseService<InventoryEntity, String> {

    @Getter(AccessLevel.PROTECTED)
    private final InventoryRepository repository;
    private final ProductRepository productRepository;

    public boolean isProductAvailable(Collection<ProductDTO> productRequests) {
        Map<Long, Integer> mapProduct = productRequests.stream().collect(Collectors.toMap(ProductDTO::getId, ProductDTO::getQuantity));
        List<Product> lst = productRepository.findAllByIdIn(mapProduct.keySet());
        for (Product product: lst) {
            if (mapProduct.get(product.getId()) > product.getQuantity()){
                log.warn("Product {} is out of stock, request {}, inventory {}",
                        product.getName(),
                        mapProduct.get(product.getId()),
                        product.getQuantity());
                return false;
            }
        }
        return true;
    }
}
