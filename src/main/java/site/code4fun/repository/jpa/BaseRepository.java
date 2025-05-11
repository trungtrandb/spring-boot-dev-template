package site.code4fun.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T, E> extends JpaRepository<T, E>, JpaSpecificationExecutor<T> {
//    Optional<T> naturalId(E naturalId);
}

//class BaseRepositoryImpl<T, E extends Serializable> extends SimpleJpaRepository<T, E> implements BaseRepository<T, E> {
//    private final EntityManager entityManager;
//
//    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
//        super(entityInformation, entityManager);
//        this.entityManager = entityManager;
//    }
//
//    @Override
//    public Optional<T> naturalId(E naturalId) {
//        return entityManager.unwrap(Session.class)
//                .bySimpleNaturalId(this.getDomainClass())
//                .loadOptional(naturalId);
//    }
//}
