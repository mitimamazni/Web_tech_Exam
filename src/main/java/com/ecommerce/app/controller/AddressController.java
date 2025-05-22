package com.ecommerce.app.controller;

import com.ecommerce.app.dto.AddressDTO;
import com.ecommerce.app.model.Address;
import com.ecommerce.app.model.AddressType;
import com.ecommerce.app.model.User;
import com.ecommerce.app.service.AddressService;
import com.ecommerce.app.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressService addressService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/add")
    public String showAddAddressForm(Model model) {
        model.addAttribute("address", new AddressDTO());
        model.addAttribute("addressTypes", AddressType.values());
        return "user/address-form";
    }
    
    @PostMapping("/add")
    public String addAddress(
            @Valid @ModelAttribute("address") AddressDTO addressDTO,
            BindingResult result,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("addressTypes", AddressType.values());
            return "user/address-form";
        }
        
        // Get username from session
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        
        // Find the user
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        try {
            addressService.createAddressFromDTO(user, addressDTO);
            redirectAttributes.addFlashAttribute("success", "Address added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding address: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
    
    @GetMapping("/edit/{id}")
    public String showEditAddressForm(@PathVariable Long id, Model model, HttpSession session) {
        // Get username from session
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        
        // Find the user
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get the address
        Address address = addressService.getAddressById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        
        // Verify the address belongs to the user
        if (!address.getUser().getId().equals(user.getId())) {
            return "redirect:/profile";
        }
        
        model.addAttribute("address", AddressDTO.fromEntity(address));
        model.addAttribute("addressTypes", AddressType.values());
        return "user/address-form";
    }
    
    @PostMapping("/edit/{id}")
    public String editAddress(
            @PathVariable Long id,
            @Valid @ModelAttribute("address") AddressDTO addressDTO,
            BindingResult result,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("addressTypes", AddressType.values());
            return "user/address-form";
        }
        
        // Get username from session
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        
        // Find the user
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get the address
        Address address = addressService.getAddressById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        
        // Verify the address belongs to the user
        if (!address.getUser().getId().equals(user.getId())) {
            return "redirect:/profile";
        }
        
        try {
            addressService.updateAddressFromDTO(id, addressDTO);
            redirectAttributes.addFlashAttribute("success", "Address updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating address: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
    
    @PostMapping("/delete/{id}")
    public String deleteAddress(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // Get username from session
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        
        // Find the user
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get the address
        Address address = addressService.getAddressById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        
        // Verify the address belongs to the user
        if (!address.getUser().getId().equals(user.getId())) {
            return "redirect:/profile";
        }
        
        try {
            addressService.deleteAddress(id);
            redirectAttributes.addFlashAttribute("success", "Address deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting address: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
    
    @PostMapping("/set-default/{id}")
    public String setDefaultAddress(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // Get username from session
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        
        // Find the user
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        try {
            addressService.setDefaultAddress(user, id);
            redirectAttributes.addFlashAttribute("success", "Default address updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error setting default address: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
}
