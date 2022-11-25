package com.arassec.igor.maven.docgen;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.application.util.IgorComponentUtil;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.maven.PluginConstants;
import com.arassec.igor.maven.docgen.markdown.IgorParamsMdDocGenerator;
import com.arassec.igor.maven.docgen.markdown.PrimitiveHtmlToMdConverter;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import lombok.Getter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Generator for documentation of igor components. Parses the class files in the module and searches for igor components.
 * <p>
 * If found, converts the class' JavaDoc to Markdown and saves the file under src/main/resources/doc-gen. Documentation for the
 * component's parameters are appended to the file.
 */
@Named
@Singleton
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class DocumentationGenerator {

    /**
     * A primitive converter for HTML to Markdown.
     */
    @Inject
    private PrimitiveHtmlToMdConverter primitiveHtmlToMdConverter;

    /**
     * A converter for igor parameter properties to Markdown documentation.
     */
    @Inject
    private IgorParamsMdDocGenerator igorParamsMdDocGenerator;

    /**
     * Generates documentation of all igor components found in the module.
     *
     * @param projectRoot  The module's root path.
     * @param sourcesDir   The directory containing the java source files.
     * @param docTargetDir The directory to put the generated documentation in.
     *
     * @throws IOException In case of generation errors.
     */
    public void generateDoc(Path projectRoot, String sourcesDir, String docTargetDir) throws IOException {

        igorParamsMdDocGenerator.initialize(projectRoot, sourcesDir, PluginConstants.I18N_SOURCES, new IgorComponentUtil());

        getSourceFiles(Paths.get(projectRoot.toString(), sourcesDir))
            .forEach(file -> {
                var parserConfiguration = new ParserConfiguration();
                parserConfiguration.setSymbolResolver(new JavaSymbolSolver(new JavaParserTypeSolver(Paths.get(projectRoot.toString(), sourcesDir))));
                var javaParser = new JavaParser(parserConfiguration);
                try {
                    ParseResult<CompilationUnit> result = javaParser.parse(file);
                    var compilationUnit = result.getResult().orElseThrow();
                    var igorComponentCollector = new IgorComponentCollector();
                    igorComponentCollector.visit(compilationUnit, null);
                    igorComponentCollector.getIgorComponents().forEach((classDeclaration, typeId) -> {

                        StringBuilder documentation = new StringBuilder();

                        classDeclaration.getJavadoc()
                            .ifPresent(javadoc -> documentation.append(primitiveHtmlToMdConverter.convert(javadoc.toText())));

                        documentation.append(igorParamsMdDocGenerator.generateDocumentation(classDeclaration, typeId));

                        createDocFile(projectRoot, docTargetDir, typeId, documentation.toString());
                    });
                } catch (IOException e) {
                    throw new IgorException("Could not parse java file!", e);
                }
            });

        igorParamsMdDocGenerator.teardown();
    }

    /**
     * Writes the provided content to a documentation file in the modules source dir tree.
     *
     * @param projectRoot  The module's root path.
     * @param docTargetDir The directory to put the generated documentation in.
     * @param typeId       The igor component's Type-ID.
     * @param content      The generated documentation as string.
     */
    private void createDocFile(Path projectRoot, String docTargetDir, String typeId, String content) {
        try {
            var targetDir = projectRoot + docTargetDir;
            Path targetDirPath = Path.of(targetDir);
            if (!Files.exists(targetDirPath)) {
                Files.createDirectories(targetDirPath);
            }
            Files.writeString(Paths.get(targetDir, typeId + ".md"), content);
        } catch (IOException e) {
            throw new IgorException("Could not write target file with igor documentation!", e);
        }
    }

    /**
     * Returns all source files found under the provided path.
     *
     * @param sourcesRoot The path to search sources in.
     *
     * @return List of Java source files found in the directory tree.
     */
    private List<Path> getSourceFiles(Path sourcesRoot) {
        try (Stream<Path> walk = Files.walk(sourcesRoot)) {
            return walk.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .toList();
        } catch (IOException e) {
            throw new IgorException("Could not determine source files!", e);
        }
    }

    /**
     * Visitor that checks parsed classes for an {@link IgorComponent} annotation.
     */
    @SuppressWarnings("java:S3985") // The class is used but Sonar doesn't recognize it...
    private static class IgorComponentCollector extends VoidVisitorAdapter<ClassOrInterfaceDeclaration> {

        /**
         * Contains all found igor components together with their respective Type-ID.
         */
        @Getter
        Map<ClassOrInterfaceDeclaration, String> igorComponents = new HashMap<>();

        /**
         * {@inheritDoc}
         */
        @Override
        public void visit(ClassOrInterfaceDeclaration n, ClassOrInterfaceDeclaration arg) {
            super.visit(n, arg);
            n.getAnnotations().stream()
                .filter(NormalAnnotationExpr.class::isInstance)
                .filter(annotation -> annotation.getName().asString().equals(IgorComponent.class.getSimpleName()))
                .flatMap(normalExpression -> ((NormalAnnotationExpr) normalExpression).getPairs().stream())
                .filter(memberValuePair -> memberValuePair.getName().asString().equals(IgorComponent.TYPE_ID))
                .findFirst()
                .ifPresent(memberValuePair -> igorComponents.put(n, memberValuePair.getValue().asStringLiteralExpr().asString()));
        }
    }

}
