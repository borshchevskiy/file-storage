package ru.borshchevskiy.filestorage.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.borshchevskiy.filestorage.dto.user.UserRequestDto;
import ru.borshchevskiy.filestorage.dto.user.UserResponseDto;
import ru.borshchevskiy.filestorage.entity.Role;
import ru.borshchevskiy.filestorage.entity.User;

import java.util.Set;

/**
 * Class provides methods for converting {@link User} objects to {@link UserResponseDto}
 * and {@link UserRequestDto} objects and vice-versa.
 */
@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    /**
     * Method converts {@link User} to a new {@link UserResponseDto} object.
     * @param entity {@link User} object.
     * @return new {@link UserResponseDto} object.
     */
    public UserResponseDto mapToDto(User entity) {
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(entity.getId());
        responseDto.setFirstname(entity.getFirstname());
        responseDto.setLastname(entity.getLastname());
        responseDto.setEmail(entity.getEmail());
        responseDto.setRoles(entity.getRoles());
        return responseDto;
    }

    /**
     * Method converts {@link UserRequestDto} to a new {@link User} object.
     * This method is used for creating new users.
     * Password which is set to {@link User} object is encoded with {@link UserMapper#passwordEncoder}.
     * For each new {@link User} role is set to {@link Role#ROLE_USER}.
     *
     * @param requestDto {@link UserRequestDto} object.
     * @return new {@link User} object.
     */
    public User mapToEntity(UserRequestDto requestDto) {
        User user = new User();
        user.setFirstname(requestDto.getFirstname());
        user.setLastname(requestDto.getLastname());
        user.setEmail(requestDto.getEmail());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setRoles(Set.of(Role.ROLE_USER));
        return user;
    }

    /**
     * Method updates {@link User} object with data received from {@link UserRequestDto}.
     * This method is used to update user's data.
     *
     * @param user existing user.
     * @param requestDto new user data.
     * @return updated {@link User} object.
     */
    public User mergeUser(User user, UserRequestDto requestDto) {
        user.setFirstname(requestDto.getFirstname());
        user.setLastname(requestDto.getLastname());
        user.setEmail(requestDto.getEmail());
        return user;
    }
}
