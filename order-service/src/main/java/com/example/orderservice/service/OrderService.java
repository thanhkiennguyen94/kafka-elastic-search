package com.example.orderservice.service;

import com.example.commonservice.entity.ApiResponse;
import com.example.commonservice.entity.PageResponse;
import com.example.orderservice.request.Order.CreateOrderRequest;
import com.example.orderservice.response.ListOrderDTOResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {
    ApiResponse<PageResponse<ListOrderDTOResponse>> getAll(Pageable pageable, String search);
    ApiResponse<String> createOrder(CreateOrderRequest createOrderRequest);
}
