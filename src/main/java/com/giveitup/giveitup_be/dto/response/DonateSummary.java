package com.giveitup.giveitup_be.dto.response;

import com.giveitup.giveitup_be.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DonateSummary {
    private Long id;
    private Double amount;
    private LocalDateTime latestDonatedAt; // mới thêm
    private UserPostResponse user;

    public DonateSummary(Long id,Double amount, LocalDateTime latestDonatedAt, UserEntity userEntity) {
        this.id = id;
        this.amount = amount;
        this.latestDonatedAt = latestDonatedAt;
        this.user = new UserPostResponse(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getFirstName(),
                userEntity.getLastName(),
                userEntity.getDob(),
                userEntity.getGender(),
                userEntity.getEmail(),
                userEntity.getPhoneNumber(),
                userEntity.getStatus(),
                userEntity.getOrganizationLogo(),
                userEntity.getOrganizationLogoPublicId(),
                userEntity.getOrganizationName(),
                null, // CategoryResponse mapping nếu cần
                userEntity.getEstablishmentDate(),
                userEntity.getOrganizationAddress(),
                userEntity.getOrganizationEmail(),
                userEntity.getRegistrationCode(),
                userEntity.getOrganizationPhone(),
                userEntity.getVerificationFile(),
                userEntity.getVerificationInfoPublicId(),
                userEntity.getLinkInfoOrganization(),
                userEntity.getOrganizationDescription(),
                userEntity.getOrganizationCreatedAt()
        );
    }
}
