package com.arassec.igor.web.controller;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.provider.Provider;
import com.arassec.igor.core.model.trigger.Trigger;
import com.arassec.igor.web.model.KeyLabelStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
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
     * Returns all connector categories as {@link KeyLabelStore}s.
     *
     * @param locale The user's locale for i18n.
     *
     * @return Set of all available connector categories.
     */
    @GetMapping("connector")
    public List<KeyLabelStore> getConnectorCategories(Locale locale) {
        return igorComponentRegistry.getCategoriesOfComponentType(Connector.class).stream()
                .map(categoryId -> new KeyLabelStore(categoryId, messageSource.getMessage(categoryId, null, locale)))
                .sorted(Comparator.comparing(KeyLabelStore::getValue))
                .collect(Collectors.toList());
    }

    /**
     * Returns all action categories as {@link KeyLabelStore}s.
     *
     * @param locale The user's locale for i18n.
     *
     * @return Set of all available action categories.
     */
    @GetMapping("provider")
    public List<KeyLabelStore> getProviderCategories(Locale locale) {
        return igorComponentRegistry.getCategoriesOfComponentType(Provider.class).stream()
                .map(categoryId -> new KeyLabelStore(categoryId, messageSource.getMessage(categoryId, null, locale)))
                .sorted(Comparator.comparing(KeyLabelStore::getValue))
                .collect(Collectors.toList());
    }

    /**
     * Returns all action categories as {@link KeyLabelStore}s.
     *
     * @param locale The user's locale for i18n.
     *
     * @return Set of all available action categories.
     */
    @GetMapping("action")
    public List<KeyLabelStore> getActionCategories(Locale locale) {
        return igorComponentRegistry.getCategoriesOfComponentType(Action.class).stream()
                .map(categoryId -> new KeyLabelStore(categoryId, messageSource.getMessage(categoryId, null, locale)))
                .sorted(Comparator.comparing(KeyLabelStore::getValue))
                .collect(Collectors.toList());
    }

    /**
     * Returns all trigger categories as {@link KeyLabelStore}s.
     *
     * @param locale The user's locale for i18n.
     *
     * @return Set of all available trigger categories.
     */
    @GetMapping("trigger")
    public List<KeyLabelStore> getTriggerCategories(Locale locale) {
        return igorComponentRegistry.getCategoriesOfComponentType(Trigger.class).stream()
                .map(categoryId -> new KeyLabelStore(categoryId, messageSource.getMessage(categoryId, null, locale)))
                .sorted(Comparator.comparing(KeyLabelStore::getValue))
                .collect(Collectors.toList());
    }

}
