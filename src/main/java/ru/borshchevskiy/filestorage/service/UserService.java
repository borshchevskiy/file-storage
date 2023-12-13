package ru.borshchevskiy.filestorage.service;

import ru.borshchevskiy.filestorage.dto.user.UserRequestDto;
import ru.borshchevskiy.filestorage.dto.user.UserResponseDto;

/**
 * Defines methods to interact with {@link ru.borshchevskiy.filestorage.entity.User} objects.
 */
public interface UserService {

    UserResponseDto findByEmail(String email);
    UserResponseDto findById(Long id);
    UserResponseDto create(UserRequestDto userRequestDto);
    UserResponseDto updateProfile(UserRequestDto userRequestDto);
    UserResponseDto updatePassword(UserRequestDto userRequestDto);
}
