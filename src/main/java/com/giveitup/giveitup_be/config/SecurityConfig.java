//package com.giveitup.giveitup_be.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable()) // tắt CSRF (nếu không dùng form login)
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                                "/swagger-ui/**",
//                                "/v3/api-docs/**",
//                                "/v3/api-docs.yaml"
//                        ).permitAll()
//                        .anyRequest().authenticated()
//                );
//
//        return http.build();
//    }
//}
