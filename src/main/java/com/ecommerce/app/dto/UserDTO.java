package com.ecommerce.app.dto;

import com.ecommerce.app.model.Role;
import com.ecommerce.app.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String phoneNumber;
    private LocalDateTime createdAt;
    
    @Builder.Default
    private List<AddressDTO> addresses = new ArrayList<>();
    
    @Builder.Default
    private List<String> roles = new ArrayList<>();
    
    // Convert from Entity to DTO
    public static UserDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }
        
        List<String> roleNames = user.getRoles() != null
            ? user.getRoles().stream()
                .map(Role::getNameAsString)
                .collect(Collectors.toList())
            : new ArrayList<>();
            
        return UserDTO.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .username(user.getUsername())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .createdAt(user.getCreatedAt())
            .addresses(AddressDTO.fromEntities(user.getAddresses()))
            .roles(roleNames)
            .build();
    }
    
    // Convert list of entities to list of DTOs
    public static List<UserDTO> fromEntities(List<User> users) {
        if (users == null) {
            return new ArrayList<>();
        }
        
        return users.stream()
            .map(UserDTO::fromEntity)
            .collect(Collectors.toList());
    }
    
    // Get full name
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
