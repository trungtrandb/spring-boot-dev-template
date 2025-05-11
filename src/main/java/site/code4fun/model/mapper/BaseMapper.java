package site.code4fun.model.mapper;

public interface BaseMapper<E,D> {

    D entityToDto(E e);
    E dtoToEntity(D e);
}
