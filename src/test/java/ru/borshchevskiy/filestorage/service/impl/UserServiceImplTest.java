package ru.borshchevskiy.filestorage.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.borshchevskiy.filestorage.dto.user.UserRequestDto;
import ru.borshchevskiy.filestorage.dto.user.UserResponseDto;
import ru.borshchevskiy.filestorage.entity.User;
import ru.borshchevskiy.filestorage.exception.ResourceAlreadyExistsException;
import ru.borshchevskiy.filestorage.exception.ResourceNotFoundException;
import ru.borshchevskiy.filestorage.mapper.UserMapper;
import ru.borshchevskiy.filestorage.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    @DisplayName("Test loadByUsername method")
    class LoadByUsername {
        @Test
        @DisplayName("Test when user exists - returns expected user")
        void userExists() {
            final Long testId = 1L;
            final String testEmail = "test@test.com";

            final User user = new User();
            user.setId(testId);
            user.setEmail(testEmail);

            doReturn(Optional.of(user)).when(userRepository).findByEmail(testEmail);

            UserDetails actualResult = userService.loadUserByUsername(testEmail);

            assertNotNull(actualResult);
            assertThat(actualResult.getUsername()).isEqualTo(testEmail);
            assertThat(actualResult).isEqualTo(user);

        }

        @Test
        @DisplayName("Test when  user doesn't exist - throws ResourceNotFoundException")
        void userDoesntExist() {
            final String testEmail = "test@test.com";

            doReturn(Optional.empty()).when(userRepository).findByEmail(anyString());
            assertThrows(ResourceNotFoundException.class, () -> userService.loadUserByUsername(testEmail));
        }
    }

    @Nested
    @DisplayName("Test findById method")
    class FindById {
        @Test
        @DisplayName("Test when user exists - returns expected dto")
        void userExists() {
            final Long testId = 1L;
            final String testEmail = "test@test.com";

            final User expectedUser = new User();
            expectedUser.setId(testId);
            expectedUser.setEmail(testEmail);

            final UserResponseDto expectedUserDto = new UserResponseDto();
            expectedUserDto.setId(testId);
            expectedUserDto.setEmail(testEmail);

            doReturn(Optional.of(expectedUser)).when(userRepository).findById(testId);
            doReturn(expectedUserDto).when(userMapper).mapToDto(expectedUser);

            UserResponseDto actualResult = userService.findById(testId);

            assertNotNull(actualResult);
            assertThat(actualResult.getEmail()).isEqualTo(testEmail);
            assertThat(actualResult).isEqualTo(expectedUserDto);

        }

        @Test
        @DisplayName("Test when user doesn't exist - throws ResourceNotFoundException")
        void userDoesntExist() {
            final Long testId = Long.MAX_VALUE;

            doReturn(Optional.empty()).when(userRepository).findById(anyLong());
            assertThrows(ResourceNotFoundException.class, () -> userService.findById(testId));
        }
    }

    @Nested
    @DisplayName("Test findByEmail method")
    class FindByEmail {
        @Test
        @DisplayName("Test when user exists - returns expected dto")
        void userExists() {
            final Long testId = 1L;
            final String testEmail = "test@test.com";

            final User expectedUser = new User();
            expectedUser.setId(testId);
            expectedUser.setEmail(testEmail);

            final UserResponseDto expectedUserDto = new UserResponseDto();
            expectedUserDto.setId(testId);
            expectedUserDto.setEmail(testEmail);

            doReturn(Optional.of(expectedUser)).when(userRepository).findByEmail(testEmail);
            doReturn(expectedUserDto).when(userMapper).mapToDto(expectedUser);

            UserResponseDto actualResult = userService.findByEmail(testEmail);

            assertNotNull(actualResult);
            assertThat(actualResult.getEmail()).isEqualTo(testEmail);
            assertThat(actualResult).isEqualTo(expectedUserDto);

        }

        @Test
        @DisplayName("Test when user doesn't exist - throws ResourceNotFoundException")
        void userDoesntExist() {
            final String testEmail = "test@test.com";

            doReturn(Optional.empty()).when(userRepository).findByEmail(anyString());
            assertThrows(ResourceNotFoundException.class, () -> userService.findByEmail(testEmail));
        }
    }

    @Nested
    @DisplayName("Test updateProfile method")
    class UpdateProfile {
        @Test
        @DisplayName("Test when user exists - returns expected dto")
        void userExists() {
            final String oldEmail = "old@old.com";
            final String oldFirstname = "oldFirstname";
            final String oldLastname = "oldLastname";
            final String newEmail = "new@new.com";
            final String newFirstname = "newFirstname";
            final String newLastname = "newLastname";

            final User existingUser = new User();
            existingUser.setEmail(oldEmail);
            existingUser.setFirstname(oldFirstname);
            existingUser.setLastname(oldLastname);

            final User updatedUser = new User();
            updatedUser.setEmail(newEmail);
            updatedUser.setFirstname(newFirstname);
            updatedUser.setLastname(newLastname);

            final UserRequestDto userRequestDto = new UserRequestDto();
            userRequestDto.setEmail(newEmail);
            userRequestDto.setFirstname(newFirstname);
            userRequestDto.setLastname(newLastname);

            final UserResponseDto expectedUserResponseDto = new UserResponseDto();
            expectedUserResponseDto.setEmail(newEmail);
            expectedUserResponseDto.setFirstname(newFirstname);
            expectedUserResponseDto.setLastname(newLastname);

            final Authentication authentication = mock(Authentication.class);
            final SecurityContext securityContext = mock(SecurityContext.class);

            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            try (MockedStatic<SecurityContextHolder> mockedSecCtxHolder = mockStatic(SecurityContextHolder.class)) {
                mockedSecCtxHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            }

            doReturn(authentication).when(securityContext).getAuthentication();
            doReturn(oldEmail).when(authentication).getName();
            doReturn(updatedUser).when(userMapper).mergeUser(existingUser, userRequestDto);
            doReturn(expectedUserResponseDto).when(userMapper).mapToDto(updatedUser);
            doReturn(Optional.of(existingUser)).when(userRepository).findByEmail(oldEmail);
            doReturn(updatedUser).when(userRepository).save(updatedUser);

            UserResponseDto actualResult = userService.updateProfile(userRequestDto);

            assertNotNull(actualResult);
            assertThat(actualResult).isEqualTo(expectedUserResponseDto);

        }

        @Test
        @DisplayName("Test when user doesn't exist - throws ResourceNotFoundException")
        void userDoesntExist() {
            final String oldEmail = "old@old.com";
            final String newEmail = "new@new.com";
            final String newFirstname = "newFirstname";
            final String newLastname = "newLastname";

            final UserRequestDto userRequestDto = new UserRequestDto();
            userRequestDto.setEmail(newEmail);
            userRequestDto.setFirstname(newFirstname);
            userRequestDto.setLastname(newLastname);

            final Authentication authentication = mock(Authentication.class);
            final SecurityContext securityContext = mock(SecurityContext.class);

            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            try (MockedStatic<SecurityContextHolder> mockedSecCtxHolder = mockStatic(SecurityContextHolder.class)) {
                mockedSecCtxHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            }

            doReturn(authentication).when(securityContext).getAuthentication();
            doReturn(oldEmail).when(authentication).getName();

            doReturn(Optional.empty()).when(userRepository).findByEmail(anyString());
            assertThrows(ResourceNotFoundException.class, () -> userService.updateProfile(userRequestDto));
        }

        @Test
        @DisplayName("Test when new email is already taken - throws ResourceAlreadyExistsException")
        void emailIsTaken() {
            final String oldEmail = "old@old.com";
            final String newEmail = "new@new.com";
            final String newFirstname = "newFirstname";
            final String newLastname = "newLastname";

            final UserRequestDto userRequestDto = new UserRequestDto();
            userRequestDto.setEmail(newEmail);
            userRequestDto.setFirstname(newFirstname);
            userRequestDto.setLastname(newLastname);

            final Authentication authentication = mock(Authentication.class);
            final SecurityContext securityContext = mock(SecurityContext.class);

            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            try (MockedStatic<SecurityContextHolder> mockedSecCtxHolder = mockStatic(SecurityContextHolder.class)) {
                mockedSecCtxHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            }

            doReturn(authentication).when(securityContext).getAuthentication();
            doReturn(oldEmail).when(authentication).getName();

            doThrow(DataIntegrityViolationException.class).when(userRepository).findByEmail(anyString());
            assertThrows(ResourceAlreadyExistsException.class, () -> userService.updateProfile(userRequestDto));
        }
    }

    @Nested
    @DisplayName("Test updatePassword method")
    class UpdatePassword {
        @Test
        @DisplayName("Test when user exists - returns expected dto")
        void userExists() {
            final String email = "old@old.com";
            final String firstname = "firstname";
            final String lastname = "lastname";
            final String oldPassword = "oldPassword";
            final String newPassword = "newPassword";
            final String encodedNewPassword = "encodedNewPassword";

            final User existingUser = new User();
            existingUser.setEmail(email);
            existingUser.setFirstname(firstname);
            existingUser.setLastname(lastname);
            existingUser.setPassword(oldPassword);

            final User updatedUser = new User();
            updatedUser.setEmail(email);
            updatedUser.setFirstname(firstname);
            updatedUser.setLastname(lastname);
            updatedUser.setPassword(encodedNewPassword);

            final UserRequestDto userRequestDto = new UserRequestDto();
            userRequestDto.setPassword(newPassword);

            final UserResponseDto expectedUserResponseDto = new UserResponseDto();
            expectedUserResponseDto.setEmail(email);
            expectedUserResponseDto.setFirstname(firstname);
            expectedUserResponseDto.setLastname(lastname);

            final Authentication authentication = mock(Authentication.class);
            final SecurityContext securityContext = mock(SecurityContext.class);

            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            try (MockedStatic<SecurityContextHolder> mockedSecCtxHolder = mockStatic(SecurityContextHolder.class)) {
                mockedSecCtxHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            }

            doReturn(authentication).when(securityContext).getAuthentication();
            doReturn(email).when(authentication).getName();
            doReturn(expectedUserResponseDto).when(userMapper).mapToDto(updatedUser);
            doReturn(encodedNewPassword).when(passwordEncoder).encode(newPassword);
            doReturn(Optional.of(existingUser)).when(userRepository).findByEmail(email);
            doReturn(updatedUser).when(userRepository).save(updatedUser);

            UserResponseDto actualResult = userService.updatePassword(userRequestDto);

            assertNotNull(actualResult);
            assertThat(actualResult).isEqualTo(expectedUserResponseDto);
        }

        @Test
        @DisplayName("Test when user doesn't exist - throws ResourceNotFoundException")
        void userDoesntExist() {
            final String email = "old@old.com";
            final String firstname = "firstname";
            final String lastname = "lastname";
            final String password = "password";

            final UserRequestDto userRequestDto = new UserRequestDto();
            userRequestDto.setEmail(email);
            userRequestDto.setFirstname(firstname);
            userRequestDto.setLastname(lastname);
            userRequestDto.setPassword(password);

            final Authentication authentication = mock(Authentication.class);
            final SecurityContext securityContext = mock(SecurityContext.class);

            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            try (MockedStatic<SecurityContextHolder> mockedSecCtxHolder = mockStatic(SecurityContextHolder.class)) {
                mockedSecCtxHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            }

            doReturn(authentication).when(securityContext).getAuthentication();
            doReturn(email).when(authentication).getName();
            doReturn(Optional.empty()).when(userRepository).findByEmail(anyString());

            assertThrows(ResourceNotFoundException.class, () -> userService.updatePassword(userRequestDto));
        }
    }

    @Nested
    @DisplayName("Test create method")
    class Create {
        @Test
        @DisplayName("Test when email is not taken - returns expected dto")
        void emailNotTaken() {
            final String email = "test@test.com";
            final String firstname = "firstname";
            final String lastname = "lastname";

            final User user = new User();
            user.setEmail(email);
            user.setFirstname(firstname);
            user.setLastname(lastname);

            final UserRequestDto userRequestDto = new UserRequestDto();
            userRequestDto.setEmail(email);
            userRequestDto.setFirstname(firstname);
            userRequestDto.setLastname(lastname);

            final UserResponseDto expectedUserResponseDto = new UserResponseDto();
            expectedUserResponseDto.setEmail(email);
            expectedUserResponseDto.setFirstname(firstname);
            expectedUserResponseDto.setLastname(lastname);

            doReturn(user).when(userMapper).mapToEntity(userRequestDto);
            doReturn(expectedUserResponseDto).when(userMapper).mapToDto(user);
            doReturn(user).when(userRepository).save(user);

            UserResponseDto actualResult = userService.create(userRequestDto);

            assertNotNull(actualResult);
            assertThat(actualResult).isEqualTo(expectedUserResponseDto);

        }

        @Test
        @DisplayName("Test when email is taken - throws ResourceNotFoundException")
        void emailIsTaken() {
            final String email = "test@test.com";
            final String firstname = "firstname";
            final String lastname = "lastname";

            final UserRequestDto userRequestDto = new UserRequestDto();
            userRequestDto.setEmail(email);
            userRequestDto.setFirstname(firstname);
            userRequestDto.setLastname(lastname);

            final User user = new User();
            user.setEmail(email);
            user.setFirstname(firstname);
            user.setLastname(lastname);

            doReturn(user).when(userMapper).mapToEntity(userRequestDto);
            doThrow(DataIntegrityViolationException.class).when(userRepository).save(any(User.class));

            assertThrows(ResourceAlreadyExistsException.class, () -> userService.create(userRequestDto));
        }
    }
}