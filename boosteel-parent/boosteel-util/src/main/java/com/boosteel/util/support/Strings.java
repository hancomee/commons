package com.boosteel.util.support;

import javax.swing.text.html.HTML;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*

유니코드를 한글로 변환
new String (uni.getBytes("8859_1"),"KSC5601");

한글을 유니코드로 변환
new String (uni.getBytes("KSC5601"),"8859_1");

 */
public class Strings {

    public interface REPLACE_HANDLER {
        String replace(String[] groups, int index) throws Exception;
    }

    public static final String replace(String target, String pattern, REPLACE_HANDLER handler) {
        return replace(target, Pattern.compile(pattern), handler);
    }

    public static final String replace(String target, Pattern pattern, REPLACE_HANDLER handler) {
        Matcher m = pattern.matcher(target);
        StringBuffer buf = new StringBuffer();
        int count = 0, len;
        String[] values;

        try {
            while (m.find()) {
                len = m.groupCount() + 1;
                values = new String[len];
                for (int i = 0; i < len; i++) {
                    values[i] = m.group(i);
                }
                m.appendReplacement(buf, handler.replace(values, count++));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        m.appendTail(buf);
        return buf.toString();
    }

    private static final Pattern r_unicode = Pattern.compile("\\\\u(\\w{4})");

    public static String unicodeDecoder(String target) {
        Matcher m = r_unicode.matcher(target);
        StringBuffer buf = new StringBuffer();

        while (m.find()) {
            m.appendReplacement(buf, String.valueOf((char) Integer.parseInt(m.group(1), 16)));
        }

        m.appendTail(buf);
        return buf.toString();
    }

    private static Map<String, String> HTML_ESCAPE = new HashMap<>();
    private static Pattern R_HTML_ESCAPE = Pattern.compile("&[#\\w]+;");

    static {
        HTML_ESCAPE.put("&quot;", "\"");
        HTML_ESCAPE.put("&amp;", "&");
        HTML_ESCAPE.put("&lt;", "<");
        HTML_ESCAPE.put("&gt;", ">");
        HTML_ESCAPE.put("&nbsp;", " ");
        HTML_ESCAPE.put("&#034;", "\"");
        HTML_ESCAPE.put("&rsquo;", "'");
        HTML_ESCAPE.put("&lsquo;", "'");
        HTML_ESCAPE.put("&middot;", "·");
        HTML_ESCAPE.put("&ldquo;", "\"");
        HTML_ESCAPE.put("&rdquo;", "\"");
        HTML_ESCAPE.put("&hellip;", "...");
        HTML_ESCAPE.put("&#039;", "'");
    }

    public static final String unEscapeHTML(String target) {
        Matcher m = R_HTML_ESCAPE.matcher(target);
        StringBuffer buf = new StringBuffer();
        String r;
        while (m.find()) {
            r = HTML_ESCAPE.get(m.group());
            if (r != null)
                m.appendReplacement(buf, r);
        }

        m.appendTail(buf);
        return buf.toString();
    }


    public List<String> cutAll(String target, String start, String end, boolean contains) {
        List<String> result = new ArrayList<>();
        int index = 0;
        int[] pos;
        while((pos = find(target, start, end, index, contains)) != null) {
            result.add(target.substring(pos[0], pos[1]));
            index = pos[1];
        }
        return result;

    }

    public static final String cut(String target, String start, String end) {
        return cut(target, start, end, true);
    }

    public static final String cut(String target, String start, String end, boolean contains) {
        int[] pos = find(target, start, end, 0, contains);
        if (pos == null) return target;
        return target.substring(pos[0], pos[1]);
    }

    public static final String cut(String target, String text, boolean contains) {
        if(target == null) return null;
        int pos = target.indexOf(text);
        if (pos == -1) return null;
        return target.substring(contains ? pos : pos + text.length(), target.length());
    }

    public static final int[] find(String target, String start, String end, int index, boolean contains) {
        int[] result = {target.indexOf(start, index), -1};
        int next;
        if (result[0] == -1) return null;

        next = result[0] + start.length();
        if (!contains) result[0] = next;

        result[1] = target.indexOf(end, next);
        if (result[1] == -1) return null;
        if (contains) result[1] += end.length();

        return result;
    }

    // 특정 앞문자열로 자르기
    public static final List<String> cutAll(String text, String split) {
        List<String> result = new ArrayList<>();
        int len = split.length(),
                i = -1, pos = text.indexOf(split);

        while (pos != -1) {
            i = pos;
            pos = text.indexOf(split, i + len);
            if (pos != -1) result.add(text.substring(i, pos));
            else if (i != -1) {
                result.add(text.substring(i));
            }
        }
        return result;
    }


    // 윈도우에서 못 쓰는 글짜 없애기
    public static final String eraseWindowStr(String target) {
        return target.replaceAll("\\\\|/|:|<|>|\\?|\\*|\"|\\|", "");
    }
}
