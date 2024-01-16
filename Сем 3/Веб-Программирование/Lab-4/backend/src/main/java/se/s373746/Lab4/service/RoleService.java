package se.s373746.Lab4.service;

import se.s373746.Lab4.entity.RoleEntity;

public interface RoleService {
    RoleEntity saveRole(RoleEntity roleEntity);

    void removeRole(RoleEntity roleEntity);
}
