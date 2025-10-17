package com.example.orderservice.controller;

import com.example.commonservice.entity.ApiResponse;
import com.example.commonservice.entity.PageResponse;
import com.example.commonservice.util.ConstantUtils;
import com.example.commonservice.util.SortUtils;
import com.example.orderservice.request.Order.CreateOrderRequest;
import com.example.orderservice.response.ListOrderDTOResponse;
import com.example.orderservice.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/order")
@Validated
public class OrderController {

    private final OrderService orderService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<PageResponse<ListOrderDTOResponse>>> getOrders(
            @Min(value = ConstantUtils.MIN_CURRENT_PAGE, message = ConstantUtils.PAGE_MIN_MSG)
            @RequestParam(defaultValue = "" +  ConstantUtils.DEFAULT_CURRENT_PAGE)
            Integer page,
            @Max(value = ConstantUtils.MAX_PAGE_SIZE, message = ConstantUtils.SIZE_MAX_MSG)
            @Min(value = ConstantUtils.MIN_PAGE_SIZE, message = ConstantUtils.SIZE_MIN_MSG)
            @RequestParam(defaultValue = "" +  ConstantUtils.DEFAULT_PAGE_SIZE)
            Integer size,
            @RequestParam(defaultValue = "id,asc") String[] sort,
            @RequestParam(required = false, defaultValue = "") String search
    ) {
        return ResponseEntity.ok(
                orderService.getAll(PageRequest.of(page, size, SortUtils.getSort(sort)), search)
        );
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<String>> createOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest) {
        return ResponseEntity.ok(orderService.createOrder(createOrderRequest));
    }
}
