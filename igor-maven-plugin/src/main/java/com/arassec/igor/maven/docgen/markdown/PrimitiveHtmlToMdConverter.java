package com.arassec.igor.maven.docgen.markdown;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts HTML JavaDoc comments in {@link com.arassec.igor.application.annotation.IgorComponent}s into markdown format.
 * <p>
 * Uses a simple mechanism to convert key HTML elements into their markdown equivalents. Not all JavaDoc HTML features are
 * supported for conversion, only what's necessary to crete the basic igor documentation markdown files is implemented.
 */
public class PrimitiveHtmlToMdConverter {

    /**
     * Converts the supplied JavaDoc into markdown format.
     *
     * @param javadoc The component's JavaDoc as String.
     *
     * @return The created markdown as String.
     */
    public String convert(String javadoc) {
        Document parsedJavaDoc = Jsoup.parse(javadoc);
        var result = new StringBuilder();
        parsedJavaDoc.body().childNodes().forEach(node -> result.append(processNode(node)));
        return result.toString().trim().replace("\n\n\n", "\n\n");
    }

    /**
     * Converts an HTML node into a markdown representation.
     * <p>
     * Only HTML nodes relevant for igor's documentation are supported!
     *
     * @param node The HTML node to convert.
     *
     * @return The corresponding markdown as String.
     */
    private String processNode(Node node) {
        if (node instanceof TextNode) {
            String text = ((TextNode) node).text();
            if (!" ".equals(text)) {
                if (text.startsWith(" ")) {
                    return text.substring(1);
                } else {
                    return text;
                }
            }
        } else if (node instanceof Element) {
            var result = new StringBuilder();
            var element = ((Element) node);
            switch (element.tagName()) {
                case "h1":
                case "h2":
                case "h3":
                case "h4":
                case "h5":
                case "h6":
                    convertHeader(element, result, Integer.parseInt(element.tagName().replace("h", "")));
                    break;
                case "br":
                    convertBreak(result);
                    break;
                case "strong":
                case "b":
                    convertStrong(element, result);
                    break;
                case "i":
                    convertItalic(element, result);
                    break;
                case "a":
                    convertAnchor(element, result);
                    break;
                case "table":
                    convertTable(element, result);
                    break;
                case "pre":
                    convertPreformatted(element, result);
                    break;
                case "p":
                    convertParagraph(element, result);
                    break;
                default:
                    break;
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
        target.append(Stream.generate(() -> "#").limit(order).collect(Collectors.joining()))
            .append(" ");
        element.childNodes().forEach(childNode -> target.append(processNode(childNode)));
        target.append("\n");
    }

    /**
     * Converts an HTML "break" element into markdown.
     *
     * @param target  The {@link StringBuilder} containing the target markdown.
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
        target.append("\n");
        element.getElementsByTag("th").forEach(headerColumn -> {
            headerColumn.childNodes().forEach(childNode -> target.append(processNode(childNode)));
            target.append(" | ");
        });
        target.append("\n");
        // Header separator: ':---|:---|:---'
        target.append(element.getElementsByTag("th").stream()
            .map(columnHeaderElement -> ":---")
            .collect(Collectors.joining("|"))).append("\n");
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

}
