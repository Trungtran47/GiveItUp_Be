package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.request.AuthorCreationRequest;
import com.giveitup.giveitup_be.dto.request.SearchListUserRequest;
import com.giveitup.giveitup_be.dto.request.UserCreationRequest;
import com.giveitup.giveitup_be.dto.request.UserUpdateRequest;
import com.giveitup.giveitup_be.dto.response.UserResponse;
import com.giveitup.giveitup_be.entity.CategoryEntity;
import com.giveitup.giveitup_be.entity.RoleEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import com.giveitup.giveitup_be.enums.UserStatus;
import com.giveitup.giveitup_be.exception.AppException;
import com.giveitup.giveitup_be.exception.ErrorCode;
import com.giveitup.giveitup_be.mapper.UserMapper;
import com.giveitup.giveitup_be.repository.CategoryRepository;
import com.giveitup.giveitup_be.repository.RoleRepository;
import com.giveitup.giveitup_be.repository.UserRepository;
import com.giveitup.giveitup_be.specification.UserSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    CloudinaryService cloudinaryService;
    CategoryRepository categoryRepository;
//
public UserResponse registerAuthor(Long userId, AuthorCreationRequest request) {
    UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    CategoryEntity categoryEntity = categoryRepository.findById(request.getCategory()).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
    if (request.getOrganizationLogo() != null && !request.getOrganizationLogo().isEmpty()) {
        // Upload file mới
        Map<String, String> uploadResult = cloudinaryService.uploadImage(
                request.getOrganizationLogo(), "GiveItUp/images");
        if (userEntity.getOrganizationLogoPublicId() != null) {
            cloudinaryService.deleteFile(userEntity.getOrganizationLogoPublicId(), true);
        }
        userEntity.setOrganizationLogo(uploadResult.get("url"));
        userEntity.setOrganizationLogoPublicId(uploadResult.get("public_id"));
    } else if (request.getOrganizationLogoUrl() != null) {
        // Nếu gửi URL cũ thì giữ nguyên
        userEntity.setOrganizationLogo(request.getOrganizationLogoUrl());
    }
    if (request.getVerificationFile() != null && !request.getVerificationFile().isEmpty()) {
        Map<String, String> uploadResult = cloudinaryService.uploadFile(
                request.getVerificationFile(), "GiveItUp/files");
        // Xóa file cũ nếu đã có
        if (userEntity.getVerificationInfoPublicId() != null) {
            cloudinaryService.deleteFile(userEntity.getVerificationInfoPublicId(), false);
        }
        userEntity.setVerificationFile(uploadResult.get("url"));
        userEntity.setVerificationInfoPublicId(uploadResult.get("public_id"));
    } else if (request.getVerificationFileUrl() != null) {
        // Nếu gửi URL cũ thì giữ nguyên
        userEntity.setVerificationFile(request.getVerificationFileUrl());
    }
    userMapper.updateAuthor(userEntity, request);
    if(request.getStatus() != null ){
        userEntity.setStatus(request.getStatus());
    }else {
        userEntity.setStatus(UserStatus.PENDING.getCode());
    }
    userEntity.setOrganizationCreatedAt(LocalDateTime.now());
    userEntity.setCategory(categoryEntity);

    return userMapper.toUserResponse(userRepository.save(userEntity));
}



    public UserResponse createUser(UserCreationRequest request) {
        UserEntity userEntity = userMapper.toUser(request);
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        userEntity.setDob(request.getDob());
        userEntity.setEmail(request.getEmail());
        userEntity.setPhoneNumber(request.getPhoneNumber());
        userEntity.setGender(request.getGender());
        userEntity.setFirstName(request.getFirstName());
        userEntity.setLastName(request.getLastName());
        userEntity.setStatus(UserStatus.USER.getCode());

//        HashSet<RoleEntity> roleEntities = new HashSet<>();
//        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roleEntities::add);
//
//        userEntity.setRoleEntities(roleEntities);
        RoleEntity roleEntity =  roleRepository.findById(request.getRole()).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        userEntity.setRole(roleEntity);

        try {
            userEntity = userRepository.save(userEntity);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return userMapper.toUserResponse(userEntity);
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        UserEntity userEntity = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(userEntity);
    }
    public UserEntity getMyInfoReturnEntity() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        return userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userMapper.updateUser(userEntity, request);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        }

//        var roles = roleRepository.findAllById(request.getRoles());
//        userEntity.setRoleEntities(new HashSet<>(roles));
        // Kiểm tra role trước khi findById
        if (request.getRoles() != null) {
            var role = roleRepository.findById(request.getRoles())
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
            userEntity.setRole(role);
        }
        if (request.getImageUser() != null && !request.getImageUser().isEmpty()) {
            // Upload file mới
            Map<String, String> uploadResult = cloudinaryService.uploadImage(
                    request.getImageUser(), "GiveItUp/images_user");
            if (userEntity.getImageUser() != null) {
                cloudinaryService.deleteFile(userEntity.getPublicImageUserId(), true);
            }
            userEntity.setImageUser(uploadResult.get("url"));
            userEntity.setPublicImageUserId(uploadResult.get("public_id"));
        }
        return userMapper.toUserResponse(userRepository.save(userEntity));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponse> getUsers(SearchListUserRequest request) {
        Specification<UserEntity> spec = Specification.allOf(
                UserSpecification.hasUsername(request.getUserName()),
                UserSpecification.hasPhoneNumber(request.getPhoneNumber()),
                (root, query, cb) -> cb.notEqual(root.get("role").get("name"), "ADMIN")
        );
        int pageIndex = Math.max(request.getCurrentPage() - 1, 0);
        Pageable pageable = PageRequest.of(
                pageIndex,
                request.getPageSize(),
                Sort.by("username").ascending()
        );

        Page<UserEntity> page = userRepository.findAll(spec, pageable);
        log.info("Found {} users", page.getTotalElements());

        return page.map(userMapper::toUserResponse);

    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(Long id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }
}
