package com.arassec.igor.web.controller;

import com.arassec.igor.core.application.IgorComponentRegistry;
import com.arassec.igor.web.model.KeyLabelStore;
import com.arassec.igor.web.model.TypeData;
import com.arassec.igor.web.util.DocumentationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
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
     * Returns all types of a certain category as {@link KeyLabelStore}s.
     *
     * @param locale   The user's locale for i18n.
     * @param category The category to use.
     *
     * @return Set of types.
     */
    @GetMapping("action/{category}")
    public List<TypeData> getActionTypes(Locale locale, @PathVariable("category") String category) {
        return igorComponentRegistry.getActionTypesOfCategory(category).stream()
                .map(typeId -> createTypeData(locale, typeId))
                .sorted(Comparator.comparing(TypeData::getValue))
                .collect(Collectors.toList());
    }

    /**
     * Returns all types of a certain category as {@link KeyLabelStore}s.
     *
     * @param locale   The user's locale for i18n.
     * @param category The category to use.
     *
     * @return Set of types.
     */
    @GetMapping("trigger/{category}")
    public List<TypeData> getTriggerTypes(Locale locale, @PathVariable("category") String category) {
        return igorComponentRegistry.getTriggerTypesOfCategory(category).stream()
                .map(typeId -> createTypeData(locale, typeId))
                .sorted(Comparator.comparing(TypeData::getValue))
                .collect(Collectors.toList());
    }

    /**
     * Returns all types of a certain category as {@link KeyLabelStore}s.
     *
     * @param locale   The user's locale for i18n.
     * @param category The category to use.
     *
     * @return Set of types.
     */
    @GetMapping("connector/{category}")
    public List<TypeData> getConnectorTypes(Locale locale, @PathVariable("category") String category) {
        return igorComponentRegistry.getConnectorTypesOfCategory(category).stream()
                .map(typeId -> createTypeData(locale, typeId))
                .sorted(Comparator.comparing(TypeData::getValue))
                .collect(Collectors.toList());
    }

    /**
     * Creates the type data for the required type.
     *
     * @param locale The user's locale for I18N.
     * @param typeId The type's ID.
     *
     * @return a newly created {@link TypeData}.
     */
    private TypeData createTypeData(Locale locale, String typeId) {
        return new TypeData(typeId, messageSource.getMessage(typeId, null, locale),
                DocumentationUtil.isDocumentationAvailable(typeId, LocaleContextHolder.getLocale()));
    }

}
