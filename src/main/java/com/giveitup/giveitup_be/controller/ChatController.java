package com.giveitup.giveitup_be.controller;

import com.giveitup.giveitup_be.dto.request.ApiResponse;
import com.giveitup.giveitup_be.dto.response.FlaskChatbotResponse;
import com.giveitup.giveitup_be.service.AiService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ChatController {
     AiService aiService;

    @PostMapping("/ask")
    public ApiResponse<FlaskChatbotResponse> askBot(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập nội dung câu hỏi!");
        }
        FlaskChatbotResponse response = aiService.askChatbot(userMessage);
        return ApiResponse.<FlaskChatbotResponse>builder()
                .result(response)
                .build();
    }
}