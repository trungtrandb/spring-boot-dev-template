package site.code4fun.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import site.code4fun.model.ShiftEntity;
import site.code4fun.repository.jpa.ShiftRepository;

@Service
@Slf4j
@RequiredArgsConstructor
@Lazy
@Getter(AccessLevel.PROTECTED)
public class ShiftService extends AbstractBaseService<ShiftEntity, Long> {

    private final ShiftRepository repository;
}
