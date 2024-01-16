package se.s373746.Lab4.converter;

import se.s373746.Lab4.entity.RoleEntity;
import se.s373746.Lab4.entity.UserEntity;
import se.s373746.Lab4.payload.UserDto;

import java.util.stream.Collectors;

public class UserEntityDtoConverter {

    public static UserEntity dtoToEntity(UserDto userDto) {
        return new UserEntity(userDto.getUsername(),
                userDto.getPassword(),
                userDto.getRoleSet().stream().map(RoleEntity::new).collect(Collectors.toSet()));
    }

    public static UserDto entityToDto(UserEntity userEntity) {
        return new UserDto(userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.getRoleSet().stream().map(RoleEntity::getRoleName).collect(Collectors.toSet()));
    }
}
