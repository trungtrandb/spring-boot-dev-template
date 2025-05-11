package site.code4fun.repository.jpa;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import site.code4fun.model.Product;

import java.util.Collection;
import java.util.List;

public interface ProductRepository extends BaseRepository<Product, Long>{

    @EntityGraph(attributePaths = {"categories"})
    List<Product> findAllByCategories_IdIn(List<Long> ids);//NOSONAR

    List<Product> findAllByIdIn(Collection<Long> ids);

    List<Product> findAllByCategories_IdInOrderByIdDesc(List<Long> ids, Limit limit);//NOSONAR

    @Query("SELECT p FROM Product p left JOIN InventoryEntity i ON p.id = i.product.id GROUP BY p.id ORDER BY SUM(i.cost) ASC")
    Page<Product> findPageByInventoryCost(Pageable pageRequest);

    @Query("SELECT p FROM Product p left JOIN InventoryEntity i ON p.id = i.product.id GROUP BY p.id ORDER BY SUM(i.cost) DESC")
    Page<Product> findPageByInventoryCostDesc(Pageable pageRequest);
}
