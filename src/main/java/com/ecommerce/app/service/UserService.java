package com.ecommerce.app.service;

import com.ecommerce.app.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(User user, boolean isAdmin);
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findById(Long id);
    
    List<User> findAllUsers();
    
    Page<User> getAllUsersPaged(Pageable pageable);
    
    User updateUser(User user);
    
    void updateUserRole(Long userId, String roleName);
    
    void deleteUser(Long userId);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}
