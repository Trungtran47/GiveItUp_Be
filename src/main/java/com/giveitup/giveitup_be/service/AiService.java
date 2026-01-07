package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.response.FlaskChatbotResponse;
import com.giveitup.giveitup_be.dto.response.FlaskCommentResponse;
import lombok.RequiredArgsConstructor; // 1. Import Lombok
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor // 2. Thêm Annotation này để tự động Inject
public class AiService {
    private final String FLASK_URL = "http://localhost:5000";
    private final RestTemplate restTemplate;

    // 1. Check Comment
    public FlaskCommentResponse checkComment(String comment) {
        String url = FLASK_URL + "/predict";

        // Tạo body JSON gửi đi: {"text": "nội dung comment"}
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", comment);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            return restTemplate.postForObject(url, request, FlaskCommentResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 2. Chatbot
    public FlaskChatbotResponse askChatbot(String message) {
        String url = FLASK_URL + "/ask";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("message", message);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
        try {
            return restTemplate.postForObject(url, request, FlaskChatbotResponse.class);
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi Chatbot AI: " + e.getMessage());
            e.printStackTrace();
            return FlaskChatbotResponse.builder()
                    .answer("Xin lỗi, hệ thống AI đang bận hoặc gặp sự cố. Vui lòng thử lại sau.")
                    .sql("")
                    .rawData(null)
                    .build();
        }
    }
}