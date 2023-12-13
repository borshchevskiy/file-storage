package ru.borshchevskiy.filestorage.web.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.borshchevskiy.filestorage.dto.user.UserRequestDto;
import ru.borshchevskiy.filestorage.dto.user.UserResponseDto;
import ru.borshchevskiy.filestorage.dto.validation.OnPasswordUpdate;
import ru.borshchevskiy.filestorage.dto.validation.OnUpdate;
import ru.borshchevskiy.filestorage.service.UserService;
import ru.borshchevskiy.filestorage.web.session.UserSessionData;
import ru.borshchevskiy.filestorage.web.util.ControllerUtil;

import java.util.List;

/**
 * Handles user manipulation requests.
 */
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserSessionData userSessionData;

    /**
     * Returns profile page view.
     * @param userDetails {@link UserDetails} object representing current user.
     * @param model MVC {@link Model}.
     * @return profile page view name.
     */
    @GetMapping
    public String getProfilePage(@AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        UserResponseDto userResponseDto = userService.findByEmail(userDetails.getUsername());
        model.addAttribute("user", userResponseDto);
        return "profile";
    }

    /**
     * Handles profile update requests. Updates session-scoped bean {@link UserSessionData}.
     * @param userRequestDto {@link UserRequestDto} object carrying user data.
     * @param bindingResult {@link BindingResult}.
     * @param redirectAttributes {@link RedirectAttributes}.
     * @return profile page view name.
     */
    @PostMapping("/update")
    public String updateProfile(@Validated(OnUpdate.class) @ModelAttribute UserRequestDto userRequestDto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            List<String> errors = ControllerUtil.getBindingErrorsList(bindingResult);
            redirectAttributes.addFlashAttribute("user", userRequestDto);
            redirectAttributes.addFlashAttribute("errors", errors);

            return "redirect:/profile";
        }

        UserResponseDto userResponseDto = userService.updateProfile(userRequestDto);
        redirectAttributes.addFlashAttribute("user", userResponseDto);
        redirectAttributes.addFlashAttribute("isProfileUpdated", true);
        userSessionData.addName(userResponseDto);

        return "redirect:/profile";
    }

    /**
     * Handles password update requests.
     * @param userRequestDto {@link UserRequestDto} object carrying user data incl new password.
     * @param bindingResult {@link BindingResult}.
     * @param redirectAttributes {@link RedirectAttributes}.
     * @return profile page view name.
     */
    @PostMapping("/update/password")
    public String updatePassword(@Validated(OnPasswordUpdate.class) @ModelAttribute UserRequestDto userRequestDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            List<String> errors = ControllerUtil.getBindingErrorsList(bindingResult);
            redirectAttributes.addFlashAttribute("user", userRequestDto);
            redirectAttributes.addFlashAttribute("errors", errors);

            return "redirect:/profile";
        }
        UserResponseDto userResponseDto = userService.updatePassword(userRequestDto);
        redirectAttributes.addFlashAttribute("user", userResponseDto);
        redirectAttributes.addFlashAttribute("isPasswordUpdated", true);

        return "redirect:/profile";
    }
}
