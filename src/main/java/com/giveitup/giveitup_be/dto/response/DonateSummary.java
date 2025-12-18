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
    private UserResponse user;

    public DonateSummary(Long id, Double amount, LocalDateTime latestDonatedAt, UserEntity userEntity) {
        this.id = id;
        this.amount = amount;
        this.latestDonatedAt = latestDonatedAt;

        OrganizationResponse orgResponse = null;
        if (userEntity.getOrganization() != null) {
            orgResponse = new OrganizationResponse(
                    userEntity.getOrganization().getId(),
                    userEntity.getOrganization().getOrganizationLogo(),
                    userEntity.getOrganization().getOrganizationLogoPublicId(),
                    userEntity.getOrganization().getOrganizationName(),
                    null, // category có thể để null hoặc tạo CategoryResponse
                    userEntity.getOrganization().getEstablishmentDate(),
                    userEntity.getOrganization().getOrganizationAddress(),
                    userEntity.getOrganization().getOrganizationEmail(),
                    userEntity.getOrganization().getRegistrationCode(),
                    userEntity.getOrganization().getOrganizationPhone(),
                    userEntity.getOrganization().getVerificationFile(),
                    userEntity.getOrganization().getVerificationInfoPublicId(),
                    userEntity.getOrganization().getLinkInfoOrganization(),
                    userEntity.getOrganization().getOrganizationDescription(),
                    userEntity.getId(),
                    userEntity.getOrganization().getOrganizationApprovedAt(),
                    userEntity.getOrganization().getCreatedAt(),
                    userEntity.getOrganization().getUpdatedAt()
            );
        }

        this.user = new UserResponse(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getFirstName(),
                userEntity.getLastName(),
                userEntity.getDob(),
                userEntity.getGender(),
                userEntity.getEmail(),
                userEntity.getPhoneNumber(),
                userEntity.getRole() != null ? userEntity.getRole().getName() : null,
                userEntity.getAddress(),
                userEntity.getImageUser(),
                userEntity.getPublicImageUserId(),
                userEntity.isPublic(),
                userEntity.getIntroduce(),
                userEntity.getCreatedAt(),
                userEntity.getUpdatedAt(),
                userEntity.getStatus(),
                orgResponse, // truyền OrganizationResponse thay vì OrganizationEntity
                null,
                null,
                null
        );
    }


}
