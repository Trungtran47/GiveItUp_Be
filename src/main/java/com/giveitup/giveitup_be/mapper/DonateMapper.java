package com.giveitup.giveitup_be.mapper;

import com.giveitup.giveitup_be.dto.request.DonateRequest;
import com.giveitup.giveitup_be.dto.response.DonateResponse;
import com.giveitup.giveitup_be.entity.DonateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, PostMapper.class, PayoutMapper.class})
public interface DonateMapper {

    @Mapping(target = "post.payouts", ignore = true)
    DonateResponse toDonateResponse(DonateEntity donateEntity);

    DonateEntity toDonate(DonateRequest request);
}
