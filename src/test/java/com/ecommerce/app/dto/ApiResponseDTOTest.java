package com.ecommerce.app.dto;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseDTOTest {
    @Test
    void testSuccessWithData() {
        String data = "Test Data";
        ApiResponseDTO<String> response = ApiResponseDTO.success(data);
        assertTrue(response.isSuccess());
        assertEquals("Success", response.getMessage());
        assertEquals(data, response.getData());
        assertEquals(200, response.getStatus());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testSuccessWithMessage() {
        String message = "Operation completed";
        ApiResponseDTO<Void> response = ApiResponseDTO.success(message);
        assertTrue(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(200, response.getStatus());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testSuccessWithMessageAndData() {
        String message = "Created";
        Integer data = 123;
        ApiResponseDTO<Integer> response = ApiResponseDTO.success(message, data);
        assertTrue(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(data, response.getData());
        assertEquals(200, response.getStatus());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testErrorWithMessage() {
        String message = "Something went wrong";
        int status = 400;
        ApiResponseDTO<Void> response = ApiResponseDTO.error(message, status);
        assertFalse(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(status, response.getStatus());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testErrorWithMessageAndErrors() {
        String message = "Validation failed";
        List<String> errors = Arrays.asList("Field A required", "Field B invalid");
        int status = 422;
        ApiResponseDTO<Void> response = ApiResponseDTO.error(message, errors, status);
        assertFalse(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(errors, response.getErrors());
        assertEquals(status, response.getStatus());
        assertNotNull(response.getTimestamp());
    }
}
