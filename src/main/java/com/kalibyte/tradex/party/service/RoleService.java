package com.kalibyte.tradex.party.service;

import com.kalibyte.tradex.party.dto.RoleDTO;
import com.kalibyte.tradex.party.entity.Role;
import com.kalibyte.tradex.party.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {
    
    private final RoleRepository roleRepository;
    
    /**
     * Get a role by ID
     */
    public Role getRoleById(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));
    }
    
    /**
     * Get a role by name
     */
    public Role getRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found with name: " + roleName));
    }
    
    /**
     * Get roles by IDs
     */
    public Set<Role> getRolesByIds(Set<Long> roleIds) {
        Set<Role> roles = roleRepository.findAllByIdIn(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new RuntimeException("One or more roles not found");
        }
        return roles;
    }
    
    /**
     * Initialize default roles if they don't exist
     */
    @Transactional
    public void initializeDefaultRoles() {
        String[] defaultRoles = {"ADMIN", "MANUFACTURER", "TRADER", "SELLER", "BUYER", "CONSUMER", "BROKER"};
        
        for (String roleName : defaultRoles) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = Role.builder()
                        .name(roleName)
                        .description(getDefaultRoleDescription(roleName))
                        .active(true)
                        .build();
                roleRepository.save(role);
            }
        }
    }
    
    private String getDefaultRoleDescription(String roleName) {
        return switch (roleName) {
            case "ADMIN" -> "Market Operator/Admin who manages the entire marketplace";
            case "MANUFACTURER" -> "Manufacturer/Plant who can act as both buyer and seller";
            case "TRADER" -> "Trader who deals in commodities";
            case "SELLER" -> "Seller/Supplier who provides raw materials";
            case "BUYER" -> "Buyer who purchases raw materials";
            case "CONSUMER" -> "Consumer/Buyer desk procuring agricultural feed and processed goods";
            case "BROKER" -> "Broker/Agent facilitating trades between counterparties";
            default -> roleName;
        };
    }
    
    /**
     * Convert Role entity to RoleDTO
     */
    public RoleDTO convertToDTO(Role role) {
        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .active(role.getActive())
                .build();
    }
}
