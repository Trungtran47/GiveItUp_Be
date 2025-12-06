package com.giveitup.giveitup_be.mapper;

import com.giveitup.giveitup_be.dto.response.PayoutResponse;
import com.giveitup.giveitup_be.entity.PayoutEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PayoutMapper {

    @Mapping(source = "requestedBy.id",   target = "requestedBy")
    @Mapping(source = "createdByAdmin.id", target = "createdByAdmin")
    PayoutResponse toPayoutResponse(PayoutEntity payout);
}
