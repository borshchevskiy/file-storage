package ru.borshchevskiy.filestorage.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = DirectoryNameValidator.class)
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface ValidDirectoryName {
    String message() default "Directory name is invalid";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
