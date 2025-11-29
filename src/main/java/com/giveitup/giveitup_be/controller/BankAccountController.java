package com.giveitup.giveitup_be.controller;

import com.giveitup.giveitup_be.dto.request.ApiResponse;
import com.giveitup.giveitup_be.dto.request.BankAccountRequest;
import com.giveitup.giveitup_be.dto.request.PostRequest;
import com.giveitup.giveitup_be.dto.response.BankAccountResponse;
import com.giveitup.giveitup_be.dto.response.PostResponse;
import com.giveitup.giveitup_be.service.BankAccountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bank_account")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BankAccountController {
    BankAccountService bankAccountService;

    @PostMapping(path = "/create")
    public ApiResponse<BankAccountResponse> createBA(@ModelAttribute BankAccountRequest request) {
        return ApiResponse.<BankAccountResponse>builder()
                .result(bankAccountService.createBankAccount(request))
                .build();
    }
    @PutMapping(path = "/update")
    public ApiResponse<BankAccountResponse> updateBA(@ModelAttribute BankAccountRequest request) {
        return ApiResponse.<BankAccountResponse>builder()
                .result(bankAccountService.updateBankAccount(request))
                .build();
    }
    @GetMapping(path = "/getBy_userId/{userId}")
    public ApiResponse<List<BankAccountResponse>> getBAbyUserId(@PathVariable Long userId) {
        return ApiResponse.<List<BankAccountResponse>>builder()
                .result(bankAccountService.getBankAccountsByUserId(userId))
                .build();
    }
    @GetMapping(path = "/{baId}")
    public ApiResponse<BankAccountResponse> getBA(@PathVariable Long baId) {
        return ApiResponse.<BankAccountResponse>builder()
                .result(bankAccountService.getBankAccount(baId))
                .build();
    }

}
