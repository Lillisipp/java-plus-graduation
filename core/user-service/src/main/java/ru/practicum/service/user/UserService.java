package ru.practicum.service.user;


import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.model.user.User;

import java.util.List;

public interface UserService {

    UserDto createUser(NewUserRequest newUser);

    void deleteUser(Long userId);

    List<UserDto> findUsers(List<Long> ids, int from, int size);

    User findUserById(Long userId);
}
