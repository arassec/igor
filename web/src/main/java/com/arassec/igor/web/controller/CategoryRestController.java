package com.arassec.igor.web.controller;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.web.model.KeyLabelStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * REST-Controller for category requests.
 */
@RestController()
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryRestController extends BaseRestController {

    /**
     * The message source with translations.
     */
    private final MessageSource messageSource;

    /**
     * The igor component registry.
     */
    private final IgorComponentRegistry igorComponentRegistry;

    /**
     * Returns all service categories as {@link KeyLabelStore}s.
     *
     * @return Set of all available service categories.
     */
    @GetMapping("service")
    public List<KeyLabelStore> getServiceCategories(Locale locale) {
        return sortByLabel(igorComponentRegistry.getCategoriesOfComponentType(Service.class).stream()
                .map(categoryId -> new KeyLabelStore(categoryId, messageSource.getMessage(categoryId, null, locale)))
                .collect(Collectors.toSet()));
    }

    /**
     * Returns all action categories as {@link KeyLabelStore}s.
     *
     * @return Set of all available action categories.
     */
    @GetMapping("provider")
    public List<KeyLabelStore> getProviderCategories(Locale locale) {
        return sortByLabel(igorComponentRegistry.getCategoriesOfComponentType(Provider.class).stream()
                .map(categoryId -> new KeyLabelStore(categoryId, messageSource.getMessage(categoryId, null, locale)))
                .collect(Collectors.toSet()));
    }

    /**
     * Returns all action categories as {@link KeyLabelStore}s.
     *
     * @return Set of all available action categories.
     */
    @GetMapping("action")
    public List<KeyLabelStore> getActionCategories(Locale locale) {
        return sortByLabel(igorComponentRegistry.getCategoriesOfComponentType(Action.class).stream()
                .map(categoryId -> new KeyLabelStore(categoryId, messageSource.getMessage(categoryId, null, locale)))
                .collect(Collectors.toSet()));
    }

    /**
     * Returns all trigger categories as {@link KeyLabelStore}s.
     *
     * @return Set of all available trigger categories.
     */
    @GetMapping("trigger")
    public List<KeyLabelStore> getTriggerCategories(Locale locale) {
        return sortByLabel(igorComponentRegistry.getCategoriesOfComponentType(Trigger.class).stream()
                .map(categoryId -> new KeyLabelStore(categoryId, messageSource.getMessage(categoryId, null, locale)))
                .collect(Collectors.toSet()));
    }

}
