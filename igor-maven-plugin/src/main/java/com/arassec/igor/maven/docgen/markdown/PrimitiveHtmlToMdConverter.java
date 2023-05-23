package com.arassec.igor.maven.docgen.markdown;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts HTML JavaDoc comments in {@link com.arassec.igor.application.annotation.IgorComponent}s into Markdown format.
 * <p>
 * Uses a simple mechanism to convert key HTML elements into their markdown equivalents. Not all JavaDoc HTML features are
 * supported for conversion, only what's necessary to crete the basic igor documentation markdown files is implemented.
 */
@Slf4j
public class PrimitiveHtmlToMdConverter {

    /**
     * Converts the supplied JavaDoc into Markdown format.
     *
     * @param javadoc The component's JavaDoc as String.
     * @return The created markdown as String.
     */
    public String convert(String javadoc) {
        Document parsedJavaDoc = Jsoup.parse(javadoc);
        var result = new StringBuilder();
        parsedJavaDoc.body().childNodes().forEach(node -> result.append(processNode(node)));
        // Normalize EOL into the expected (UNIX) format:
        return result.toString().trim().replaceAll("(\r\n|\r)", "\n").replace("\n\n\n", "\n\n");
    }

    /**
     * Converts an HTML node into a markdown representation.
     * <p>
     * Only HTML nodes relevant for igor's documentation are supported!
     *
     * @param node The HTML node to convert.
     * @return The corresponding markdown as String.
     */
    private String processNode(Node node) {
        if (node instanceof TextNode textNode) {
            String text = textNode.text();
            if (!" ".equals(text)) {
                if (text.startsWith(" ")) {
                    return text.substring(1);
                } else {
                    return text;
                }
            }
        } else if (node instanceof Element element) {
            var result = new StringBuilder();
            switch (element.tagName()) {
                case "h1", "h2", "h3", "h4", "h5", "h6" ->
                    convertHeader(element, result, Integer.parseInt(element.tagName().replace("h", "")));
                case "br" -> convertBreak(result);
                case "strong", "b" -> convertStrong(element, result);
                case "i" -> convertItalic(element, result);
                case "a" -> convertAnchor(element, result);
                case "table" -> convertTable(element, result);
                case "pre" -> convertPreformatted(element, result);
                case "p" -> convertParagraph(element, result);
                case "ul" -> convertList(element, result);
                default -> log.info("Element {} not supported for markdown conversion!", element.tagName());

            }
            return result.toString();
        }
        return "";
    }

    /**
     * Converts an HTML "header" into markdown.
     *
     * @param element The HTML element to convert.
     * @param target  The {@link StringBuilder} containing the target markdown.
     * @param order   The header's order, e.g. 1 for an "h1" element.
     */
    private void convertHeader(Element element, StringBuilder target, int order) {
        target.append("\n\n");
        // JavaDoc DocLint prohibits h1 elements with recent versions, so those are ignored during markdown generation:
        target.append(Stream.generate(() -> "#").limit(order - 1L).collect(Collectors.joining()))
            .append(" ");
        element.childNodes().forEach(childNode -> target.append(processNode(childNode)));
        target.append("\n");
    }

    /**
     * Converts an HTML "break" element into markdown.
     *
     * @param target The {@link StringBuilder} containing the target markdown.
     */
    private void convertBreak(StringBuilder target) {
        target.append("\n\n");
    }

    /**
     * Converts an HTML "strong" or "b" element into markdown.
     *
     * @param element The HTML element to convert.
     * @param target  The {@link StringBuilder} containing the target markdown.
     */
    private void convertStrong(Element element, StringBuilder target) {
        target.append("**");
        element.childNodes().forEach(childNode -> target.append(processNode(childNode)));
        target.append("** ");
    }

    /**
     * Converts an HTML "i" element into markdown.
     *
     * @param element The HTML element to convert.
     * @param target  The {@link StringBuilder} containing the target markdown.
     */
    private void convertItalic(Element element, StringBuilder target) {
        target.append("*");
        element.childNodes().forEach(childNode -> target.append(processNode(childNode)));
        target.append("*");
    }

    /**
     * Converts an HTML "a" element into markdown.
     *
     * @param element The HTML element to convert.
     * @param target  The {@link StringBuilder} containing the target markdown.
     */
    private void convertAnchor(Element element, StringBuilder target) {
        target.append("[");
        target.append(element.text());
        target.append("]");
        target.append("(");
        target.append(element.attr("href"));
        target.append(") ");
    }

    /**
     * Converts an HTML "table" element into markdown.
     *
     * @param element The HTML element to convert.
     * @param target  The {@link StringBuilder} containing the target markdown.
     */
    private void convertTable(Element element, StringBuilder target) {
        // Column headers:
        target.append("\n|");
        element.getElementsByTag("th").forEach(headerColumn -> {
            headerColumn.childNodes().forEach(childNode -> target.append(" ").append(processNode(childNode)));
            target.append(" |");
        });
        target.append("\n|");
        // Header separator: ':---|:---|:---'
        target.append(element.getElementsByTag("th").stream()
            .map(columnHeaderElement -> " :--- ")
            .collect(Collectors.joining("|"))).append("|\n| ");
        // Rows:
        element.getElementsByTag("tr").stream()
            .skip(1) // skip the header!
            .forEach(row -> {
                row.getElementsByTag("td").forEach(column -> {
                    column.childNodes().forEach(childNode -> target.append(processNode(childNode)));
                    target.append(" | ");
                });
                target.append("\n");
            });
        target.append("\n");
    }

    /**
     * Converts an HTML "pre" element into markdown.
     *
     * @param element The HTML element to convert.
     * @param target  The {@link StringBuilder} containing the target markdown.
     */
    private void convertPreformatted(Element element, StringBuilder target) {
        var codeElement = element.getElementsByTag("code");
        if (StringUtils.hasText(codeElement.text())) {
            target.append("\n```\n").append(codeElement.text()).append("\n```\n\n");
        } else {
            target.append("`");
            element.childNodes().forEach(childNode -> target.append(processNode(childNode)));
            target.append("`");
        }
    }

    /**
     * Converts an HTML "p" element into markdown.
     *
     * @param element The HTML element to convert.
     * @param target  The {@link StringBuilder} containing the target markdown.
     */
    private void convertParagraph(Element element, StringBuilder target) {
        target.append("\n\n");
        element.childNodes().forEach(childNode -> target.append(processNode(childNode)));
        target.append("\n");
    }

    /**
     * Converts a HTML list into a markdown list.
     *
     * @param element The "ul" element from the HTML list.
     * @param target The {@link StringBuilder} containing the target markdown.
     */
    private void convertList(Element element, StringBuilder target) {
        Elements listElements = element.getElementsByTag("li");
        listElements.eachText().forEach(text -> {
            target.append("* ");
            target.append(text);
            target.append("\n");
        });
        target.append("\n");
    }

}
