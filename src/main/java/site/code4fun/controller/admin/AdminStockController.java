package site.code4fun.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import site.code4fun.constant.AppEndpoints;
import site.code4fun.model.dto.StockSymbolDTO;
import site.code4fun.model.mapper.StockSymbolMapper;
import site.code4fun.service.StockService;
import site.code4fun.service.StockSymbolService;

import java.util.Map;

@Getter
@RestController
@RequestMapping(AppEndpoints.ADMIN_API_PREFIX + AppEndpoints.STOCK_ENDPOINT)
@RequiredArgsConstructor
@Lazy
public class AdminStockController{

	private final StockService service;
	private final StockSymbolService stockSymbolService;
	private final StockSymbolMapper stockSymbolMapper;

	@GetMapping("/symbols")
	@Operation( summary = "Get all paging", description = "Get all data with paging")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful") })
	public Page<StockSymbolDTO> getAllPaging(@RequestParam Map<String, String> searchRequest) {
		return stockSymbolService.getPaging(searchRequest).map(stockSymbolMapper::entityToDto);
	}
}
