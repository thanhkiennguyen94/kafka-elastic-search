package com.example.userservice.controller;

import com.example.commonservice.entity.ApiResponse;
import com.example.commonservice.entity.PageResponse;
import com.example.commonservice.util.ConstantUtils;
import com.example.commonservice.util.SortUtils;
import com.example.userservice.request.User.CreateUserDTORequest;
import com.example.userservice.response.User.ListUserDTOResponse;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<PageResponse<ListUserDTOResponse>>> getAccounts(
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
                userService.getAll(PageRequest.of(page, size, SortUtils.getSort(sort)), search)
        );
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<String>> createUser(@Valid @RequestBody CreateUserDTORequest createUserDTORequest) {
        return ResponseEntity.ok(userService.createUser(createUserDTORequest));
    }

}
