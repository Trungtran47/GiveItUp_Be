package com.giveitup.giveitup_be.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giveitup.giveitup_be.service.AuthenticationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    @Lazy
    private final AuthenticationService authenticationService;
    // Inject service này để lấy Access Token
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final RestTemplate restTemplate = new RestTemplate(); // Hoặc inject Bean

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        // 1. Lấy thông tin cơ bản
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String photoUrl = oAuth2User.getAttribute("picture");

//        // 2. Lấy Access Token để gọi People API
//        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
//                oauthToken.getAuthorizedClientRegistrationId(),
//                oauthToken.getName());

//        String accessToken = client.getAccessToken().getTokenValue();
//
//        // 3. Gọi Google People API để lấy DOB, Gender, Phone
//        // personFields: các trường muốn lấy
//        String url = "https://people.googleapis.com/v1/people/me?personFields=birthdays,genders,phoneNumbers";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(accessToken);
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        LocalDate dob = null;
//        String gender = null; // Hoặc Long nếu map sang ID
//        String phoneNumber = null;
//
//        try {
//            ResponseEntity<String> apiResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode root = mapper.readTree(apiResponse.getBody());
//
//            // --- XỬ LÝ NGÀY SINH ---
//            if (root.has("birthdays")) {
//                JsonNode dateNode = root.get("birthdays").get(0).get("date");
//                if (dateNode != null && dateNode.has("year") && dateNode.has("month") && dateNode.has("day")) {
//                    int year = dateNode.get("year").asInt();
//                    int month = dateNode.get("month").asInt();
//                    int day = dateNode.get("day").asInt();
//                    dob = LocalDate.of(year, month, day);
//                }
//            }
//
//            // --- XỬ LÝ GIỚI TÍNH ---
//            if (root.has("genders")) {
//                String genderValue = root.get("genders").get(0).get("value").asText();
//                // Google trả về "male", "female", "unspecified"
//                // Bạn cần map sang code Long của bạn (VD: 1=Nam, 2=Nữ)
//                gender = genderValue;
//            }
//
//            // --- XỬ LÝ SỐ ĐIỆN THOẠI (Thường sẽ null) ---
//            if (root.has("phoneNumbers")) {
//                phoneNumber = root.get("phoneNumbers").get(0).get("value").asText();
//            }
//
//        } catch (Exception e) {
//            log.error("Không thể lấy thông tin chi tiết từ Google People API: {}", e.getMessage());
//            // Không throw lỗi, vẫn cho login bình thường với thông tin cơ bản
//        }

        if (email == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Không tìm thấy Email từ Google");
            return;
        }

        // 4. Truyền tất cả vào Service xử lý
        String token = authenticationService.handleGoogleLogin(email, name, photoUrl
//                , dob, gender, phoneNumber
        );

        response.sendRedirect("http://localhost:3000/oauth-success?token=" + token);
    }
}