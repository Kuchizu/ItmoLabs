package se.s373746.Lab4.service;

import se.s373746.Lab4.payload.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto saveUser(UserDto userDto);

    Collection<UserDto> getUsers();

    boolean userExistByName(String username);

    boolean userExistById(Long id);
}
