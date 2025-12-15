package com.giveitup.giveitup_be.mapper;

import com.giveitup.giveitup_be.dto.request.BankAccountRequest;
import com.giveitup.giveitup_be.dto.response.BankAccountResponse;
import com.giveitup.giveitup_be.entity.BankAccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {
    @Mapping(target = "organization", ignore = true)
    BankAccountEntity toBankAccount(BankAccountRequest request);
    @Mapping(source = "organization.id", target = "organizationId")
    BankAccountResponse toBankAccountResponse(BankAccountEntity bankAccountEntity);
    List<BankAccountResponse> toListBankAccountResponse(List<BankAccountEntity> bankAccountEntities);
//    void updateBankAccount(@MappingTarget BankAccountEntity bankAccountEntity, BankAccountRequest request);
}
