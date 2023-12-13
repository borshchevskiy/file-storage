package ru.borshchevskiy.filestorage.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ru.borshchevskiy.filestorage.dto.validation.OnCreate;
import ru.borshchevskiy.filestorage.dto.validation.OnPasswordUpdate;
import ru.borshchevskiy.filestorage.dto.validation.OnUpdate;

import java.io.Serializable;

/**
 * Class represents {@link ru.borshchevskiy.filestorage.entity.User} data, which is received with HTTP request.
 */
@Data
public class UserRequestDto implements Serializable {

    private Long id;

    @NotBlank(message = "Email must not be null!", groups = {OnCreate.class, OnUpdate.class})
    @Email(message = "Email is not valid!", groups = {OnCreate.class, OnUpdate.class})
    @Length(message = "Email must be less than 255 symbols long!", max = 255,
            groups = {OnCreate.class, OnUpdate.class})
    private String email;

    @NotBlank(message = "Firstname must not be null!", groups = {OnCreate.class, OnUpdate.class})
    @Length(message = "Firstname must be less than 255 symbols long!", max = 255,
            groups = {OnCreate.class, OnUpdate.class})
    private String firstname;

    @Length(message = "lastname must be less than 255 symbols long!", max = 255,
            groups = {OnCreate.class, OnUpdate.class})
    private String lastname;

    @NotBlank(message = "Password must not be null!", groups = {OnCreate.class, OnPasswordUpdate.class})
    @Length(message = "Password must be at least 8 symbols long!", min = 8,
            groups = {OnCreate.class, OnPasswordUpdate.class})
    @Length(message = "Password must be less than 255 symbols long!", max = 255,
            groups = {OnCreate.class, OnPasswordUpdate.class})
    private String password;

    @NotBlank(message = "Confirmation must not be null!", groups = {OnCreate.class, OnPasswordUpdate.class})
    @Length(message = "Confirmation must be less than 255 symbols long!", max = 255,
            groups = {OnCreate.class, OnPasswordUpdate.class})
    private String passwordConfirmation;

}
