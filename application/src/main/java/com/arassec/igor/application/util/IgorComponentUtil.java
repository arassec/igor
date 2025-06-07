package com.arassec.igor.application.util;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.action.MissingComponentAction;
import com.arassec.igor.core.model.connector.MissingComponentConnector;
import com.arassec.igor.core.model.trigger.MissingComponentTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility with common methods to help with processing {@link IgorComponent}s.
 */
@Component
public class IgorComponentUtil {

    /**
     * Returns the component's Type-ID.
     *
     * @param igorComponent The component to get the Type-ID from.
     *
     * @return The component's Type-ID.
     */
    public String getTypeId(IgorComponent igorComponent) {
        if (igorComponent instanceof MissingComponentAction) {
            return "missing-component-action";
        } else if (igorComponent instanceof MissingComponentTrigger) {
            return "missing-component-trigger";
        } else if (igorComponent instanceof MissingComponentConnector) {
            return "missing-component-connector";
        }
        return Objects.requireNonNull(
            igorComponent.getClass().getAnnotation(com.arassec.igor.application.annotation.IgorComponent.class)).typeId();
    }

    /**
     * Returns the component's Category-ID.
     *
     * @param igorComponent The component to get the Category-ID from.
     *
     * @return The component's Category-ID.
     */
    public String getCategoryId(IgorComponent igorComponent) {
        if (igorComponent instanceof MissingComponentAction
            || igorComponent instanceof MissingComponentTrigger
            || igorComponent instanceof MissingComponentConnector) {
            return "core";
        }
        String result = Objects.requireNonNull(
            igorComponent.getClass().getAnnotation(com.arassec.igor.application.annotation.IgorComponent.class)).categoryId();
        if (StringUtils.hasText(result)) {
            return result;
        }
        return com.arassec.igor.application.annotation.IgorComponent.DEFAULT_CATEGORY;
    }

    /**
     * Formats an igor parameter's property name into a more user-readable way.
     * <p>
     * Will convert Camel-Case property names into parts seperated by spaces while converting the first character to upper case,
     * e.g. "superDuperIgorParameter" will become "Super Duper Igor Parameter"
     *
     * @param igorParamName The parameter's name.
     *
     * @return The formatted name.
     */
    public String formatIgorParamName(String igorParamName) {
        if (!StringUtils.hasText(igorParamName)) {
            return igorParamName;
        }

        return Arrays.stream(igorParamName.split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])"))
            .map(word -> {
                if (word.isEmpty()) {
                    return word;
                }
                return Character.toTitleCase(word.charAt(0)) + word.substring(1).toLowerCase();
            })
            .collect(Collectors.joining(" "));
    }

}
