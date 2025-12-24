package com.giveitup.giveitup_be.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlaskChatbotResponse {

    private String sql;

    // Map trường "raw_data" bên Python sang biến "rawData" bên Java
    @JsonProperty("raw_data")
    private Object rawData;

    private String answer;
}