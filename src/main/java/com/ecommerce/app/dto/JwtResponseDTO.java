package com.ecommerce.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponseDTO {
    private String tokenType = "Bearer";
    private String accessToken;
    private String refreshToken;
    private Long id;
    private String username;
    private String email;
    private String[] roles;
}
