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
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generator for documentation of igor components. Parses the class files in the module and searches for igor components.
 * <p>
 * If found, converts the class' JavaDoc to Markdown and saves the file under src/main/resources/doc-gen. Documentation for the
 * component's parameters are appended to the file.
 */
@Slf4j
@Named
@Singleton
public class DocumentationGenerator {

    /**
     * A primitive converter for HTML to Markdown.
     */
    private final PrimitiveHtmlToMdConverter primitiveHtmlToMdConverter;

    /**
     * A converter for igor parameter properties to Markdown documentation.
     */
    private final IgorParamsMdDocGenerator igorParamsMdDocGenerator;

    /**
     * Creates a new instance.
     *
     * @param primitiveHtmlToMdConverter A primitive converter for HTML to Markdown.
     * @param igorParamsMdDocGenerator   A converter for igor parameter properties to Markdown documentation.
     */
    @Inject
    public DocumentationGenerator(PrimitiveHtmlToMdConverter primitiveHtmlToMdConverter, IgorParamsMdDocGenerator igorParamsMdDocGenerator) {
        this.primitiveHtmlToMdConverter = primitiveHtmlToMdConverter;
        this.igorParamsMdDocGenerator = igorParamsMdDocGenerator;
    }

    /**
     * Generates documentation of all igor components found in the module.
     *
     * @param projectRoot     The module's root path.
     * @param sourcesDir      The directory containing the java source files.
     * @param docGenTargetDir The directory to put the generated documentation in.
     * @param docTargetDir    The directory where manually created documentation lies in.
     * @throws IOException In case of generation errors.
     */
    public void generateDoc(Path projectRoot, String sourcesDir, String docGenTargetDir, String docTargetDir) throws IOException {

        igorParamsMdDocGenerator.initialize(projectRoot, sourcesDir, PluginConstants.I18N_SOURCES, new IgorComponentUtil());

        var srcDir = Paths.get(projectRoot.toString(), sourcesDir);

        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(srcDir));
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);

        var parserConfiguration = new ParserConfiguration();
        parserConfiguration.setSymbolResolver(symbolSolver);
        var javaParser = new JavaParser(parserConfiguration);

        getSourceFiles(srcDir).forEach(file -> {
            try {
                ParseResult<CompilationUnit> result = javaParser.parse(file);

                var compilationUnit = result.getResult().orElseThrow();
                var igorComponentCollector = new IgorComponentCollector(file, result, combinedTypeSolver);
                igorComponentCollector.visit(compilationUnit, null);
                igorComponentCollector.getIgorComponents().forEach((classDeclaration, typeId) -> {

                    checkErrors(file, result, projectRoot, docTargetDir, typeId);

                    StringBuilder documentation = new StringBuilder();

                    classDeclaration.getJavadoc()
                        .ifPresent(javadoc -> documentation.append(primitiveHtmlToMdConverter.convert(javadoc.toText())));

                    documentation.append(igorParamsMdDocGenerator.generateDocumentation(classDeclaration, typeId));

                    createDocFile(projectRoot, docGenTargetDir, typeId, documentation.toString());
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
     * Checks for errors during parsing and logs an error message if required.
     * <p>
     * Logging is suppressed when manually created documentation for the component exists.
     *
     * @param file         The parsed file.
     * @param parseResult  The parser's results.
     * @param projectRoot  The module's root path.
     * @param docTargetDir The directory where manually crated documentation lies in.
     * @param typeId       The igor component's Type-ID.
     */
    private void checkErrors(Path file, ParseResult<CompilationUnit> parseResult, Path projectRoot, String docTargetDir, String typeId) {
        var targetDir = projectRoot + docTargetDir;
        if (!parseResult.getProblems().isEmpty() &&
            !Files.exists(Paths.get(targetDir, typeId + ".md"))) {
            log.error("Could not parse file {} (generated documentation might be incomplete): {}", file,
                parseResult.getProblems().stream()
                    .map(Problem::toString)
                    .collect(Collectors.joining())
            );
        }
    }

    /**
     * Visitor that checks parsed classes for an {@link IgorComponent} annotation.
     */
    @Getter
    @SuppressWarnings("java:S3985") // The class is used but Sonar doesn't recognize it...
    @RequiredArgsConstructor
    private static class IgorComponentCollector extends VoidVisitorAdapter<ClassOrInterfaceDeclaration> {

        /**
         * The source file containing the original javadoc comments.
         */
        private final Path sourceFile;

        /**
         * The result of the java parser for the source file.
         */
        private final ParseResult<CompilationUnit> parseResult;

        /**
         * Type solver for analyzing parsed source files.
         */
        private final CombinedTypeSolver combinedTypeSolver;

        /**
         * Contains all found igor components together with their respective Type-ID.
         */
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
                .ifPresent(memberValuePair -> {
                    if (memberValuePair.getValue() instanceof FieldAccessExpr fieldAccessExpr) {
                        String typeClassName = fieldAccessExpr.getScope().asNameExpr().getNameAsString();
                        parseResult.ifSuccessful(cu -> {
                            String qualifiedTypeClassName = cu.getImports().stream()
                                .map(NodeWithName::getNameAsString)
                                .filter(importStatement -> importStatement.endsWith(typeClassName))
                                .findFirst().orElseThrow(() -> new IgorException("Could not find Import for type ID resolution. Please avoid asterisks in class imports when generating igor component documentation."));
                            ResolvedReferenceTypeDeclaration resolvedReferenceTypeDeclaration = combinedTypeSolver.solveType(qualifiedTypeClassName);
                            JavaParserFieldDeclaration javaParserFieldDeclaration = (JavaParserFieldDeclaration) resolvedReferenceTypeDeclaration.getField(fieldAccessExpr.getNameAsString());
                            StringLiteralExpr expression = javaParserFieldDeclaration.getVariableDeclarator().getInitializer().orElseThrow().asStringLiteralExpr();
                            igorComponents.put(n, expression.getValue());
                        });
                    } else {
                        igorComponents.put(n, memberValuePair.getValue().asStringLiteralExpr().asString());
                    }
                });
        }
    }

}
