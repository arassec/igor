package com.arassec.igor.web.controller;


import com.arassec.igor.web.util.DocumentationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Controller for igor's documentation.
 */
@RestController
@RequestMapping("/api/doc")
@Slf4j
@RequiredArgsConstructor
public class DocumentationRestController {

    /**
     * Returns the documentation for the given key.
     *
     * @param key The documentation key.
     *
     * @return The documentation as String if it exists.
     */
    @GetMapping(value = "{key}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> readDoc(@PathVariable String key) {
        String doc = DocumentationUtil.readDocumentation(key, LocaleContextHolder.getLocale());
        if (!StringUtils.isEmpty(doc)) {
            return new ResponseEntity<>(doc, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
