package site.code4fun.controller.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.Role;
import site.code4fun.model.User;
import site.code4fun.model.dto.OrderDTO;
import site.code4fun.model.dto.RoleDTO;
import site.code4fun.model.dto.UserDTO;
import site.code4fun.model.mapper.OrderMapper;
import site.code4fun.model.mapper.RoleMapper;
import site.code4fun.model.mapper.UserMapper;
import site.code4fun.service.OrderService;
import site.code4fun.service.RoleService;
import site.code4fun.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(AppEndpoints.ADMIN_API_PREFIX + AppEndpoints.USERS_ENDPOINT)
@RequiredArgsConstructor
@Lazy
@Getter
public class AdminUserController extends AdminAbstractBaseController<User, UserDTO, Long>{

	private final UserService service;
	private final RoleService roleService;
	private final UserMapper mapper;
	private final RoleMapper roleMapper;
	private final OrderService orderService;
	private final OrderMapper orderMapper;

	@PutMapping("/{id}/status")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void updateUserStatus(@PathVariable long id){
		service.updateEnable(id);
	}

	@GetMapping("/{userId}/payments")
	public Page<OrderDTO> getUserTransactions(@PathVariable Long userId, @RequestParam Map<String, String> mapRequest) {
		return orderService.getPagingByUserId(mapRequest, userId).map(orderMapper::entityToDto);
	}

	@PostMapping("/roles")
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public RoleDTO createRole(@RequestBody RoleDTO roleDTO) {
		Role role = roleMapper.dtoToEntity(roleDTO);
		return roleMapper.entityToDto(roleService.create(role));
	}

	@PostMapping("/roles/delete")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void deleteById(@RequestBody Map<String, List<Long>> map){
		roleService.deleteByIds(map.get("id"));
	}


	@DeleteMapping("/roles/{id}")
	@Transactional
	public void deleteRole(@PathVariable long id) {
		roleService.delete(id);
	}

	@GetMapping("/roles")
	public Page<RoleDTO> getAllRolePaging(@RequestParam Map<String, String> mapRequest) {
		return roleService.getPaging(mapRequest).map(roleMapper::entityToDto);
	}

	@GetMapping("/statistic")
	public Object getUserStatistic() {
		return service.getStatistic();
	}
}
