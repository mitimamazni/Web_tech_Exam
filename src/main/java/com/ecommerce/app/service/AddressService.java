package com.ecommerce.app.service;

import com.ecommerce.app.dto.AddressDTO;
import com.ecommerce.app.model.Address;
import com.ecommerce.app.model.User;

import java.util.List;
import java.util.Optional;

public interface AddressService {
    /**
     * Get all addresses for a user
     * @param user The user
     * @return List of addresses
     */
    List<Address> getAddressByUser(User user);
    
    /**
     * Get address by ID
     * @param id Address ID
     * @return Optional Address
     */
    Optional<Address> getAddressById(Long id);
    
    /**
     * Save a new address
     * @param address Address to save
     * @return Saved address
     */
    Address saveAddress(Address address);
    
    /**
     * Update an existing address
     * @param address Address to update
     * @return Updated address
     */
    Address updateAddress(Address address);
    
    /**
     * Delete an address
     * @param id Address ID to delete
     */
    void deleteAddress(Long id);
    
    /**
     * Get user's default address
     * @param user The user
     * @return Optional default address
     */
    Optional<Address> getDefaultAddress(User user);
    
    /**
     * Set an address as default
     * @param user The user
     * @param addressId Address ID to set as default
     * @return The updated address
     */
    Address setDefaultAddress(User user, Long addressId);
    
    /**
     * Create a new address from DTO
     * @param user The user
     * @param addressDTO Address DTO with data
     * @return Created address
     */
    Address createAddressFromDTO(User user, AddressDTO addressDTO);
    
    /**
     * Update address from DTO
     * @param addressId Address ID to update
     * @param addressDTO Address DTO with new data
     * @return Updated address
     */
    Address updateAddressFromDTO(Long addressId, AddressDTO addressDTO);
}
