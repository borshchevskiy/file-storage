package ru.borshchevskiy.filestorage.web.session;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import ru.borshchevskiy.filestorage.dto.user.UserResponseDto;

import java.io.Serializable;

/**
 * Session scoped bean that carries data of current user.
 */
@Data
@Component
@SessionScope
public class UserSessionData implements Serializable {

    private Long id;
    private String firstname;
    private String lastname;
    private String displayName;
    private String userDirectory;

    public void addName(UserResponseDto userResponseDto) {
        setFirstname(userResponseDto.getFirstname());
        setLastname(userResponseDto.getLastname());
        setDisplayName(userResponseDto.getFirstname() +
                (userResponseDto.getLastname() == null ? "" : " " + userResponseDto.getLastname()));
    }
}
