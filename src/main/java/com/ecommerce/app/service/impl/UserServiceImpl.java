package com.ecommerce.app.service.impl;

import com.ecommerce.app.model.ERole;
import com.ecommerce.app.model.Role;
import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.RoleRepository;
import com.ecommerce.app.repository.UserRepository;
import com.ecommerce.app.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public User registerUser(User user, boolean isAdmin) {
        // We're not using Spring Security anymore, so we're storing passwords as is
        // In a real application, you would want to use a proper password hashing method here
        // like BCrypt even without Spring Security
        
        Set<Role> roles = new HashSet<>();

        // Always add ROLE_USER
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseGet(() -> {
                    Role newRole = new Role(ERole.ROLE_USER);
                    return roleRepository.save(newRole);
                });
        roles.add(userRole);

        // Add ROLE_ADMIN if requested
        if (isAdmin) {
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseGet(() -> {
                        Role newRole = new Role(ERole.ROLE_ADMIN);
                        return roleRepository.save(newRole);
                    });
            roles.add(adminRole);
        }

        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    
    @Override
    public Page<User> getAllUsersPaged(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void updateUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        ERole eRole;
        try {
            eRole = ERole.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role name: " + roleName);
        }
        
        Role role = roleRepository.findByName(eRole)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        
        Set<Role> roles = user.getRoles();
        
        // If adding ROLE_ADMIN
        if (eRole == ERole.ROLE_ADMIN && !userHasRole(user, ERole.ROLE_ADMIN)) {
            roles.add(role);
        } 
        // If removing ROLE_ADMIN
        else if (eRole == ERole.ROLE_USER && userHasRole(user, ERole.ROLE_ADMIN)) {
            roles.removeIf(r -> r.getName() == ERole.ROLE_ADMIN);
        }
        
        user.setRoles(roles);
        userRepository.save(user);
    }
    
    private boolean userHasRole(User user, ERole roleName) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == roleName);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
