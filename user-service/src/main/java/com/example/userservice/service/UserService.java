package com.example.userservice.service;

import com.example.commonservice.entity.ApiResponse;
import com.example.commonservice.entity.PageResponse;
import com.example.userservice.request.User.CreateUserDTORequest;
import com.example.userservice.response.User.ListUserDTOResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    ApiResponse<PageResponse<ListUserDTOResponse>> getAll(Pageable pageable, String search);
    ApiResponse<String> createUser(CreateUserDTORequest createUserDTORequest);
}
