package com.giveitup.giveitup_be.controller;

import com.giveitup.giveitup_be.dto.paging.PagingResponse;
import com.giveitup.giveitup_be.dto.request.*;
import com.giveitup.giveitup_be.dto.response.UserResponse;
import com.giveitup.giveitup_be.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {
    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @GetMapping
    public ApiResponse<PagingResponse<UserResponse>> getUsers(@ModelAttribute SearchListUserRequest request) {
        Page<UserResponse> page = userService.getUsers(request);

        PagingResponse.PagingInfo paging = PagingResponse.PagingInfo.builder()
                .CurrentPage(request.getCurrentPage())
                .NumberOfRecord(request.getPageSize())
                .TotalRecord(page.getTotalElements())
                .TotalPages(page.getTotalPages())
                .build();

        PagingResponse<UserResponse> pagingResponse = PagingResponse.<UserResponse>builder()
                .Paging(paging)
                .Data(page.getContent())
                .build();

        return ApiResponse.<PagingResponse<UserResponse>>builder()
//                .code(200)
//                .message("Success")
                .result(pagingResponse)
                .build();
    }


    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable("userId") String userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUser(userId))
                .build();
    }

    @GetMapping("/my-info")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @DeleteMapping("/{userId}")
    ApiResponse<String> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ApiResponse.<String>builder().result("User has been deleted").build();
    }

    @PutMapping(value = "/{userId}", consumes = {"multipart/form-data"})
    ApiResponse<UserResponse> updateUser(@PathVariable Long userId, @ModelAttribute UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }
    @PutMapping(value = "/register/author/{userId}", consumes = {"multipart/form-data"})
    public ApiResponse<UserResponse> createAuthor(
            @PathVariable Long userId,
            @ModelAttribute AuthorCreationRequest request // dùng @ModelAttribute để nhận cả file + text
    ) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.registerAuthor(userId,request))
                .build();
    }
    }

