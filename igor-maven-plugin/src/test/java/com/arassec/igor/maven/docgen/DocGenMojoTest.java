package com.arassec.igor.maven.docgen;

import com.arassec.igor.core.util.IgorException;
import lombok.SneakyThrows;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link DocGenMojo}.
 */
class DocGenMojoTest {

    /**
     * Tests executing the Mojo.
     */
    @Test
    @DisplayName("Tests executing the Mojo.")
    @SneakyThrows
    void testExecute() {
        MavenProject mavenProject = new MavenProject();
        mavenProject.setBasedir(new File("unit-test"));

        DocumentationGenerator documentationGenerator = mock(DocumentationGenerator.class);

        DocGenMojo docGenMojo = new DocGenMojo(documentationGenerator);
        docGenMojo.setProject(mavenProject);

        docGenMojo.execute();

        verify(documentationGenerator, times(1)).generateDoc(Path.of("unit-test"), "/src/main/java",
            "/src/main/resources/doc-gen/", "/src/main/resources/doc/");
    }

    /**
     * Tests executing the Mojo with exceptions during execution.
     */
    @Test
    @DisplayName("Tests executing the Mojo with exceptions during execution.")
    @SneakyThrows
    void testExecuteFailsafe() {
        MavenProject mavenProject = new MavenProject();
        mavenProject.setBasedir(new File("unit-test"));

        DocumentationGenerator documentationGenerator = mock(DocumentationGenerator.class);
        doThrow(new IOException("test-exception")).when(documentationGenerator).generateDoc(any(Path.class), any(String.class), any(String.class), any(String.class));

        DocGenMojo docGenMojo = new DocGenMojo(documentationGenerator);
        docGenMojo.setProject(mavenProject);

        assertThrows(IgorException.class, docGenMojo::execute);
    }

}
