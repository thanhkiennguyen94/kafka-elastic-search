package com.example.userservice.service.impl;

import com.example.commonservice.entity.ApiResponse;
import com.example.commonservice.entity.PageResponse;
import com.example.commonservice.event.CreateUserEvent;
import com.example.commonservice.outbox.OutboxEvent;
import com.example.commonservice.outbox.OutboxRepository;
import com.example.commonservice.outbox.OutboxStatus;
import com.example.commonservice.util.ConstantUtils;
import com.example.commonservice.util.PageMapperUtil;
import com.example.userservice.elasticsearch.UserDocument;
import com.example.userservice.elasticsearch.UserElasticRepository;
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
    private final OutboxRepository outboxRepository;
    private final UserElasticRepository userElasticRepository;

    @Override
    public ApiResponse<PageResponse<ListUserDTOResponse>> getAll(Pageable pageable, String search) {
//        Page<User> userPage = userRepository.findAll(pageable, search);
        Page<UserDocument> userDocuments = userElasticRepository.findByUsernameContainingOrEmailContaining(search,search, pageable);
        return PageMapperUtil.toApiResponse(userDocuments, ListUserDTOResponse.class);
    }

    @Override
    @Transactional
    public ApiResponse<String> createUser(CreateUserDTORequest createUserDTORequest) {
        User user = modelMapper.map(createUserDTORequest, User.class);
        userRepository.save(user);

        OutboxEvent event = null;
        try {
            event = OutboxEvent.builder()
                    .aggregateType("User")
                    .aggregateId(user.getId())
                    .eventType("UserCreated")
                    .payload(objectMapper.writeValueAsString(new CreateUserEvent(user.getId(), user.getUsername(), user.getEmail())))
                    .statusOrder(OutboxStatus.PENDING)
                    .statusElastic(OutboxStatus.PENDING)
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        outboxRepository.save(event);
        return ApiResponse.success(null, ConstantUtils.CREATE_SUCCESSFULLY);
    }
}
