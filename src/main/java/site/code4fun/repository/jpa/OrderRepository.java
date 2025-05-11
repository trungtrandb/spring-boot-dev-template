package site.code4fun.repository.jpa;

import site.code4fun.constant.OrderStatus;
import site.code4fun.model.OrderEntity;

public interface OrderRepository extends BaseRepository<OrderEntity, Long> {
    long countAllByStatus(OrderStatus status);
    long countAllByStatusAndUser_id(OrderStatus status, Long userId);
}
