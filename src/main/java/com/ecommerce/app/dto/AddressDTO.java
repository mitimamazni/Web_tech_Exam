package com.ecommerce.app.dto;

import com.ecommerce.app.model.Address;
import com.ecommerce.app.model.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private Long id;
    private AddressType addressType;
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isDefault;
    
    // Convert from Entity to DTO
    public static AddressDTO fromEntity(Address address) {
        if (address == null) {
            return null;
        }
        
        return AddressDTO.builder()
            .id(address.getId())
            .addressType(address.getAddressType())
            .streetAddress(address.getStreetAddress())
            .city(address.getCity())
            .state(address.getState())
            .postalCode(address.getPostalCode())
            .country(address.getCountry())
            .isDefault(address.getIsDefault())
            .build();
    }
    
    // Convert list of entities to list of DTOs
    public static List<AddressDTO> fromEntities(List<Address> addresses) {
        if (addresses == null) {
            return new ArrayList<>();
        }
        
        return addresses.stream()
            .map(AddressDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
