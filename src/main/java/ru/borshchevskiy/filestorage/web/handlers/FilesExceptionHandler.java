package ru.borshchevskiy.filestorage.web.handlers;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.borshchevskiy.filestorage.exception.MultipartProcessingException;
import ru.borshchevskiy.filestorage.exception.NotMultipartRequestException;
import ru.borshchevskiy.filestorage.exception.repository.MinioRepositoryException;
import ru.borshchevskiy.filestorage.web.controllers.DirectoriesController;
import ru.borshchevskiy.filestorage.web.controllers.FilesController;

import java.util.List;

/**
 * Handles exceptions from {@link FilesController}.
 */
@Slf4j
@ControllerAdvice(assignableTypes = {FilesController.class, DirectoriesController.class})
public class FilesExceptionHandler {

    @ExceptionHandler(MinioRepositoryException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleMinioRepositoryException(MinioRepositoryException exception,
                                                 Model model) {
        model.addAttribute("exceptionMessages", exception.getMessage());
        log.error("Repository related exception. " + exception.getMessage() + exception);
        return "errors/error500";
    }

    @ExceptionHandler(NotMultipartRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleNotMultipartRequestException(NotMultipartRequestException exception,
                                                     Model model) {
        model.addAttribute("exceptionMessages", List.of(exception.getMessage()));
        log.error("Not multipart request." + exception.getMessage());
        return "errors/error400";
    }

    @ExceptionHandler(MultipartProcessingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMultipartProcessingException(MultipartProcessingException exception,
                                                     Model model) {
        model.addAttribute("exceptionMessages", List.of(exception.getMessage()));
        log.error("Error while processing multipart request. " + exception);
        return "errors/error400";
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleConstraintViolationException(ConstraintViolationException exception,
                                                     Model model) {
        model.addAttribute("exceptionMessages", exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .toList());
        log.error("Constraint violation." + exception.getMessage());
        return "errors/error400";
    }
}
