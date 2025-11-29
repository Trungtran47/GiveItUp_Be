package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.dto.response.BankAccountResponse;
import com.giveitup.giveitup_be.entity.BankAccountEntity;
import com.giveitup.giveitup_be.entity.CategoryEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccountEntity, Long>, JpaSpecificationExecutor<BankAccountEntity> {
    List<BankAccountEntity> findAllByUser_Id(Long id);
}
