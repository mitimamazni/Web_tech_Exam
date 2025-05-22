package com.ecommerce.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDTO<T> {
    private boolean success;
    private String message;
    private T data;
    private List<String> errors;
    private int status;
    private String path;
    private LocalDateTime timestamp;
    
    // Success response with data
    public static <T> ApiResponseDTO<T> success(T data) {
        return ApiResponseDTO.<T>builder()
            .success(true)
            .message("Success")
            .data(data)
            .status(200)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    // Success response with message
    public static <T> ApiResponseDTO<T> success(String message) {
        return ApiResponseDTO.<T>builder()
            .success(true)
            .message(message)
            .status(200)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    // Success response with message and data
    public static <T> ApiResponseDTO<T> success(String message, T data) {
        return ApiResponseDTO.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .status(200)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    // Error response with message
    public static <T> ApiResponseDTO<T> error(String message, int status) {
        return ApiResponseDTO.<T>builder()
            .success(false)
            .message(message)
            .status(status)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    // Error response with message and errors list
    public static <T> ApiResponseDTO<T> error(String message, List<String> errors, int status) {
        return ApiResponseDTO.<T>builder()
            .success(false)
            .message(message)
            .errors(errors)
            .status(status)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
