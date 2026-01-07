package com.giveitup.giveitup_be.service;


import com.giveitup.giveitup_be.dto.request.AuthenticationRequest;
import com.giveitup.giveitup_be.dto.request.IntrospectRequest;
import com.giveitup.giveitup_be.dto.request.LogoutRequest;
import com.giveitup.giveitup_be.dto.request.RefreshRequest;
import com.giveitup.giveitup_be.dto.response.AuthenticationResponse;
import com.giveitup.giveitup_be.dto.response.IntrospectResponse;
import com.giveitup.giveitup_be.entity.InvalidatedTokenEntity;
import com.giveitup.giveitup_be.entity.RoleEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import com.giveitup.giveitup_be.enums.UserStatus;
import com.giveitup.giveitup_be.exception.AppException;
import com.giveitup.giveitup_be.exception.ErrorCode;
import com.giveitup.giveitup_be.mapper.RoleMapper;
import com.giveitup.giveitup_be.repository.InvalidatedTokenRepository;
import com.giveitup.giveitup_be.repository.RoleRepository;
import com.giveitup.giveitup_be.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    RoleMapper roleMapper;
    RoleRepository roleRepository;
    JavaMailSender javaMailSender;
    PasswordEncoder passwordEncoder;
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    // 1. Gửi OTP
    public void sendOtp(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_EXISTED));
        // Tạo OTP 6 số
        String otp = String.format("%06d", new Random().nextInt(999999));
        // Lưu OTP vào DB (hết hạn sau 5 phút)
        user.setOtp(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);
        // Gửi email
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email); // Gửi đến email người dùng nhập
        msg.setSubject("Mã xác nhận quên mật khẩu");
        msg.setText("Mã OTP của bạn là: " + otp + "\nMã này có hiệu lực trong 5 phút.");
        javaMailSender.send(msg);
    }
    // 2. Xác thực OTP và Đổi mật khẩu
    public void verifyAndResetPassword(String email, String otp, String newPassword) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            throw new AppException(ErrorCode.INCORRECT_OTP_CODE);
        }
        if (user.getOtpExpiryTime().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.OTP_CODE_HAS_EXPIRED);
        }
        // Đổi mật khẩu và mã hóa
        user.setPassword(passwordEncoder.encode(newPassword));
        // Xóa OTP để không dùng lại được
        user.setOtp(null);
        user.setOtpExpiryTime(null);
        userRepository.save(user);
    }

    @Transactional
    public String handleGoogleLogin(String email, String fullName, String photoUrl
//            , LocalDate dob, String genderStr, String phoneNumber


    ) {
        Optional<UserEntity> optionalUser = userRepository.findByEmail(email);
        UserEntity user;

        if (optionalUser.isEmpty()) {
            // --- CASE 1: TẠO MỚI ---
            log.info("Tạo mới user từ Google: {}", email);

            String firstName = fullName;
            String lastName = "";
            if (fullName != null && fullName.contains(" ")) {
                int lastSpaceIndex = fullName.lastIndexOf(" ");
                firstName = fullName.substring(0, lastSpaceIndex);
                lastName = fullName.substring(lastSpaceIndex + 1);
            }
//            Long genderId = null;
//            if ("male".equalsIgnoreCase(genderStr)) genderId = 1L;
//            else if ("female".equalsIgnoreCase(genderStr)) genderId = 2L;
            RoleEntity defaultRole = roleRepository.findById("USER")
                    .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));

            user = UserEntity.builder()
                    .email(email)
                    .username(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .imageUser(photoUrl)
                    .role(defaultRole)
                    .status( UserStatus.USER.getCode())
                    .password(UUID.randomUUID().toString())
                    .isPublic(false)
//                    .dob(dob)
//                    .gender(genderId)
//                    .phoneNumber(phoneNumber)
                    .build();

            user = userRepository.save(user); // Lưu xong user vẫn ở trạng thái persistent
        } else {
            // --- CASE 2: CẬP NHẬT ---
            user = optionalUser.get();
            if(user.getStatus().equals(UserStatus.INACTIVE.getCode())){
                throw new AppException(ErrorCode.USER_HAS_BLOCKED);
            }
            user = userRepository.save(user);
        }
        return generateToken(user);
    }
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var loginKey = request.getUsername(); // Biến này chứa input người dùng nhập (có thể là user hoặc email)
        var user = userRepository
                .findByUsernameOrEmail(loginKey, loginKey)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if(!Objects.equals(user.getRole().getName(), "ADMIN") &&  user.getStatus().equals(UserStatus.INACTIVE.getCode())){
            throw new AppException(ErrorCode.USER_HAS_BLOCKED);
        }
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        var token = generateToken(user);
        long expires = VALID_DURATION;

        return AuthenticationResponse.builder().token(token).expires(expires).authenticated(true).build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedTokenEntity invalidatedTokenEntity =
                    InvalidatedTokenEntity.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedTokenEntity);
        } catch (AppException exception) {
            log.info("Token already expired");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedTokenEntity invalidatedTokenEntity =
                InvalidatedTokenEntity.builder().id(jit).expiryTime(expiryTime).build();

        invalidatedTokenRepository.save(invalidatedTokenEntity);

        var username = signedJWT.getJWTClaimsSet().getSubject();

        var user =
                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        var token = generateToken(user);
        long expires = VALID_DURATION;

        return AuthenticationResponse.builder().token(token).expires(expires).authenticated(true).build();
    }

    private String generateToken(UserEntity userEntity) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(userEntity.getUsername())
                .issuer("giveitup.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(userEntity))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT
                        .getJWTClaimsSet()
                        .getIssueTime()
                        .toInstant()
                        .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                        .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

//    private String buildScope(UserEntity userEntity) {
//        StringJoiner stringJoiner = new StringJoiner(" ");
//
//        if (!CollectionUtils.isEmpty(userEntity.getRoleEntities()))
//            userEntity.getRoleEntities().forEach(role -> {
//                stringJoiner.add("ROLE_" + role.getName());
//                if (!CollectionUtils.isEmpty(role.getPermissions()))
//                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
//            });
//
//        return stringJoiner.toString();
//    }
    private String buildScope(UserEntity userEntity) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        RoleEntity role = userEntity.getRole();
        if (role != null) {
            stringJoiner.add("ROLE_" + role.getName());

            if (!CollectionUtils.isEmpty(role.getPermissions())) {
                role.getPermissions()
                        .forEach(permission -> stringJoiner.add(permission.getName()));
            }
        }

        return stringJoiner.toString();
    }

}
