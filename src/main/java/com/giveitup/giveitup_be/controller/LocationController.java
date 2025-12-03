package com.giveitup.giveitup_be.controller;


import com.giveitup.giveitup_be.dto.request.ApiResponse;
import com.giveitup.giveitup_be.dto.response.LikeResponse;
import com.giveitup.giveitup_be.entity.ProvinceEntity;
import com.giveitup.giveitup_be.entity.WardEntity;
import com.giveitup.giveitup_be.service.LocationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/location")
public class LocationController {
      LocationService locationService;
    // Lấy tất cả tỉnh
    @GetMapping("/provinces")
    public ApiResponse<List<ProvinceEntity>> getProvinces() {
        return ApiResponse.<List<ProvinceEntity>>builder()
                .result(locationService.getAllProvinces())
                .build();
    }
    // Lấy tất cả phường/xã
    @GetMapping("/wards")
    public ApiResponse<List<WardEntity>> getWards() {
        return ApiResponse.<List<WardEntity>>builder()
                .result(locationService.getAllWards())
                .build();
    }

    // Lấy phường/xã theo tỉnh
    @GetMapping("/wards/{provinceId}")
    public ApiResponse<List<WardEntity>> getWardsByProvince(@PathVariable Integer  provinceId) {
        return ApiResponse.<List<WardEntity>>builder()
                .result(locationService.getWardsByProvince(provinceId))
                .build();
    }
}
