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

        this.user = new UserResponse(
                userEntity.getId(),                        // id
                userEntity.getUsername(),                 // username
                userEntity.getFirstName(),                // firstName
                userEntity.getLastName(),                 // lastName
                userEntity.getDob(),                      // dob
                userEntity.getGender(),                   // gender
                userEntity.getEmail(),                    // email
                userEntity.getPhoneNumber(),              // phoneNumber
                userEntity.getRole().getName(),           // role
                userEntity.getAddress(),                  // address
                userEntity.getImageUser(),                // imageUser
                userEntity.getPublicImageUserId(),        // publicImageUserId
                userEntity.getCreatedAt(),                // createdAt
                userEntity.getUpdatedAt(),                // updatedAt
                userEntity.getStatus(),                   // status
                userEntity.getOrganizationLogo(),         // organizationLogo
                userEntity.getOrganizationLogoPublicId(), // organizationLogoPublicId
                userEntity.getOrganizationName(),         // organizationName
                null,                                     // category (hoặc map nếu cần)
                userEntity.getEstablishmentDate(),        // establishmentDate
                userEntity.getOrganizationAddress(),      // organizationAddress
                userEntity.getOrganizationEmail(),        // organizationEmail
                userEntity.getRegistrationCode(),         // registrationCode
                userEntity.getOrganizationPhone(),        // organizationPhone
                userEntity.getVerificationFile(),         // verificationFile
                userEntity.getVerificationInfoPublicId(), // verificationInfoPublicId
                userEntity.getLinkInfoOrganization(),     // linkInfoOrganization
                userEntity.getOrganizationDescription(),  // organizationDescription
                userEntity.getOrganizationCreatedAt(),   // organizationCreatedAt
                null,
                null,
                null
        );
    }

}
