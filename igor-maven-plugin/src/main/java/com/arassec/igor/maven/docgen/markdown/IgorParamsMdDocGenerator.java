package com.arassec.igor.maven.docgen.markdown;

import com.arassec.igor.application.util.IgorComponentUtil;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts the JavaDoc annotations of every {@link IgorParam} found in an {@link com.arassec.igor.core.model.IgorComponent} into
 * a markdown documentation for igor.
 */
@Slf4j
@Singleton
public class IgorParamsMdDocGenerator {

    /**
     * The primitive "JavaDoc to markdown" converter of this plugin.
     */
    @Inject
    private PrimitiveHtmlToMdConverter primitiveHtmlToMdConverter;

    /**
     * The Classloader.
     */
    private URLClassLoader urlClassLoader;

    /**
     * {@link ResourceBundle} for I18N of the parameter names.
     */
    private ResourceBundle resourceBundle;

    /**
     * Utility to work with the igor component.
     */
    private IgorComponentUtil igorComponentUtil;

    /**
     * Root path to the sources of the module to process.
     */
    private Path sourcesRoot;

    /**
     * The parser for the Java sources.
     */
    private JavaParser javaParser;

    /**
     * Initializes the converter.
     *
     * @param projectRoot       The project's root path.
     * @param sourceDir         The path to the project's sources.
     * @param i18nSourceDir     The path to the project's I18N sources.
     * @param igorComponentUtil Igor's component utility.
     *
     * @throws IOException In case the I18N files could not be read.
     */
    public void initialize(Path projectRoot, String sourceDir, String i18nSourceDir, IgorComponentUtil igorComponentUtil) throws IOException {
        this.igorComponentUtil = igorComponentUtil;

        String i18nDir = projectRoot.toString() + i18nSourceDir;

        String basename = determineResourceBundleBasename(i18nDir);

        if (StringUtils.hasText(basename)) {
            var i18nFiles = new File(i18nDir);
            URL[] i18nFilesUri = {i18nFiles.toURI().toURL()}; // NOSONAR - var and array initialization don't work together.
            urlClassLoader = new URLClassLoader(i18nFilesUri);
            resourceBundle = ResourceBundle.getBundle(basename, Locale.getDefault(), urlClassLoader);
        }

        sourcesRoot = Paths.get(projectRoot.toString(), sourceDir);
        var parserConfiguration = new ParserConfiguration();
        parserConfiguration.setSymbolResolver(new JavaSymbolSolver(
            new CombinedTypeSolver(new JavaParserTypeSolver(sourcesRoot))));
        javaParser = new JavaParser(parserConfiguration);
    }

    /**
     * Cleans up the environment after finishing conversion.
     *
     * @throws IOException In case the internally used Classloader cannot be closed.
     */
    public void teardown() throws IOException {
        if (urlClassLoader != null) {
            urlClassLoader.close();
        }
    }

    /**
     * Converts all parameter documentation of the supplied component into markdown.
     *
     * @param igorComponent The component to create the paremeter's markdown from.
     * @param typeId        The component's typeId, required for I18N.
     *
     * @return Documentation of all parameters of the component in markdown format.
     */
    public String generateDocumentation(ClassOrInterfaceDeclaration igorComponent, String typeId) {
        var result = new StringBuilder();

        List<FieldDeclaration> igorParams = collectIgorParams(igorComponent);

        igorParams = igorParams.stream().sorted((o1, o2) -> {
            Integer firstSortIndex = extractSortIndex(o1);
            Integer secondSortIndex = extractSortIndex(o2);
            return firstSortIndex.compareTo(secondSortIndex);
        }).toList();

        if (!igorParams.isEmpty()) {
            writeHeader(result);
            igorParams.forEach(igorParam -> appendParameter(result, igorParam, typeId));
        }

        // Normalize EOL into the expected (UNIX) format:
        return result.toString().replaceAll("(\r\n|\r)", "\n");
    }

    /**
     * Collects {@link FieldDeclaration}s of all fields annotated as igor parameters.
     *
     * @param igorComponent The component to get the fields from.
     *
     * @return List of igor parameters, including fields from (available) super-classes.
     */
    private List<FieldDeclaration> collectIgorParams(ClassOrInterfaceDeclaration igorComponent) {
        List<FieldDeclaration> igorParams = igorComponent.findAll(FieldDeclaration.class).stream()
            .filter(fieldDeclaration -> fieldDeclaration.getAnnotations().stream()
                .anyMatch(annotation -> annotation.getName().asString().equals(IgorParam.class.getSimpleName())))
            .collect(Collectors.toList());
        var extendedClassesIterator = igorComponent.getExtendedTypes().stream().iterator();
        while (extendedClassesIterator.hasNext()) {
            var extendedClass = extendedClassesIterator.next();
            ParseResult<CompilationUnit> parseResult;
            try {
                parseResult = javaParser.parse(Path.of(sourcesRoot.toString(),
                    ((ResolvedReferenceType) extendedClass.resolve()).getQualifiedName().replace(".", "/") + ".java"
                ));
                var compilationUnit = parseResult.getResult().orElseThrow();
                var collector = new ClassOrInterfaceDeclarationCollector();
                collector.visit(compilationUnit, null);
                igorParams.addAll(collectIgorParams(collector.getClassOrInterfaceDeclaration()));
            } catch (Exception e) {
                log.debug("IgorMavenPlugin: This might be an issue, but probably is just a super-class we can't access the " +
                    "sources of! In this case the exception can be ignored.", e);
            }
        }
        return igorParams;
    }

    /**
     * Extracts the sort index from the igor parameter annotation of a field.
     *
     * @param fieldDeclaration The field to get the sort index for.
     *
     * @return The sort index.
     */
    private int extractSortIndex(FieldDeclaration fieldDeclaration) {
        Integer sortIndex = 0;
        AnnotationExpr o1Annotation = fieldDeclaration.getAnnotationByName(IgorParam.class.getSimpleName()).orElseThrow();
        if (o1Annotation.isNormalAnnotationExpr()) {
            Expression value = ((NormalAnnotationExpr) o1Annotation).getPairs().stream().filter(memberValuePair -> memberValuePair.getName().asString().equals("sortIndex")).findFirst().orElse(new MemberValuePair("fallback", new IntegerLiteralExpr("0"))).getValue();
            if (value.isIntegerLiteralExpr()) {
                sortIndex = (Integer) value.asIntegerLiteralExpr().asNumber();
            } else if (value.isBinaryExpr() && value.asBinaryExpr().getLeft().toString().equals("Integer.MAX_VALUE")) {
                sortIndex =
                    Integer.MAX_VALUE - (Integer) value.asBinaryExpr().getRight().asIntegerLiteralExpr().asNumber();
            }
        }
        return sortIndex;
    }

    /**
     * Writes the markdown header for the igor parameter documentation.
     *
     * @param target The target {@link StringBuilder} to write the header to.
     */
    private void writeHeader(StringBuilder target) {
        target.append("\n\n## Parameters\nThe component can be configured by the following parameters:\n\n");
        target.append("| Parameter | Description |\n");
        target.append("| :--- | :--- |\n");
    }

    /**
     * Appends the documentation for a single parameter to the markdown documentation.
     *
     * @param target           The {@link StringBuilder} containing the parameter documentation.
     * @param fieldDeclaration The igor parameter field.
     * @param typeId           The component's type ID.
     */
    private void appendParameter(StringBuilder target, FieldDeclaration fieldDeclaration, String typeId) {
        fieldDeclaration.getAnnotations().stream()
            .filter(annotation -> annotation.getName().asString().equals(IgorParam.class.getSimpleName()))
            .findFirst()
            .flatMap(annotationExpr -> fieldDeclaration.getJavadoc())
            .ifPresent(javadoc -> {
                var paramName = fieldDeclaration.getVariable(0).getNameAsString();
                if (resourceBundle != null) {
                    try {
                        paramName = resourceBundle.getString(typeId + "." + paramName);
                    } catch (MissingResourceException e) {
                        paramName = igorComponentUtil.formatIgorParamName(paramName);
                    }
                } else {
                    paramName = igorComponentUtil.formatIgorParamName(paramName);
                }

                target.append("| ")
                    .append(paramName)
                    .append(" | ")
                    .append(primitiveHtmlToMdConverter.convert(javadoc.toText().replaceAll("(\r\n|\n)", " ")))
                    .append(" |\n");
            });
    }

    /**
     * Tries to determine igor's default I18N properties file for translating the parameter names.
     *
     * @param directory The directory containing properties files for I18N.
     *
     * @return Path to the default I18N file or an empty string, if none exists.
     */
    private String determineResourceBundleBasename(String directory) {
        if (Files.exists(Path.of(directory))) {
            try (Stream<Path> fileCandidates = Files.list(Path.of(directory))) {
                String candidate = fileCandidates
                    .map(path -> path.getFileName().toString())
                    .filter(file -> file.endsWith("-labels.properties"))
                    .filter(StringUtils::hasText)
                    .findFirst()
                    .orElse("");
                return candidate.replace(".properties", "");
            } catch (IOException e) {
                log.debug("Directory with I18N sources does not exist. Parameter names will not be translated.", e);
            }
        }
        return "";
    }

    /**
     * Collects the class or interface declaration of a parsed Java source file.
     */
    private static class ClassOrInterfaceDeclarationCollector extends VoidVisitorAdapter<ClassOrInterfaceDeclaration> {

        @Getter
        private ClassOrInterfaceDeclaration classOrInterfaceDeclaration;

        @Override
        public void visit(ClassOrInterfaceDeclaration n, ClassOrInterfaceDeclaration arg) {
            classOrInterfaceDeclaration = n;
        }
    }

}
