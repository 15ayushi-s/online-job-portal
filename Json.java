package com.jobhub.util;

import com.jobhub.exception.ValidationException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Minimal JSON writer and parser (no external library needed). */
public final class Json {
    private Json() {}

    // ------------------------------------------------------------------ writer
    public static String write(Object value) {
        StringBuilder sb = new StringBuilder();
        write(value, sb);
        return sb.toString();
    }

    private static void write(Object v, StringBuilder sb) {
        if (v == null) sb.append("null");
        else if (v instanceof JsonSerializable js) write(js.toJson(), sb);
        else if (v instanceof Map<?, ?> m) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                quote(String.valueOf(e.getKey()), sb);
                sb.append(':');
                write(e.getValue(), sb);
            }
            sb.append('}');
        } else if (v instanceof Iterable<?> it) {
            sb.append('[');
            boolean first = true;
            for (Object o : it) {
                if (!first) sb.append(',');
                first = false;
                write(o, sb);
            }
            sb.append(']');
        } else if (v instanceof Number || v instanceof Boolean) sb.append(v);
        else quote(v instanceof Enum<?> e ? e.name() : v.toString(), sb);   // String, LocalDateTime, ...
    }

    private static void quote(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
    }

    // ------------------------------------------------------------------ parser
    /** Parses a JSON object into a Map (values: String, Long, Double, Boolean, null, Map, List). */
    public static Map<String, Object> parseObject(String text) {
        Parser p = new Parser(text);
        p.skipWs();
        Object v = p.value(0);
        p.skipWs();
        if (p.pos != text.length()) throw p.error("unexpected trailing characters");
        if (!(v instanceof Map<?, ?>)) throw new ValidationException("JSON body must be an object");
        @SuppressWarnings("unchecked") Map<String, Object> m = (Map<String, Object>) v;
        return m;
    }

    private static final class Parser {
        private static final int MAX_DEPTH = 32;
        private final String s;
        private int pos;

        Parser(String s) { this.s = s; }

        ValidationException error(String why) { return new ValidationException("Malformed JSON: " + why); }

        void skipWs() { while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++; }

        char peek() { if (pos >= s.length()) throw error("unexpected end"); return s.charAt(pos); }

        void expect(char c) { if (peek() != c) throw error("expected '" + c + "'"); pos++; }

        Object value(int depth) {
            if (depth > MAX_DEPTH) throw error("too deeply nested");
            skipWs();
            char c = peek();
            if (c == '{') return object(depth);
            if (c == '[') return array(depth);
            if (c == '"') return string();
            if (s.startsWith("true", pos)) { pos += 4; return Boolean.TRUE; }
            if (s.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
            if (s.startsWith("null", pos)) { pos += 4; return null; }
            return number();
        }

        Map<String, Object> object(int depth) {
            Map<String, Object> m = new LinkedHashMap<>();
            expect('{'); skipWs();
            if (peek() == '}') { pos++; return m; }
            while (true) {
                skipWs();
                String key = string();
                skipWs(); expect(':');
                m.put(key, value(depth + 1));
                skipWs();
                char c = peek();
                pos++;
                if (c == '}') return m;
                if (c != ',') throw error("expected ',' or '}'");
            }
        }

        List<Object> array(int depth) {
            List<Object> l = new ArrayList<>();
            expect('['); skipWs();
            if (peek() == ']') { pos++; return l; }
            while (true) {
                l.add(value(depth + 1));
                skipWs();
                char c = peek();
                pos++;
                if (c == ']') return l;
                if (c != ',') throw error("expected ',' or ']'");
            }
        }

        String string() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = peek();
                pos++;
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    char e = peek();
                    pos++;
                    switch (e) {
                        case '"', '\\', '/' -> sb.append(e);
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'u' -> {
                            if (pos + 4 > s.length()) throw error("bad unicode escape");
                            try { sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16)); }
                            catch (NumberFormatException ex) { throw error("bad unicode escape"); }
                            pos += 4;
                        }
                        default -> throw error("bad escape");
                    }
                } else if (c < 0x20) {
                    throw error("control character in string");
                } else sb.append(c);
            }
        }

        Object number() {
            int start = pos;
            while (pos < s.length() && "+-0123456789.eE".indexOf(s.charAt(pos)) >= 0) pos++;
            String t = s.substring(start, pos);
            if (t.isEmpty()) throw error("unexpected character '" + s.charAt(start) + "'");
            try {
                if (t.contains(".") || t.contains("e") || t.contains("E")) return Double.parseDouble(t);
                return Long.parseLong(t);
            } catch (NumberFormatException e) { throw error("bad number"); }
        }
    }
}
