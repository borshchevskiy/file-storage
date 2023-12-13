package ru.borshchevskiy.filestorage.web.handlers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.borshchevskiy.filestorage.dto.user.UserRequestDto;
import ru.borshchevskiy.filestorage.exception.ResourceAlreadyExistsException;
import ru.borshchevskiy.filestorage.exception.ResourceNotFoundException;
import ru.borshchevskiy.filestorage.web.controllers.UserController;

import java.util.List;

/**
 * Handles exceptions from {@link UserController}.
 */
@Slf4j
@ControllerAdvice(assignableTypes = {UserController.class})
public class UserExceptionHandler {
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public String handleResourceAlreadyExistsException(ResourceAlreadyExistsException exception,
                                                       HttpServletRequest request,
                                                       RedirectAttributes redirectAttributes) {

        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setEmail(request.getParameter("email"));
        requestDto.setFirstname(request.getParameter("firstname"));
        requestDto.setLastname(request.getParameter("lastname"));

        redirectAttributes.addFlashAttribute("errors", List.of(exception.getMessage()));
        redirectAttributes.addFlashAttribute("userRequestDto", requestDto);

        return "redirect:/profile";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFoundException(ResourceNotFoundException exception,
                                                  Model model) {
        model.addAttribute("exceptionMessage", exception.getMessage());
        return "errors/error400";
    }
}
