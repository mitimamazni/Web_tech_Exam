package com.ecommerce.app.config;

import com.ecommerce.app.model.ERole;
import com.ecommerce.app.model.Role;
import com.ecommerce.app.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataLoader {

    private final RoleRepository roleRepository;

    @Autowired
    public DataLoader(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    @Transactional
    public void loadData() {
        createRoleIfNotFound(ERole.ROLE_USER);
        createRoleIfNotFound(ERole.ROLE_ADMIN);
        
        System.out.println("Initial data loading completed!");
    }

    private void createRoleIfNotFound(ERole roleName) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = new Role(roleName);
            roleRepository.save(role);
            System.out.println("Created role: " + roleName);
        }
    }
}
