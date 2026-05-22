package org.aspose.pdf.html;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses HTML (possibly malformed) into a DOM Document.
 * Strategy: wrap in XHTML envelope, try strict XML parse,
 * if fails, clean up common issues and retry.
 */
public class HtmlTagParser {
    private static final Logger LOG = Logger.getLogger(HtmlTagParser.class.getName());

    private static final Set<String> VOID_ELEMENTS = Set.of(
        "area","base","br","col","embed","hr","img","input",
        "link","meta","param","source","track","wbr");

    /** HTML5 boolean attributes that need value for XML: disabled → disabled="disabled" */
    private static final Set<String> BOOLEAN_ATTRS = Set.of(
        "allowfullscreen","async","autofocus","autoplay","checked","controls",
        "default","defer","disabled","formnovalidate","hidden","ismap","loop",
        "multiple","muted","nomodule","novalidate","open","playsinline",
        "readonly","required","reversed","selected");

    private HtmlTagParser() {} // utility class

    /**
     * Parses the given HTML string into a DOM {@link org.w3c.dom.Document}.
     *
     * <p>The parser first attempts a strict XML parse after wrapping the input
     * in a minimal XHTML envelope. If that fails (e.g. due to unclosed tags or
     * HTML entities), it applies common clean-up heuristics and retries.</p>
     *
     * @param html the HTML string to parse; may be a fragment or a full document
     * @return a DOM Document representing the parsed HTML
     * @throws IOException if the HTML cannot be parsed even after clean-up
     */
    public static org.w3c.dom.Document parse(String html) throws IOException {
        // Try strict parse first
        try {
            return parseAsXml(ensureXmlStructure(html));
        } catch (Exception e1) {
            // Fall back: clean up common HTML issues
            String cleaned = cleanHtml(html);
            try {
                return parseAsXml(ensureXmlStructure(cleaned));
            } catch (Exception e2) {
                // Last resort: aggressive clean
                try {
                    return parseAsXml(ensureXmlStructure(aggressiveClean(cleaned)));
                } catch (Exception e3) {
                    LOG.warning("HTML parse failed after all cleanup attempts: " + e3.getMessage());
                    // Fallback: preserve body markup if the head section is what
                    // breaks XML parsing (common for legacy HTML with malformed meta/style).
                    try {
                        String bodyOnly = extractBodyHtml(aggressiveClean(cleaned));
                        return parseAsXml(ensureXmlStructure(cleanHtml(bodyOnly)));
                    } catch (Exception e4) {
                        LOG.warning("HTML body-only fallback failed: " + e4.getMessage());
                    }
                    // Ultra-fallback: strip ALL tags and wrap plain text
                    try {
                        String text = cleaned.replaceAll("<[^>]*>", " ")
                            .replaceAll("\\s+", " ").trim();
                        return parseAsXml("<html><body><p>" + escapeXml(text) + "</p></body></html>");
                    } catch (Exception e5) {
                        throw new IOException("Failed to parse HTML: " + e2.getMessage(), e2);
                    }
                }
            }
        }
    }

    /**
     * Cleans common HTML constructs that are not valid XML.
     */
    static String cleanHtml(String html) {
        // Remove IE conditional comments: <!--[if ...]>...<![endif]-->
        html = html.replaceAll("<!--\\[if[^]]*\\]>[\\s\\S]*?<!\\[endif\\]-->", "");
        // Remove non-comment IE conditionals: <![if ...]>...<![endif]>
        html = html.replaceAll("<!\\[if[^]]*\\]>", "");
        html = html.replaceAll("<!\\[endif\\]>", "");
        // Remove CDATA sections
        html = html.replaceAll("<!\\[CDATA\\[[\\s\\S]*?\\]\\]>", "");

        // Remove <script> and <style> content (often contains bare < > that break XML)
        html = html.replaceAll("(?i)<script[^>]*>[\\s\\S]*?</script>", "");
        html = html.replaceAll("(?i)<style[^>]*>[\\s\\S]*?</style>", "");

        // Remove HTML comments (after IE conditionals are handled)
        html = html.replaceAll("<!--[\\s\\S]*?-->", "");

        // Strip xmlns attributes (cause namespace issues in non-namespace-aware mode)
        html = html.replaceAll("\\s+xmlns\\s*=\\s*\"[^\"]*\"", "");
        html = html.replaceAll("\\s+xmlns\\s*=\\s*'[^']*'", "");

        // Fix missing space between attributes: ="value"attr= → ="value" attr=
        html = html.replaceAll("\"([a-zA-Z])", "\" $1");
        // Same for single-quoted
        html = html.replaceAll("'([a-zA-Z])", "' $1");

        // Close void elements: <br> -> <br/>, <BR> -> <BR/>, <img src="x"> -> <img src="x"/>
        // Case-insensitive to handle <BR>, <Hr>, etc.
        for (String ve : VOID_ELEMENTS) {
            html = html.replaceAll("(?i)<(" + ve + ")(\\s[^>]*?)?\\s*(?<!/)>", "<$1$2/>");
        }

        // Fix boolean attributes: <audio controls> → <audio controls="controls">
        for (String ba : BOOLEAN_ATTRS) {
            html = html.replaceAll(
                "(?i)(<[a-zA-Z][^>]*\\s)" + ba + "(?=\\s|/?>)",
                "$1" + ba + "=\"" + ba + "\"");
        }

        // Fix unquoted attribute values: name=value → name="value"
        // But skip already-quoted values and numeric entities
        html = html.replaceAll(
            "(?<=\\s)(\\w+)=([a-zA-Z][a-zA-Z0-9_.:-]*)(?=\\s|/?>)",
            "$1=\"$2\"");

        // Replace named HTML entities with numeric equivalents
        html = replaceHtmlEntities(html);

        // Catch-all: replace any remaining unknown &name; entities (except XML built-ins)
        html = html.replaceAll("&(?!amp;|lt;|gt;|quot;|apos;|#)([a-zA-Z]{2,10});", "&#xFFFD;");

        // Auto-close unclosed tags (skip for very large documents)
        if (html.length() < 500_000) {
            html = autoCloseTag(html, "p");
            html = autoCloseTag(html, "li");
            html = autoCloseTag(html, "td");
            html = autoCloseTag(html, "th");
            html = autoCloseTag(html, "tr");
            html = autoCloseTag(html, "dt");
            html = autoCloseTag(html, "dd");
        }

        // Escape bare ampersands (& not followed by # or letter+;)
        html = html.replaceAll("&(?![a-zA-Z#][a-zA-Z0-9]*;)", "&amp;");

        return html;
    }

    /**
     * Replaces common named HTML entities with numeric equivalents.
     */
    private static String replaceHtmlEntities(String html) {
        html = html.replace("&nbsp;", "&#160;");
        html = html.replace("&mdash;", "&#8212;");
        html = html.replace("&ndash;", "&#8211;");
        html = html.replace("&laquo;", "&#171;");
        html = html.replace("&raquo;", "&#187;");
        html = html.replace("&copy;", "&#169;");
        html = html.replace("&reg;", "&#174;");
        html = html.replace("&trade;", "&#8482;");
        html = html.replace("&bull;", "&#8226;");
        html = html.replace("&hellip;", "&#8230;");
        html = html.replace("&ldquo;", "&#8220;");
        html = html.replace("&rdquo;", "&#8221;");
        html = html.replace("&lsquo;", "&#8216;");
        html = html.replace("&rsquo;", "&#8217;");
        html = html.replace("&euro;", "&#8364;");
        html = html.replace("&pound;", "&#163;");
        html = html.replace("&yen;", "&#165;");
        html = html.replace("&cent;", "&#162;");
        html = html.replace("&times;", "&#215;");
        html = html.replace("&divide;", "&#247;");
        html = html.replace("&deg;", "&#176;");
        html = html.replace("&micro;", "&#181;");
        html = html.replace("&para;", "&#182;");
        html = html.replace("&sect;", "&#167;");
        html = html.replace("&acute;", "&#180;");
        html = html.replace("&cedil;", "&#184;");
        html = html.replace("&ordf;", "&#170;");
        html = html.replace("&ordm;", "&#186;");
        html = html.replace("&iquest;", "&#191;");
        html = html.replace("&iexcl;", "&#161;");
        html = html.replace("&lsaquo;", "&#8249;");
        html = html.replace("&rsaquo;", "&#8250;");
        return html;
    }

    /**
     * Aggressive clean: remove unknown/problematic tags, fix structural issues.
     */
    private static String aggressiveClean(String html) {
        // Remove tags that commonly cause XML issues
        html = html.replaceAll("(?i)<(audio|video|iframe|object|embed|canvas|svg|math|noscript)" +
            "[^>]*>[\\s\\S]*?</\\1>", "");
        html = html.replaceAll("(?i)<(audio|video|iframe|object|embed|canvas|svg|math|noscript)" +
            "[^>]*/?>", "");
        // Remove processing instructions
        html = html.replaceAll("<\\?[^?]*\\?>", "");
        // Remove any remaining <![...]> constructs
        html = html.replaceAll("<!\\[[^]]*\\]>", "");
        // Remove attributes with newlines in values (malformed)
        html = html.replaceAll("\\w+=\\s*\"[^\"]*\\n[^\"]*\"", "");
        // Fix unquoted attributes more aggressively
        html = html.replaceAll("(\\w+)=\\s*([^\"'\\s>][^\\s>]*)(?=\\s|/?>)", "$1=\"$2\"");
        return html;
    }

    private static String extractBodyHtml(String html) {
        Matcher matcher = Pattern.compile("(?is)<body[^>]*>(.*)</body>").matcher(html);
        if (matcher.find()) {
            return "<html><body>" + matcher.group(1) + "</body></html>";
        }
        return "<html><body>" + html + "</body></html>";
    }

    /**
     * Auto-closes unclosed tags of the given name by inserting closing tags
     * before the next opening tag of the same name or before certain parent closes.
     */
    private static String autoCloseTag(String html, String tag) {
        StringBuilder sb = new StringBuilder();
        String lower = html.toLowerCase();
        int pos = 0;
        int openCount = 0;

        while (pos < html.length()) {
            if (pos < lower.length() - tag.length() - 1
                    && lower.charAt(pos) == '<'
                    && lower.substring(pos + 1).startsWith(tag)
                    && (pos + 1 + tag.length() < lower.length())
                    && !Character.isLetterOrDigit(lower.charAt(pos + 1 + tag.length()))) {
                if (pos + 1 < lower.length() && lower.charAt(pos + 1) != '/') {
                    if (openCount > 0) {
                        sb.append("</").append(tag).append(">");
                    }
                    openCount++;
                }
            }
            if (pos < lower.length() - tag.length() - 2
                    && lower.substring(pos).startsWith("</" + tag)) {
                if (openCount > 0) openCount--;
            }

            sb.append(html.charAt(pos));
            pos++;
        }
        if (openCount > 0) {
            sb.append("</").append(tag).append(">");
        }
        return sb.toString();
    }

    /**
     * Ensures the HTML string has a minimal XML-compatible structure.
     */
    static String ensureXmlStructure(String html) {
        html = html.replaceAll("(?i)<\\?xml[^?]*\\?>", "").trim();
        html = html.replaceAll("(?i)<!DOCTYPE[^>]*>", "").trim();

        String lower = html.toLowerCase();
        if (!lower.contains("<html")) {
            html = "<html><body>" + html + "</body></html>";
        } else {
            // Ensure </body> exists before </html>
            if (!lower.contains("</body>") && lower.contains("</html>")) {
                html = html.replaceAll("(?i)(</html\\s*>)", "</body>$1");
            }
            // Ensure <body> exists
            if (!lower.contains("<body")) {
                // Insert <body> after </head> or after <html...>
                if (lower.contains("</head>")) {
                    html = html.replaceAll("(?i)(</head\\s*>)", "$1<body>");
                    if (!html.toLowerCase().contains("</body>")) {
                        html = html.replaceAll("(?i)(</html\\s*>)", "</body>$1");
                    }
                }
            }
            // Ensure </body> and </html> exist at the end
            lower = html.toLowerCase();
            if (lower.contains("<body") && !lower.contains("</body>")) {
                html = html + "</body>";
            }
            if (lower.contains("<html") && !lower.contains("</html>")) {
                html = html + "</html>";
            }
        }
        return html;
    }

    /**
     * Escapes text for safe inclusion in XML.
     */
    private static String escapeXml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    /**
     * Parses the given XML string into a DOM Document using JAXP.
     */
    private static org.w3c.dom.Document parseAsXml(String xml) throws Exception {
        javax.xml.parsers.DocumentBuilderFactory factory =
            javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        try { factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); }
        catch (Exception ignored) {}
        try { factory.setFeature("http://xml.org/sax/features/external-general-entities", false); }
        catch (Exception ignored) {}
        javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
        // Suppress stderr output from parser
        builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
        return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
