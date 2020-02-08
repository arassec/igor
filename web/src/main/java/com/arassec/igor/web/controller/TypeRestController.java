package com.arassec.igor.web.controller;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.web.model.KeyLabelStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * REST-Controller for types.
 */
@RestController()
@RequestMapping("/api/type")
@RequiredArgsConstructor
public class TypeRestController implements BaseRestController {

    /**
     * The message source with translations.
     */
    private final MessageSource messageSource;

    /**
     * The igor component registry.
     */
    private final IgorComponentRegistry igorComponentRegistry;

    /**
     * Returns all types of a certain category as {@link KeyLabelStore}s.
     *
     * @param category The category to use.
     *
     * @return Set of types.
     */
    @GetMapping("{category}")
    public List<KeyLabelStore> getTypes(Locale locale, @PathVariable("category") String category) {
        return sortByLabel(igorComponentRegistry.getTypesOfCategory(category).stream()
                .map(typeId -> new KeyLabelStore(typeId, messageSource.getMessage(typeId, null, locale)))
                .collect(Collectors.toSet()));
    }

}
