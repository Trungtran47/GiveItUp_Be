package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.constant.PredefinedRole;
import com.giveitup.giveitup_be.dto.request.UserCreationRequest;
import com.giveitup.giveitup_be.dto.request.UserUpdateRequest;
import com.giveitup.giveitup_be.dto.response.UserResponse;
import com.giveitup.giveitup_be.entity.RoleEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import com.giveitup.giveitup_be.exception.AppException;
import com.giveitup.giveitup_be.exception.ErrorCode;
import com.giveitup.giveitup_be.mapper.UserMapper;
import com.giveitup.giveitup_be.repository.RoleRepository;
import com.giveitup.giveitup_be.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreationRequest request) {
        UserEntity userEntity = userMapper.toUser(request);
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        userEntity.setDob(request.getDob());
        userEntity.setEmail(request.getEmail());
        userEntity.setPhoneNumber(request.getPhoneNumber());
        userEntity.setGender(request.getGender());
        userEntity.setFirstName(request.getFirstName());
        userEntity.setLastName(request.getLastName());

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

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(userEntity, request);
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));

//        var roles = roleRepository.findAllById(request.getRoles());
//        userEntity.setRoleEntities(new HashSet<>(roles));
        var roles = roleRepository.findById(request.getRoles()).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        userEntity.setRole(roles);
        return userMapper.toUserResponse(userRepository.save(userEntity));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        log.info("In method get Users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }
}
