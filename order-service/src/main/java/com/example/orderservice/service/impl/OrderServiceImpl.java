package com.example.orderservice.service.impl;

import com.example.commonservice.entity.ApiResponse;
import com.example.commonservice.entity.PageResponse;
import com.example.commonservice.util.ConstantUtils;
import com.example.commonservice.util.PageMapperUtil;
import com.example.orderservice.model.Order;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.request.Order.CreateOrderRequest;
import com.example.orderservice.response.ListOrderDTOResponse;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;

    @Override
    public ApiResponse<PageResponse<ListOrderDTOResponse>> getAll(Pageable pageable, String search) {
        Page<Order> userPage = orderRepository.findAll(pageable, search);
        return PageMapperUtil.toApiResponse(userPage, ListOrderDTOResponse.class);
    }

    @Override
    public ApiResponse<String> createOrder(CreateOrderRequest createOrderRequest) {
        Order order = modelMapper.map(createOrderRequest, Order.class);
        orderRepository.save(order);
        return ApiResponse.success(null, ConstantUtils.CREATE_SUCCESSFULLY);
    }
}
