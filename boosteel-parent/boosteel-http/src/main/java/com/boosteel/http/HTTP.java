package com.boosteel.http;

import com.boosteel.util.support.IO;
import com.boosteel.util.support.Patterns;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public class HTTP {

    private static  Map<String, String> DEFAULT_HEADER;

    static {
        DEFAULT_HEADER = new HashMap<>();
        DEFAULT_HEADER.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36");
    }

    public static final Map<String, String> getDefaultHeader() {
        return new HashMap<>(DEFAULT_HEADER);
    }
    public static final void setDefaultHeader(String lines) {
        DEFAULT_HEADER = createHeader(lines);
    }

    public static final void setDefaultHeader(Map<String, String> map) {
        DEFAULT_HEADER = map;
    }

    public static final Map<String, String> readHeader(URL url) throws Exception {
        return readHeader(Paths.get(url.toURI()));
    }

    public static final Map<String, String> readHeader(Path path) throws Exception {
        return createHeader(Files.readAllLines(path));
    }
    public static final Map<String, String> createHeader(String lines) {
        return createHeader(Arrays.asList(lines.split("\n")));
    }
    public static final Map<String, String> createHeader(Iterable<String> lines) {
        Map<String, String> header = new HashMap<>();
        int pos = -1;
        for (String line : lines) {
            pos = line.indexOf(":");
            if (pos != -1) {
                header.put(line.substring(0, pos).trim(), line.substring(pos+1).trim());
            }
        }
        return header;
    }

    public static final HttpURLConnection head(String url) throws IOException {
        return head(url, DEFAULT_HEADER);
    }
    public static final HttpURLConnection head(String url, Map<String, String> header) throws IOException {
        HttpURLConnection con = $get(url, header);
        con.setRequestMethod("HEAD");
        return con;
    }


    public static final HttpURLConnection $get(String url, Map<String, String> header) throws IOException {
        return $get(new URL(url), header);
    }
    public static final HttpURLConnection $get(String url) throws IOException {
        return $get(new URL(url));
    }
    public static final HttpURLConnection $get(URL url) throws IOException {
        return $get(url, DEFAULT_HEADER);
    }
    public static final HttpURLConnection $get(URL url, Map<String, String> header) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        for(Map.Entry<String, String> entry : header.entrySet())
            con.setRequestProperty(entry.getKey(), entry.getValue());
        return con;
    }

    public static final HttpURLConnection $post(String url, Map<String, String> header) throws IOException {
        return $post(new URL(url), header);
    }
    public static final HttpURLConnection $post(URL url) throws IOException {
        return $post(url, DEFAULT_HEADER);
    }
    public static final HttpURLConnection $post(URL url, Map<String, String> header) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);

        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        for(Map.Entry<String, String> entry : header.entrySet())
            con.setRequestProperty(entry.getKey(), entry.getValue());

        con.disconnect();
        return con;
    }

    public static final HttpURLConnection $post(String url, String[] content) throws IOException {
        return $post(url, Arrays.asList(content));
    }
    public static final HttpURLConnection $post(String url, List<String> content) throws IOException {
        return $post(new URL(url), DEFAULT_HEADER, content);
    }
    public static final HttpURLConnection $post(URL url, Map<String, String> header, Iterable<String> content) throws IOException {
        HttpURLConnection con = $post(url, header);

        try(PrintWriter pw = new PrintWriter(con.getOutputStream())) {
            for (String line : content) pw.println(line);
            pw.flush();
        }
        con.disconnect();
        return con;
    }

    // 한 줄로 읽기
    public static String get(String url) throws Exception {
        return get(url, "utf-8");
    }
    public static String get(String url, String charset) throws Exception {
        return get(new URL(url), DEFAULT_HEADER, charset);
    }
    public static String get(String url, Map<String, String> header) throws Exception {
        return get(url, header, "utf-8");
    }
    public static String get(String url, Map<String, String> header, String charset) throws Exception {
        return get(new URL(url), header, charset);
    }
    public static String get(URL url, Map<String, String> header, String charset) throws Exception {
        return IO.read($get(url, header).getInputStream(), charset);
    }

    // 한 줄로 읽기
    public static String post(String url) throws Exception {
        return post(url, Collections.EMPTY_LIST);
    }
    public static String post(String url, String charset) throws Exception {
        return post(new URL(url), DEFAULT_HEADER, Collections.EMPTY_LIST, charset);
    }
    public static String post(String url, String[] content) throws Exception {
        return post(url, Arrays.asList(content));
    }
    public static String post(String url, List<String> content) throws Exception {
        return post(url, content, "utf-8");
    }
    public static String post(String url, String contents, String charset) throws Exception {
        return post(url,Arrays.asList(contents.split("\n")), charset);
    }
    public static String post(String url, String[] content, String charset) throws Exception {
        return post(url, Arrays.asList(content), charset);
    }
    public static String post(String url, List<String> content, String charset) throws Exception {
        return post(new URL(url), DEFAULT_HEADER, content, charset);
    }
    public static String post(String url, Map<String, String> header) throws Exception {
        return post(url, header, "utf-8");
    }
    public static String post(String url, Map<String, String> header, String charset) throws Exception {
        return post(new URL(url), header, Collections.EMPTY_LIST, charset);
    }
    public static String post(String url, Map<String, String> header, String[] content) throws Exception {
        return post(url, header, Arrays.asList(content));
    }
    public static String post(String url, Map<String, String> header, List<String> content) throws Exception {
        return post(new URL(url), header, content, "utf-8");
    }
    public static String post(String url, Map<String, String> header, String content, String charset) throws Exception {
        return IO.read($post(new URL(url), header, Arrays.asList(content.split("\n"))).getInputStream(), charset);
    }
    public static String post(String url, Map<String, String> header, List<String> content, String charset) throws Exception {
        return IO.read($post(new URL(url), header, content).getInputStream(), charset);
    }
    public static String post(URL url, Map<String, String> header, List<String> content, String charset) throws Exception {
        return IO.read($post(url, header, content).getInputStream(), charset);
    }

}
