package com.giveitup.giveitup_be.mapper;

import com.giveitup.giveitup_be.dto.request.AuthorCreationRequest;
import com.giveitup.giveitup_be.dto.response.OrganizationResponse;
import com.giveitup.giveitup_be.entity.OrganizationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {
    OrganizationResponse toOrganizationResponse(OrganizationEntity entity);
    @Mapping(target = "category", ignore = true )
    @Mapping(target = "organizationLogo", ignore = true)      // bỏ map MultipartFile
    @Mapping(target = "verificationFile", ignore = true)
    void updateAuthor(@MappingTarget OrganizationEntity organizationEntity, AuthorCreationRequest request);

}

