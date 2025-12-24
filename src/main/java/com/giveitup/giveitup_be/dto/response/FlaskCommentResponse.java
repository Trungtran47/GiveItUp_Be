package com.giveitup.giveitup_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FlaskCommentResponse {
     String text;
     String label;
     Double confidence;
     boolean alert;
}