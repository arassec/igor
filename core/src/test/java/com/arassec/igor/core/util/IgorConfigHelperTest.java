package com.arassec.igor.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link IgorConfigHelper} utility.
 */
@DisplayName("Igor-Config-Helper tests")
class IgorConfigHelperTest {

    /**
     * Tests creating a message source for the supplied base-names.
     */
    @Test
    @DisplayName("Tests creating a message source.")
    void testCreateMessageSource() {
        MessageSource messageSource = IgorConfigHelper.createMessageSource("i18n/test");
        assertEquals("label", messageSource.getMessage("key", null, Locale.FRENCH));
        assertEquals("Beschriftung", messageSource.getMessage("key", null, Locale.GERMANY));
    }

}
