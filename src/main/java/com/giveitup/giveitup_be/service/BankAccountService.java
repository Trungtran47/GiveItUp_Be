package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.request.BankAccountRequest;
import com.giveitup.giveitup_be.dto.response.BankAccountResponse;
import com.giveitup.giveitup_be.entity.BankAccountEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import com.giveitup.giveitup_be.enums.BankAccountStatus;
import com.giveitup.giveitup_be.exception.AppException;
import com.giveitup.giveitup_be.exception.ErrorCode;
import com.giveitup.giveitup_be.mapper.BankAccountMapper;
import com.giveitup.giveitup_be.repository.BankAccountRepository;
import com.giveitup.giveitup_be.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BankAccountService {
    BankAccountRepository bankAccountRepository;
    BankAccountMapper bankAccountMapper;
    UserRepository userRepository;
    @PreAuthorize("hasRole('AUTHOR')")
    public BankAccountResponse createBankAccount(BankAccountRequest request) {
        BankAccountEntity bankAccountEntity = bankAccountMapper.toBankAccount(request);
        UserEntity userEntity = userRepository.findById(request.getUser()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        bankAccountEntity.setUser(userEntity);
        bankAccountEntity.setStatus(BankAccountStatus.ACTIVE.getCode());
        try {
            bankAccountEntity = bankAccountRepository.save(bankAccountEntity);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.BANK_ACCOUNT_EXISTED);
        }

        return bankAccountMapper.toBankAccountResponse(bankAccountEntity);
    }
    @PreAuthorize("hasRole('AUTHOR')")
    public BankAccountResponse updateBankAccount(BankAccountRequest request) {
        BankAccountEntity bankAccountEntity = bankAccountRepository.findById(request.getId()).orElseThrow(() -> new AppException(ErrorCode.BANK_ACCOUNT_NOT_EXISTED));
        UserEntity userEntity = userRepository.findById(request.getUser()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        bankAccountEntity.setUser(userEntity);
        bankAccountEntity.setStatus(BankAccountStatus.ACTIVE.getCode());
        bankAccountEntity.setBankAccountNumber(request.getBankAccountNumber());
        bankAccountEntity.setAccountCode(request.getAccountCode());
        bankAccountEntity.setAccountHolderName(request.getAccountHolderName());
        bankAccountEntity.setBankName(request.getBankName());
        bankAccountEntity = bankAccountRepository.save(bankAccountEntity);
        return bankAccountMapper.toBankAccountResponse(bankAccountEntity);
    }
    @PreAuthorize("hasRole('AUTHOR')")
    public void deleteBankAccount(Long id) {
            bankAccountRepository.deleteById(id);

    }
    public BankAccountResponse getBankAccount(Long id) {
        return bankAccountMapper.toBankAccountResponse(
                bankAccountRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BANK_ACCOUNT_NOT_EXISTED)));
    }
    public List<BankAccountResponse> getBankAccountsByUserId(Long userId  ) {
        return bankAccountMapper.toListBankAccountResponse(
                bankAccountRepository.findAllByUser_Id(userId));
    }
//    public Page<UserResponse> getAllBankAccounts(SearchListUserRequest request) {
//        Specification<UserEntity> spec = Specification.allOf(
//                UserSpecification.hasUsername(request.getUserName()),
//                UserSpecification.hasPhoneNumber(request.getPhoneNumber())
//        );
//        int pageIndex = Math.max(request.getCurrentPage() - 1, 0);
//        Pageable pageable = PageRequest.of(
//                pageIndex,
//                request.getPageSize(),
//                Sort.by("username").ascending()
//        );
//
//        Page<UserEntity> page = userRepository.findAll(spec, pageable);
//        log.info("Found {} users", page.getTotalElements());
//
//        return page.map(userMapper::toUserResponse);
//
//    }

}
