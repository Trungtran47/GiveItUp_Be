package com.giveitup.giveitup_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.giveitup.giveitup_be.entity.WardEntity;

import java.util.List;

@Repository
public interface WardRepository extends JpaRepository<WardEntity, Integer > {
    List<WardEntity> findByProvinceId(Integer provinceId); // Lấy theo tỉnh nếu cần
}