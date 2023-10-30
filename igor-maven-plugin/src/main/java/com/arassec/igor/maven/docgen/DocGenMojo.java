package com.arassec.igor.maven.docgen;

import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.maven.PluginConstants;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Creates the markdown documentation files of igor components during the build if none are provided by the developer.
 */
@Getter
@Setter
@Mojo(name = "DocGen", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class DocGenMojo extends AbstractMojo {

    /**
     * The project data provided by maven.
     */
    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    /**
     * The generator for igor component documentation.
     */
    private DocumentationGenerator documentationGenerator;

    /**
     * Creates a new instance.
     *
     * @param documentationGenerator The generator for igor component documentation.
     */
    @Inject
    public DocGenMojo(DocumentationGenerator documentationGenerator) {
        this.documentationGenerator = documentationGenerator;
    }

    /**
     * Creates the generated documentation of igor components.
     */
    public void execute() {
        getLog().info("Generating documentation of igor components.");
        try {
            documentationGenerator.generateDoc(Path.of(project.getBasedir().getPath()),
                PluginConstants.JAVA_SOURCES, PluginConstants.DOC_GEN_TARGET_DIR, PluginConstants.DOC_TARGET_DIR);
        } catch (IOException e) {
            throw new IgorException("Could not generate documentation!", e);
        }
    }

}
