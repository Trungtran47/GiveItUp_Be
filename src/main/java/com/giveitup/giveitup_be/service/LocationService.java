package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.entity.ProvinceEntity;
import com.giveitup.giveitup_be.entity.WardEntity;
import com.giveitup.giveitup_be.repository.ProvinceRepository;
import com.giveitup.giveitup_be.repository.WardRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {
    private final ProvinceRepository provinceRepository;
    private final WardRepository wardRepository;
    public LocationService(ProvinceRepository provinceRepository, WardRepository wardRepository) {
        this.provinceRepository = provinceRepository;
        this.wardRepository = wardRepository;
    }
    public List<ProvinceEntity> getAllProvinces() {
        return provinceRepository.findAll();
    }
    public List<WardEntity> getAllWards() {
        return wardRepository.findAll();
    }
    public List<WardEntity> getWardsByProvince(Integer provinceId) {
        return wardRepository.findByProvinceId(provinceId);
    }

}