package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.entity.BankAccountEntity;
import com.giveitup.giveitup_be.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository

public interface ImageRepository  extends JpaRepository<ImageEntity, Long>  {
    void deleteImageByPostId(Long postId);
}

