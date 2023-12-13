package ru.borshchevskiy.filestorage.web.util;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for controllers layer.
 */
public class ControllerUtil {
    /**
     * Retrieves list of error messages from {@link BindingResult}.
     * @param bindingResult {@link BindingResult}.
     * @return list of error messages.
     */
    public static List<String> getBindingErrorsList(BindingResult bindingResult) {
        return new ArrayList<>(bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList());
    }
}
