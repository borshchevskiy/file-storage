package ru.borshchevskiy.filestorage.web.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.borshchevskiy.filestorage.exception.NotMultipartRequestException;
import ru.borshchevskiy.filestorage.exception.ResourceAlreadyExistsException;
import ru.borshchevskiy.filestorage.exception.ResourceNotFoundException;
import ru.borshchevskiy.filestorage.exception.repository.MinioRepositoryException;

/**
 * Handles exceptions not handled by specific handlers.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MinioRepositoryException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleMinioRepositoryException(MinioRepositoryException exception,
                                                 Model model) {
        model.addAttribute("exceptionMessage", exception.getMessage());
        log.error("Repository related exception. " + exception.getMessage() + exception);
        return "errors/error500";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralException(Exception exception) {
        log.error("General exception. " + exception.getMessage() + exception);
        return "errors/error500";
    }
}
