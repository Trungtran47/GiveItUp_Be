package com.giveitup.giveitup_be.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    private static final String DEFAULT_FOLDER = "GiveItUp/images";

    // ✅ Upload ảnh (image) và trả về URL + public_id
    public Map<String, String> uploadImage(MultipartFile file, String folder) {
        Map<String, String> result = new HashMap<>();
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder", folder
                    )
            );
            result.put("url", (String) uploadResult.get("secure_url"));
            result.put("public_id", (String) uploadResult.get("public_id"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
        return result;
    }

    // ✅ Upload file tài liệu (pdf, doc, docx) và trả về URL + public_id
    public Map<String, String> uploadFile(MultipartFile file, String folder) {
        Map<String, String> result = new HashMap<>();
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "raw", // "raw" dùng cho file
                            "folder", folder
                    )
            );
            result.put("url", (String) uploadResult.get("secure_url"));
            result.put("public_id", (String) uploadResult.get("public_id"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
        return result;
    }

    // ✅ Xóa ảnh hoặc file theo public_id
    public void delete(String publicId, String resourceType) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete from Cloudinary", e);
        }
    }
    // ✅ Xóa file hoặc ảnh theo public_id
    public void deleteFile(String publicId, boolean isImage) {
        if (publicId == null || publicId.isEmpty()) return;

        try {
            String resourceType = isImage ? "image" : "raw"; // ảnh hay file
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
        } catch (IOException e) {
            throw new RuntimeException("Xóa file thất bại", e);
        }
    }


}
