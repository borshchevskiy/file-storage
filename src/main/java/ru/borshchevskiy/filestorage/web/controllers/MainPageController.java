package ru.borshchevskiy.filestorage.web.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.borshchevskiy.filestorage.service.FileService;
import ru.borshchevskiy.filestorage.util.FilePathUtil;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
/**
 * Controller for main page requests.
 */
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class MainPageController {

    private final FileService fileService;

    /**
     * Returns main page view.
     * If path parameter is not empty, parentPath parameter is calculated for page navigation purposes.
     * @param path path in user's storage which content should be displayed.
     * @param model MVC {@link Model}.
     * @param user current {@link org.springframework.security.core.userdetails.User}.
     * @return main page view.
     */
    @GetMapping
    public String getMainPage(@RequestParam(value = "path", required = false, defaultValue = "") String path,
                              Model model,
                              @AuthenticationPrincipal UserDetails user) {
        if (user != null) {
            model.addAttribute("path", path);

            String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);

            model.addAttribute("breadcrumbs", FilePathUtil.generateBreadcrumbs(decodedPath));
            model.addAttribute("filesList", fileService.getItemsByPath(decodedPath));

            if (!path.isEmpty()) {
                model.addAttribute("parentPath", FilePathUtil.getParent(decodedPath));
            }
        }
        return "main";
    }

    /**
     * Updates files' list on main page without refreshing entire page.
     * If path parameter is not empty, parentPath parameter is calculated for page navigation purposes.
     * @param path storage path which content should be viewed.
     * @param model MVC {@link Model}.
     * @return fragment to be updated on main page.
     */
    @GetMapping("/updateFilesList")
    public String updateFilesList(@RequestParam(value = "path", required = false, defaultValue = "") String path,
                                  Model model) {
        model.addAttribute("path", path);
        model.addAttribute("filesList", fileService.getItemsByPath(path));

        if (!path.isEmpty()) {
            model.addAttribute("parentPath", FilePathUtil.getParent(path));
        }

        return "fragments/main/fragment-filesList :: filesList";
    }
}
