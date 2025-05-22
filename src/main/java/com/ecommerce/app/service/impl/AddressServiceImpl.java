package com.ecommerce.app.service.impl;

import com.ecommerce.app.dto.AddressDTO;
import com.ecommerce.app.model.Address;
import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.AddressRepository;
import com.ecommerce.app.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Override
    public List<Address> getAddressByUser(User user) {
        return addressRepository.findByUserOrderByIsDefaultDesc(user);
    }

    @Override
    public Optional<Address> getAddressById(Long id) {
        return addressRepository.findById(id);
    }

    @Override
    @Transactional
    public Address saveAddress(Address address) {
        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public Address updateAddress(Address address) {
        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }

    @Override
    public Optional<Address> getDefaultAddress(User user) {
        return addressRepository.findByUserAndIsDefaultTrue(user);
    }

    @Override
    @Transactional
    public Address setDefaultAddress(User user, Long addressId) {
        // First, set all user's addresses to non-default
        List<Address> userAddresses = addressRepository.findByUser(user);
        for (Address address : userAddresses) {
            address.setIsDefault(false);
            addressRepository.save(address);
        }
        
        // Then set the specified address as default
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new RuntimeException("Address not found with ID: " + addressId));
        
        // Verify address belongs to user
        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Address does not belong to user");
        }
        
        address.setIsDefault(true);
        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public Address createAddressFromDTO(User user, AddressDTO addressDTO) {
        Address address = new Address();
        address.setUser(user);
        address.setAddressType(addressDTO.getAddressType());
        address.setStreetAddress(addressDTO.getStreetAddress());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setPostalCode(addressDTO.getPostalCode());
        address.setCountry(addressDTO.getCountry());
        
        // If this is the first address or request to make it default
        if (addressDTO.getIsDefault() != null && addressDTO.getIsDefault()) {
            // Set all existing addresses to non-default
            List<Address> userAddresses = addressRepository.findByUser(user);
            for (Address existingAddress : userAddresses) {
                existingAddress.setIsDefault(false);
                addressRepository.save(existingAddress);
            }
            address.setIsDefault(true);
        } else {
            // Check if user has any addresses
            List<Address> userAddresses = addressRepository.findByUser(user);
            if (userAddresses.isEmpty()) {
                // If this is the first address, make it default
                address.setIsDefault(true);
            } else {
                address.setIsDefault(false);
            }
        }
        
        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public Address updateAddressFromDTO(Long addressId, AddressDTO addressDTO) {
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new RuntimeException("Address not found with ID: " + addressId));
        
        address.setAddressType(addressDTO.getAddressType());
        address.setStreetAddress(addressDTO.getStreetAddress());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setPostalCode(addressDTO.getPostalCode());
        address.setCountry(addressDTO.getCountry());
        
        // Handle default address status if provided
        if (addressDTO.getIsDefault() != null && addressDTO.getIsDefault() && !address.getIsDefault()) {
            // Set all user's addresses to non-default
            List<Address> userAddresses = addressRepository.findByUser(address.getUser());
            for (Address existingAddress : userAddresses) {
                existingAddress.setIsDefault(false);
                addressRepository.save(existingAddress);
            }
            address.setIsDefault(true);
        }
        
        return addressRepository.save(address);
    }
}
