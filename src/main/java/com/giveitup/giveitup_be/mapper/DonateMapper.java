package com.giveitup.giveitup_be.mapper;

import com.giveitup.giveitup_be.dto.request.DonateRequest;
import com.giveitup.giveitup_be.dto.response.DonateResponse;
import com.giveitup.giveitup_be.dto.response.DonateSummary;
import com.giveitup.giveitup_be.entity.DonateEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface DonateMapper {
    DonateEntity toDonate(DonateRequest request);
    DonateResponse toDonateResponse(DonateEntity donateEntity);

}
