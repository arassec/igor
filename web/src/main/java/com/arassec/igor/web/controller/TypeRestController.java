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
public class TypeRestController extends BaseRestController {

    /**
     * The message source with translations.
     */
    private final MessageSource messageSource;

    /**
     * The igor component registry.
     */
    private final IgorComponentRegistry igorComponentRegistry;

    /**
     * Returns all service types of a certain category as {@link KeyLabelStore}s.
     *
     * @param category The service category to use.
     *
     * @return Set of service types.
     */
    @GetMapping("service/{category}")
    public List<KeyLabelStore> getServiceTypes(Locale locale, @PathVariable("category") String category) {
        return sortByLabel(igorComponentRegistry.getTypesOfCategory(category).stream()
                .map(typeId -> new KeyLabelStore(typeId, messageSource.getMessage(typeId, null, locale)))
                .collect(Collectors.toSet()));
    }

    /**
     * Returns all action types of a certain category as {@link KeyLabelStore}s.
     *
     * @param category The action category to use.
     *
     * @return Set of action types.
     */
    @GetMapping("action/{category}")
    public List<KeyLabelStore> getActionTypes(Locale locale, @PathVariable("category") String category) {
        return sortByLabel(igorComponentRegistry.getTypesOfCategory(category).stream()
                .map(typeId -> new KeyLabelStore(typeId, messageSource.getMessage(typeId, null, locale)))
                .collect(Collectors.toSet()));
    }

    /**
     * Returns all action types of a certain category as {@link KeyLabelStore}s.
     *
     * @param category The action category to use.
     *
     * @return Set of action types.
     */
    @GetMapping("provider/{category}")
    public List<KeyLabelStore> getProviderTypes(Locale locale, @PathVariable("category") String category) {
        return sortByLabel(igorComponentRegistry.getTypesOfCategory(category).stream()
                .map(typeId -> new KeyLabelStore(typeId, messageSource.getMessage(typeId, null, locale)))
                .collect(Collectors.toSet()));
    }

    /**
     * Returns all action types of a certain category as {@link KeyLabelStore}s.
     *
     * @param category The action category to use.
     *
     * @return Set of action types.
     */
    @GetMapping("trigger/{category}")
    public List<KeyLabelStore> getTriggerTypes(Locale locale, @PathVariable("category") String category) {
        return sortByLabel(igorComponentRegistry.getTypesOfCategory(category).stream()
                .map(typeId -> new KeyLabelStore(typeId, messageSource.getMessage(typeId, null, locale)))
                .collect(Collectors.toSet()));
    }

}
