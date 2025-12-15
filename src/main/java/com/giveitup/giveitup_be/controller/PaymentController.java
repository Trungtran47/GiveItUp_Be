package com.giveitup.giveitup_be.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.giveitup.giveitup_be.dto.request.payment.CreateQrRequest;
import com.giveitup.giveitup_be.dto.request.payment.PayOSWebhookBodyPayload;
import com.giveitup.giveitup_be.service.PaymentService;
import com.giveitup.giveitup_be.util.PayOSSignatureUtil;
import com.giveitup.giveitup_be.util.PayOSUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/")
public class PaymentController {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${payos.clientId}")
    private String clientId;
    @Value("${payos.apiKey}")
    private String apiKey;
    @Value("${payos.checksumKey}")
    private String checksumKey;
    @Value("${payos.createPaymentUrl}")
    private String payosCreateUrl;


    // React gọi endpoint này để tạo order và lấy QR
    @PostMapping("payment/createQr")
    public ResponseEntity<?> createQr(@RequestBody CreateQrRequest request) {
        try {
            String url = "https://api-merchant.payos.vn/v2/payment-requests";

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            paymentService.createOrder(String.valueOf(request.getOrderCode()));
            // 1. Tạo object để tạo signature
            Map<String, Object> dataForSignature = new HashMap<>();
            dataForSignature.put("amount", request.getAmount());
            dataForSignature.put("description", request.getDescription());
            dataForSignature.put("orderCode", request.getOrderCode());
            dataForSignature.put("cancelUrl", "https://example.com/cancel");
            dataForSignature.put("returnUrl", "https://example.com/return");

            // 2. Tạo chữ ký
            String signature = PayOSUtils.generateSignature(dataForSignature, checksumKey);

            // 3. Payload cuối cùng gửi đi
            Map<String, Object> payload = new HashMap<>(dataForSignature);
            payload.put("signature", signature);

            HttpEntity<Object> entity = new HttpEntity<>(payload, headers);

            RestTemplate rest = new RestTemplate();
            Map response = rest.postForObject(url, entity, Map.class);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // SSE endpoint để UI subscribe trạng thái order
    @GetMapping(path = "sse/payment/status/{orderCode}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamOrder(@PathVariable String orderCode) {
        SseEmitter emitter = paymentService.createEmitter(orderCode);
        try {
            String status = paymentService.getStatus(orderCode);
            emitter.send(SseEmitter.event().name("order-status").data(status));
        } catch (Exception e) {
            // Nếu gửi thất bại ngay lúc connect (rất hiếm), xóa luôn
            emitter.completeWithError(e);
        }
        return emitter;
    }

    // Webhook PayOS sẽ gọi endpoint này khi giao dịch thay đổi
    @PostMapping("/webhook/payos")
    public ResponseEntity<String> handleWebhook(@RequestBody PayOSWebhookBodyPayload payload) {
        Map<String, Object> data = payload.getData();
        String signature = payload.getSignature();

        if (!PayOSSignatureUtil.verifyWebhookSignature(data, signature, checksumKey)) {
            System.err.println("Invalid signature! received: " + signature);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }

        // Lấy orderCode
        String orderCode = String.valueOf(data.get("orderCode"));

        // Dùng code của PayOS để xác định trạng thái
        String code = String.valueOf(data.get("code"));
        String status;
        if ("00".equals(code)) {
            status = "SUCCESS";
        } else {
            status = "FAILED";
        }
        if (orderCode != null ) {
            paymentService.setStatus(orderCode, status);
            System.out.println("Updated order " + orderCode + " to status " + status);
        }

        return ResponseEntity.ok("OK");
    }



    // Hủy order (ví dụ)
    @PostMapping("payment/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderId) {
        try {
            String url = "https://api-merchant.payos.vn/v2/payment-requests/" + orderId + "/cancel";
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Build signature body
            Map<String, Object> data = Map.of("orderCode", orderId);
            String signature = PayOSUtils.generateSignature(data, checksumKey);
            Map<String, Object> body = Map.of(
                    "orderCode", orderId,
                    "signature", signature
            );
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            Map res = restTemplate.postForObject(url, entity, Map.class);
            // cập nhật status local + bắn SSE
            paymentService.setStatus(orderId, "CANCELLED");
            return ResponseEntity.ok(res);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

}
