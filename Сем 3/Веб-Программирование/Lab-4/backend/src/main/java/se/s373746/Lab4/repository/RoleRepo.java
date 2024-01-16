package se.s373746.Lab4.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.s373746.Lab4.entity.RoleEntity;
import se.s373746.Lab4.security.UserRole;

@Repository
public interface RoleRepo extends JpaRepository<RoleEntity, Long> {

    RoleEntity findByRoleName(UserRole userRole);
}
