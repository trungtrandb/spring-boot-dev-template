package site.code4fun.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import site.code4fun.model.CouponEntity;
import site.code4fun.repository.jpa.CouponRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Lazy
@RequiredArgsConstructor
public class CouponService extends AbstractBaseService<CouponEntity, Long> {

    @Getter(AccessLevel.PROTECTED)
    private final CouponRepository repository;

    @Override
    public List<CouponEntity> getAll(){
        List<CouponEntity> lst = getRepository().findAll();
        if (lst.isEmpty()){
            return getRepository().saveAll(simple());
        }
        return lst;
    }

    private List<CouponEntity> simple() {
        List<CouponEntity> lst = new ArrayList<>();
        CouponEntity cp = new CouponEntity();
        cp.setCode("4OFF");
        cp.setType("fixed");
        cp.setAmount(4);
        return lst;
    }
}
