package site.code4fun.controller.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.OrderEntity;
import site.code4fun.model.dto.OrderDTO;
import site.code4fun.model.mapper.OrderMapper;
import site.code4fun.service.OrderService;

@Getter
@RestController
@RequestMapping(AppEndpoints.ADMIN_API_PREFIX + AppEndpoints.ORDERS_ENDPOINT)
@RequiredArgsConstructor
@Lazy
public class AdminOrderController extends AdminAbstractBaseController<OrderEntity, OrderDTO, Long>{

	private final OrderService service;
	private final OrderMapper mapper;

	@Override
	@PutMapping("/{id}")
	public OrderDTO replace(@PathVariable Long id, @RequestBody(required = false) OrderDTO request){
		return mapper.entityToDto(service.changeOrderStatus(id));
	}

	@PutMapping("/{id}/reject")
	@Transactional
	public OrderDTO rejectOrder(@PathVariable long id){
		return mapper.entityToDto(service.rejectOrder(id));
	}

	@GetMapping("/statistic")
	public Object getUserStatistic() {
		return service.getStatistic();
	}
}
