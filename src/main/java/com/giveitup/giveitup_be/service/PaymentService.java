package com.giveitup.giveitup_be.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentService {
    // Trạng thái từng order
    private final Map<String, String> orderStatus = new ConcurrentHashMap<>();
    // Mỗi orderCode chỉ có 1 client SSE
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // ---------------------------
    // Quản lý order
    // ---------------------------
    public String createOrder(String orderId) {
        orderStatus.put(orderId, "PENDING");
        return orderId;
    }

    public String getStatus(String orderId) {
        return orderStatus.getOrDefault(orderId, "NOT_FOUND");
    }
    public void setStatus(String orderId, String status) {
        orderStatus.put(orderId, status);
        // push ngay cho emitter (nếu có)
        SseEmitter emitter = emitters.get(orderId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("order-status")
                        .data(status));
            } catch (Exception e) {
//                emitter.completeWithError(e);
//                emitters.remove(orderId);
                // Client đã disconnect → xóa emitter
                emitters.remove(orderId);
                emitter.complete();
                // Có thể log info, không nên log lỗi full stack
                System.out.println("SSE client disconnected: " + orderId);
            }
        }
    }
    // ---------------------------
    // SSE
    // ---------------------------
    public SseEmitter createEmitter(String orderId) {
        SseEmitter emitter = new SseEmitter(0L); // infinite timeout
        emitters.put(orderId, emitter);

        emitter.onCompletion(() -> emitters.remove(orderId));
        emitter.onTimeout(() -> emitters.remove(orderId));
        emitter.onError((e) -> emitters.remove(orderId));

        return emitter;
    }
}
