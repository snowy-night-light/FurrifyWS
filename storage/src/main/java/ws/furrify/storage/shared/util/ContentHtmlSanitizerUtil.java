/*
 * furrify-storage-service - Furrify Workspace Project
 * Copyright © 2026 FurrifyWS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
