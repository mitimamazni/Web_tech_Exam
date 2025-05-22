package com.ecommerce.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false)
    private AddressType addressType = AddressType.BOTH;

    @NotBlank
    @Size(max = 255)
    @Column(name = "street_address", nullable = false)
    private String streetAddress;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String city;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String state;

    @NotBlank
    @Size(max = 20)
    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String country;

    @Column(name = "is_default")
    private Boolean isDefault = false;
}
