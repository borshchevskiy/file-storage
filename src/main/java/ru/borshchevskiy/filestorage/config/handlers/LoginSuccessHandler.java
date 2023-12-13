package ru.borshchevskiy.filestorage.config.handlers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import ru.borshchevskiy.filestorage.dto.user.UserResponseDto;
import ru.borshchevskiy.filestorage.service.UserService;
import ru.borshchevskiy.filestorage.web.session.UserSessionData;

import java.io.IOException;

/**
 * Manages creation of session-scoped bean {@link UserSessionData} on successful login and populates it with
 * initial data.
 *
 * @see UserSessionData
 */
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final UserSessionData userSessionData;

    /**
     * Prefix for user's folder name which is contained in {@link UserSessionData} bean
     */
    @Value("${app.user-folder.prefix:user-}")
    private String userFolderPrefix;

    /**
     * Suffix for user's folder name which is contained in {@link UserSessionData} bean
     */
    @Value("${app.user-folder.suffix:-files}")
    private String userFolderSuffix;

    /**
     * On successful authentication creates session-scoped bean {@link UserSessionData} and populates it
     * with initial data. {@link UserSessionData}'s field's which are populated:
     * <li> id - authenticated user's id;</li>
     * <li> firstname - authenticated user's firstname;</li>
     * <li> lastname - authenticated user's lastname;</li>
     * <li> displayName - authenticated user's firstname + lastname;</li>
     * <li> userDirectory - authenticated user's personal directory name. Combined from {@link #userFolderPrefix},
     * user's id and {@link #userFolderSuffix}. E.g. "user-42-files", where "user-" is prefix, "42" is user's id
     * and "-files" is suffix.</li>
     *
     * @param request the request which caused the successful authentication
     * @param response the response
     * @param authentication the <tt>Authentication</tt> object which was created during
     * the authentication process.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        UserDetails user = (UserDetails) authentication.getPrincipal();
        UserResponseDto userResponseDto = userService.findByEmail(user.getUsername());
        userSessionData.setId(userResponseDto.getId());
        userSessionData.setUserDirectory(userFolderPrefix + userResponseDto.getId() + userFolderSuffix);
        userSessionData.addName(userResponseDto);

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
