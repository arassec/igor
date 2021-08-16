package com.arassec.igor.maven.docgen;

import com.arassec.igor.maven.docgen.markdown.IgorParamsMdDocGenerator;
import com.arassec.igor.maven.docgen.markdown.PrimitiveHtmlToMdConverter;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DocumentationGenerator}.
 */
@ExtendWith(MockitoExtension.class)
class DocumentationGeneratorTest {

    /**
     * The generator under test.
     */
    @InjectMocks
    private DocumentationGenerator documentationGenerator;

    /**
     * A primitive converter for HTML to Markdown.
     */
    @Mock
    private PrimitiveHtmlToMdConverter primitiveHtmlToMdConverter;

    /**
     * A converter for igor parameter properties to Markdown documentation.
     */
    @Mock
    private IgorParamsMdDocGenerator igorParamsMdDocGenerator;

    /**
     * Tests generating documentation from Java sources.
     */
    @Test
    @DisplayName("Tests generating documentation from Java sources.")
    @SneakyThrows
    void testGenerateDoc() {
        Path targetFile = Path.of("target/gen-docs/maven-plugin-test-type-id.md");
        Files.deleteIfExists(targetFile);

        when(primitiveHtmlToMdConverter.convert(any(String.class))).thenReturn("generated-component-doc\n");

        when(igorParamsMdDocGenerator.generateDocumentation(any(ClassOrInterfaceDeclaration.class),
            eq("maven-plugin-test-type-id"))).thenReturn("generated-parameter-doc");

        documentationGenerator.generateDoc(Path.of("."), "src/test/java/com/arassec/igor/maven/test/sources", "/target/gen-docs");

        String result = Files.readString(targetFile);
        assertEquals("generated-component-doc\ngenerated-parameter-doc", result);
    }

}
