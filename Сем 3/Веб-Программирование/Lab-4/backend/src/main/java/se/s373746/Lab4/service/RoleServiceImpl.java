package se.s373746.Lab4.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.s373746.Lab4.entity.RoleEntity;
import se.s373746.Lab4.repository.RoleRepo;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepo roleRepo;

    @Override
    public RoleEntity saveRole(RoleEntity roleEntity) {
        return roleRepo.save(roleEntity);
    }

    @Override
    public void removeRole(RoleEntity roleEntity) {
        roleRepo.delete(roleEntity);
    }
}
