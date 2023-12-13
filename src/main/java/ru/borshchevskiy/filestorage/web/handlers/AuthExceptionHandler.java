package ru.borshchevskiy.filestorage.web.handlers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.borshchevskiy.filestorage.dto.user.UserRequestDto;
import ru.borshchevskiy.filestorage.exception.ResourceAlreadyExistsException;
import ru.borshchevskiy.filestorage.web.controllers.AuthController;

import java.util.List;
/**
 *  Handles exceptions from {@link AuthController}.
 */
@ControllerAdvice(assignableTypes = {AuthController.class})
public class AuthExceptionHandler {
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public String handleResourceAlreadyExistsException(ResourceAlreadyExistsException exception,
                                                       HttpServletRequest request,
                                                       RedirectAttributes redirectAttributes) {

        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setEmail(request.getParameter("email"));
        requestDto.setFirstname( request.getParameter("firstname"));
        requestDto.setLastname(request.getParameter("lastname"));

        redirectAttributes.addFlashAttribute("errors", List.of(exception.getMessage()));
        redirectAttributes.addFlashAttribute("userRequestDto", requestDto);

        return "redirect:/registration";
    }
}
