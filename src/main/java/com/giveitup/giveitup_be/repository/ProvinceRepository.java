package com.giveitup.giveitup_be.repository;


import com.giveitup.giveitup_be.entity.ProvinceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvinceRepository extends JpaRepository<ProvinceEntity, Integer > {
}
