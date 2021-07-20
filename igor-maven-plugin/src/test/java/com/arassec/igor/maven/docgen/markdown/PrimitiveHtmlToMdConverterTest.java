package com.arassec.igor.maven.docgen.markdown;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Teste the {@link PrimitiveHtmlToMdConverter}.
 */
class PrimitiveHtmlToMdConverterTest {

    /**
     * The converter under test.
     */
    private final PrimitiveHtmlToMdConverter converter = new PrimitiveHtmlToMdConverter();

    /**
     * Tests converting HTML header elements.
     */
    @Test
    @DisplayName("Tests converting HTML header elements.")
    void testConvertHeaders() {
        assertEquals("# header-test", converter.convert("<h1>header-test</h1>"));
        assertEquals("## header-test", converter.convert("<h2>header-test</h2>"));
        assertEquals("### header-test", converter.convert("<h3>header-test</h3>"));
        assertEquals("#### header-test", converter.convert("<h4>header-test</h4>"));
        assertEquals("##### header-test", converter.convert("<h5>header-test</h5>"));
        assertEquals("###### header-test", converter.convert("<h6>header-test</h6>"));
    }

    /**
     * Tests converting HTML break elements.
     */
    @Test
    @DisplayName("Tests converting HTML break elements.")
    void testConvertBr() {
        assertEquals("break-test", converter.convert("break-test<br>"));
        assertEquals("break-test", converter.convert("break-test<br/>"));
        assertEquals("break-test\n\nbreak-test", converter.convert("break-test<br>break-test"));
        assertEquals("break-test\n\nbreak-test", converter.convert("break-test<br/>break-test"));
        assertEquals("break-test \n\nbreak-test", converter.convert("break-test <br>\nbreak-test"));
        assertEquals("break-test \n\nbreak-test", converter.convert("break-test <br/>\nbreak-test"));
    }

    /**
     * Tests converting bold and strong HTML elements.
     */
    @Test
    @DisplayName("Tests converting bold and strong HTML elements.")
    void testConvertStrongAndBold() {
        assertEquals("**strong-test**", converter.convert("<strong>strong-test</strong>"));
        assertEquals("**bold-test**", converter.convert("<b>bold-test</b>"));
    }

    /**
     * Tests converting italic HTML elements.
     */
    @Test
    @DisplayName("Tests converting italic HTML elements.")
    void testConvertItalic() {
        assertEquals("*italic-test*", converter.convert("<i>italic-test</i>"));
    }

    /**
     * Tests converting HTML anchor elements.
     */
    @Test
    @DisplayName("Tests converting HTML anchor elements.")
    void testConvertAnchor() {
        assertEquals("[display-name](url)", converter.convert("<a href=\"url\">display-name</a>"));
    }

    /**
     * Tests converting HTML tables.
     */
    @Test
    @DisplayName("Tests converting HTML tables.")
    void testConvertTable() {
        String tableHtml = "<table><tr><th>h1</th><th>h2</th></tr><tr><td>c1</td><td>c2</td></tr></table>";
        String tableMarkdown = "h1 | h2 | \n:---|:---\nc1 | c2 |";
        assertEquals(tableMarkdown, converter.convert(tableHtml));
    }

    /**
     * Tests converting HTML 'pre' elements.
     */
    @Test
    @DisplayName("Tests converting HTML 'pre' elements.")
    void testConvertPreformatted() {
        assertEquals("`pre-test`", converter.convert("<pre>pre-test</pre>"));
        assertEquals("```\npre-code-test\n```", converter.convert("<pre><code>pre-code-test</code></pre>"));
        assertEquals("```\npre-code-test\n```\n\nfollowing", converter.convert("<pre><code>pre-code-test</code></pre>following"));
    }

    /**
     * Tests converting HTML paragraphs.
     */
    @Test
    @DisplayName("Tests converting HTML paragraphs.")
    void testConvertParagraph() {
        assertEquals("previous \n\nparagraph-test", converter.convert("previous <p>paragraph-test</p>"));
        assertEquals("previous \n\nparagraph-test\nfollowing", converter.convert("previous <p>paragraph-test</p>\n following"));
    }

}
