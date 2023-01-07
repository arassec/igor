package com.arassec.igor.maven.docgen.markdown;

import com.arassec.igor.application.util.IgorComponentUtil;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link IgorParamsMdDocGenerator}.
 */
class IgorParamsMdDocGeneratorTest {

    /**
     * The converter under test.
     */
    private final IgorParamsMdDocGenerator converter = new IgorParamsMdDocGenerator();

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    @SneakyThrows
    void inititialize() {
        ReflectionTestUtils.setField(converter, "primitiveHtmlToMdConverter", new PrimitiveHtmlToMdConverter());
        converter.initialize(Path.of("."), "", "/src/test/resources", new IgorComponentUtil());
    }

    /**
     * Tests generating igor parameters Markdown documentation
     */
    @Test
    @DisplayName("Tests generating igor parameters Markdown documentation.")
    @SneakyThrows
    void testGenerateDocumentation() {
        var javaParser = new JavaParser();

        ParseResult<CompilationUnit> result = javaParser.parse(Path.of("src/test/java/com/arassec/igor/maven/test/sources" +
            "/component/TestComponent.java"));
        var compilationUnit = result.getResult().orElseThrow();


        String generatedDoc = converter.generateDocumentation(compilationUnit.getClassByName("TestComponent").orElseThrow(),
            "maven-plugin-test-type-id");

        converter.teardown();

        assertEquals(Files.readString(Path.of("src/test/resources/expected-parameter-documentation.md")), generatedDoc);
    }

}
