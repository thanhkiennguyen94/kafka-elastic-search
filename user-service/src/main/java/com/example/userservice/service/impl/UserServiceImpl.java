package com.example.userservice.service.impl;

import com.example.commonservice.entity.ApiResponse;
import com.example.commonservice.entity.PageResponse;
import com.example.commonservice.event.CreateUserEvent;
import com.example.commonservice.exception_handler.exception.SystemErrorException;
import com.example.commonservice.outbox.OutboxEvent;
import com.example.commonservice.outbox.OutboxService;
import com.example.commonservice.util.ConstantEventType;
import com.example.commonservice.util.ConstantType;
import com.example.commonservice.util.ConstantUtils;
import com.example.commonservice.util.PageMapperUtil;
import com.example.userservice.elasticsearch.UserDocument;
import com.example.userservice.elasticsearch.UserElasticRepository;
import com.example.userservice.elasticsearch.UserOutboxProcessor;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.request.User.CreateUserDTORequest;
import com.example.userservice.response.User.ListUserDTOResponse;
import com.example.userservice.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    private final UserElasticRepository userElasticRepository;
    private final UserOutboxProcessor userOutboxProcessor;
    private final OutboxService outboxService;
    @Override
    public ApiResponse<PageResponse<ListUserDTOResponse>> getAll(Pageable pageable, String search) {
        Page<UserDocument> userDocuments = userElasticRepository.findByUsernameContainingOrEmailContaining(search,search, pageable);
        return PageMapperUtil.toApiResponse(userDocuments, ListUserDTOResponse.class);
    }

    @Override
    @Transactional
    public ApiResponse<String> createUser(CreateUserDTORequest createUserDTORequest) {
        User user = modelMapper.map(createUserDTORequest, User.class);
        userRepository.save(user);

        OutboxEvent event;
        try {
            String payload = objectMapper.writeValueAsString(new CreateUserEvent(user.getId(), user.getUsername(), user.getEmail()));
            event = outboxService.saveEvent(ConstantType.TYPE_USER, user.getId(), ConstantEventType.EVENT_CREATE_USER, payload);
        } catch (JsonProcessingException e) {
            throw new SystemErrorException(e.getMessage());
        }
        // xử lý ngay (synchronous) → dữ liệu Order có ngay
        outboxService.processSingleEvent(event);
        // xử lý ngay (synchronous) → dữ liệu Elastic có ngay
        userOutboxProcessor.processSingleEvent(event);

        return ApiResponse.success(null, ConstantUtils.CREATE_SUCCESSFULLY);
    }
}
