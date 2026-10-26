package com.kalibyte.tradex.party.repository;

import com.kalibyte.tradex.party.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    
    Set<Role> findAllByIdIn(Set<Long> ids);
    
    boolean existsByName(String name);
}
