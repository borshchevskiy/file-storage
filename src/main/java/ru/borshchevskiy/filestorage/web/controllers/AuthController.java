package ru.borshchevskiy.filestorage.web.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.borshchevskiy.filestorage.dto.user.UserRequestDto;
import ru.borshchevskiy.filestorage.dto.validation.OnCreate;
import ru.borshchevskiy.filestorage.service.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for operations related to log in and registration.
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * Method returns login page view.
     * @return login page view.
     */
    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    /**
     * Method returns registration page view.
     * @return registration page view.
     */
    @GetMapping("/registration")
    public String getRegistrationPage(UserRequestDto userRequestDto, Model model) {
        model.addAttribute(userRequestDto);
        return "registration";
    }

    /**
     * Method handles registration request. Performs password and confirmation equality check.
     * @return login page view on success or registration page view on fail.
     */
    @PostMapping("/registration")
    public String performRegistration(@ModelAttribute @Validated(OnCreate.class) UserRequestDto userRequestDto,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {

        List<String> errors = new ArrayList<>();

        if (userRequestDto.getPassword() != null
                && !userRequestDto.getPassword().equals(userRequestDto.getPasswordConfirmation())) {
            errors.add("Password and confirmation do not match!");
        }

        if (!errors.isEmpty() || bindingResult.hasErrors()) {
            errors.addAll(bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList());
            redirectAttributes.addFlashAttribute("userRequestDto", userRequestDto);
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:/registration";
        }

        userService.create(userRequestDto);

        return "redirect:/login";
    }
}
