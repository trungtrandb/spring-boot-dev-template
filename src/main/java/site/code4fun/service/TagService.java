package site.code4fun.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import site.code4fun.model.TagEntity;
import site.code4fun.repository.jpa.TagRepository;

@Service
@Slf4j
@RequiredArgsConstructor
@Lazy
@Getter(AccessLevel.PROTECTED)
public class TagService extends AbstractBaseService<TagEntity, Long> {

    private final TagRepository repository;
}
