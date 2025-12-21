package com.giveitup.giveitup_be.configwebsocket;

import com.giveitup.giveitup_be.configuration.CustomJwtDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99) // Chạy trước các security filter mặc định
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final CustomJwtDecoder customJwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter; // Reuse bean từ SecurityConfig

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint: http://localhost:8080/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Cho phép Next.js/React truy cập
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Client subscribe vào: /topic/.. hoặc /queue/..
        registry.enableSimpleBroker("/topic", "/queue");
        // Client gửi message lên: /app/..
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Interceptor để bắt lấy Token từ STOMP Header khi Client CONNECT
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                // Chỉ kiểm tra khi Client gửi lệnh CONNECT
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {

                    // Lấy Header "Authorization" trong gói tin STOMP
                    List<String> authorization = accessor.getNativeHeader("Authorization");

                    if (authorization != null && !authorization.isEmpty()) {
                        String token = authorization.get(0);
                        if (token.startsWith("Bearer ")) {
                            token = token.substring(7);
                        }

                        try {
                            // 1. Decode token bằng CustomJwtDecoder của bạn
                            Jwt jwt = customJwtDecoder.decode(token);

                            // 2. Convert JWT thành Authentication object (chứa Roles/Authorities)
                            Authentication authentication = jwtAuthenticationConverter.convert(jwt);

                            // 3. Set User vào Session của WebSocket
                            accessor.setUser(authentication);

                            log.info("User connected via WebSocket: {}", authentication.getName());
                        } catch (Exception e) {
                            log.error("WebSocket Authentication Failed: {}", e.getMessage());
                            // Có thể throw exception để từ chối kết nối nếu muốn
                        }
                    }
                }
                return message;
            }
        });
    }
}