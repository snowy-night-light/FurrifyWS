package ws.furrify.storage.shared.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

public class ContentHtmlSanitizerUtil {

    private static final PolicyFactory EPUB_POLICY = new HtmlPolicyBuilder()
            .allowElements(
                    "hr", "br", "section", "article", "nav", "aside",
                    "header", "footer", "main", "figure", "figcaption",
                    "ruby", "rt", "rp", "bdi", "bdo", "mark", "time",
                    "data", "wbr", "dfn", "kbd", "samp", "var",
                    "q", "cite", "abbr", "dl", "dt", "dd", "address",
                    "span", "small", "big"
            )
            .allowAttributes("id", "title", "dir", "lang", "xml:lang", "epub:type", "role", "class").globally()
            .toFactory();

    private static final PolicyFactory POLICY = Sanitizers.BLOCKS
            .and(Sanitizers.LINKS)
            .and(Sanitizers.STYLES)
            .and(Sanitizers.FORMATTING)
            .and(Sanitizers.TABLES)
            .and(Sanitizers.IMAGES)
            .and(EPUB_POLICY);

    public static String sanitize(String htmlContent) {
        return POLICY.sanitize(htmlContent);
    }
}
