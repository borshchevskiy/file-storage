package ru.borshchevskiy.filestorage.web.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.borshchevskiy.filestorage.service.SearchService;

/**
 * Handles search requests.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    /**
     * Handles search requests.
     * @param query search query which should be searched in storage.
     * @param model MVC {@link Model}.
     * @return view name with search results.
     */
    @GetMapping
    public String search(@RequestParam(value = "query", required = false, defaultValue = "") String query,
                         Model model) {

        if (!query.isEmpty()) {
            model.addAttribute("results", searchService.search(query));
        }

        return "search";
    }
}
