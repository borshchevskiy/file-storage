package ru.borshchevskiy.filestorage.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.borshchevskiy.filestorage.dto.user.UserRequestDto;
import ru.borshchevskiy.filestorage.dto.user.UserResponseDto;
import ru.borshchevskiy.filestorage.entity.User;
import ru.borshchevskiy.filestorage.exception.ResourceAlreadyExistsException;
import ru.borshchevskiy.filestorage.exception.ResourceNotFoundException;
import ru.borshchevskiy.filestorage.mapper.UserMapper;
import ru.borshchevskiy.filestorage.repository.UserRepository;
import ru.borshchevskiy.filestorage.service.UserService;

/**
 * Class provides methods to manipulate {@link User} entity.
 * Also implements {@link UserDetailsService} for security needs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Finds user by its username which is {@link User#email} field.
     * Method primarily used by security services.
     * @param username user's username.
     * @return {@link UserDetails} object.
     * @throws ResourceNotFoundException in case if user not found.
     * @see User
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    log.error("User with email:" + username + " not found.");
                    throw new ResourceNotFoundException("User with email:" + username + " not found.");
                });
    }
    /**
     * Finds user by its id.
     * @param id user's id.
     * @return {@link UserResponseDto} object representing user's data.
     * @throws ResourceNotFoundException in case if user not found.
     * @see User
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::mapToDto)
                .orElseThrow(() -> {
                    log.error("User with id=" + id + " not found.");
                    throw new ResourceNotFoundException("User with id=" + id + " not found.");
                });
    }
    /**
     * Finds user by its email.
     * @param email user's email.
     * @return {@link UserResponseDto} object representing user's data.
     * @throws ResourceNotFoundException in case if user not found.
     * @see User
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::mapToDto)
                .orElseThrow(() -> {
                    log.error("User with email=" + email + " not found.");
                    throw new ResourceNotFoundException("User with email=" + email + " not found.");
                });
    }
    /**
     * Method updates current user's profile.
     *
     * @param userRequestDto object, containing user's data to be updated.
     * @return {@link UserResponseDto} object representing user's data.
     * @throws ResourceNotFoundException in case if user not found.
     * @throws ResourceAlreadyExistsException if email is updated but new value already taken by another user.
     * @see User
     */
    @Override
    @Transactional
    public UserResponseDto updateProfile(UserRequestDto userRequestDto) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            return userRepository.findByEmail(email)
                    .map(currentUser -> {
                        log.debug("Profile updated for user " + email + ". " +
                                "Current email is " + userRequestDto.getEmail());
                        return userMapper.mergeUser(currentUser, userRequestDto);
                    })
                    .map(userRepository::save)
                    .map(userMapper::mapToDto)
                    .orElseThrow(() -> {
                        log.error("Failed to update user with email=" + userRequestDto.getEmail() + ". " +
                                "Email not found.");
                        throw new ResourceNotFoundException("User with email=" + email + " not found.");
                    });
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to update user with email=" + userRequestDto.getEmail() + ". " +
                    "Email already taken.");
            throw new ResourceAlreadyExistsException("User with email "
                    + userRequestDto.getEmail() + " already exists.");
        }
    }
    /**
     * Method updates current user's password.
     *
     * @param userRequestDto object, containing new password.
     * @return {@link UserResponseDto} object representing user's data.
     * @throws ResourceNotFoundException in case if user not found.
     * @see User
     */
    @Override
    @Transactional
    public UserResponseDto updatePassword(UserRequestDto userRequestDto) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .map(currentUser -> {
                    currentUser.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
                    log.debug("password updated for user " + email + ".");
                    return currentUser;
                })
                .map(userRepository::save)
                .map(userMapper::mapToDto)
                .orElseThrow(() -> {
                    log.error("Password update for " + email + " failed. User with email=" + email + " not found.");
                    throw new ResourceNotFoundException("User with email=" + email + " not found.");
                });
    }
    /**
     * Method creates new user.
     *
     * @param userRequestDto object, containing user's data.
     * @return {@link UserResponseDto} object representing user's data.
     * @throws ResourceAlreadyExistsException if email is already taken by another user.
     * @see User
     */
    @Override
    @Transactional
    public UserResponseDto create(UserRequestDto userRequestDto) {
        try {
            User user = userRepository.save(userMapper.mapToEntity(userRequestDto));
            log.debug("New user id=" + user.getId() + " created.");
            return userMapper.mapToDto(user);
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to create user with email=" + userRequestDto.getEmail() + ". " +
                    "Email already taken.");
            throw new ResourceAlreadyExistsException("User with email "
                    + userRequestDto.getEmail() + " already exists.");
        }
    }
}
