package org.opencdmp.commons;

public final class SQLUtils {

    private SQLUtils() {}

    public static String toLucene(String input) {
        if (input == null) return "*";

        String s = input.strip();
        if (s.isEmpty()) return "*";

        boolean isExact = s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"");
        String payload = isExact ? s.substring(1, s.length() - 1) : s;

        // Handle the "" case: avoid sending empty phrase queries to Lucene
        if (payload.isEmpty()) return "*";

        // Unified escaping for all content
        String escaped = payload.replaceAll("([.+^=!:${}()|\\[\\]/\\\\])", "\\\\$1");
        escaped = escaped.replace("\"", "\\\"");

        if (isExact) {
            return "\"" + escaped + "\"";
        }

        // Could also check payload; with the current escaping rules, the result is equivalent.
        boolean hasWildcard = escaped.contains("*") || escaped.contains("?");
        return hasWildcard ? escaped : "*" + escaped + "*";
    }


    public static String toPostgres(String string) {
        string = string.strip();
        string = string.replaceAll("([.+^=!:${}()|\\[\\]/\\\\])", "\\\\$1");
        StringBuilder out = new StringBuilder();
        boolean hasWildcard = false;
        boolean isExact = string.length() >= 2 && string.startsWith("\"") && string.endsWith("\""); // fix: single char funny bug
        if (isExact) string = string.substring(1, string.length() - 1);
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch (c) {
                //escape
                case '*' -> {
                    hasWildcard = true;
                    out.append('%');
                }
                case '?' -> {
                    hasWildcard = true;
                    out.append('_');
                }
                case '%', '_' -> {
                    out.append('\\').append(c);
                }
                default ->
                {
                    out.append(c);
                }
            }
        }
        return hasWildcard || isExact ? out.toString() : "%" + out.toString() + "%";
    }
}
