package com.boosteel.nativedb.core.support;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepositoryConfig {

    private Map<String, String> sqlMap = new HashMap<>();


    public String getSQL(String s) {
        return sqlMap.get(s);
    }

    /*
     *   문서를 읽어들여서 sqlMap을 만든다.
     *
     *   #select.work           <-- map의 key가 된다.  #이 여러개 나올경우 맨 마지막 #을 키값으로 쓴다.
     *   SELECT ~~
     *   ;                      <-- ;문자가 나오면 하나의 SQL로 인식한다.
     */
    // getClassLoader().getResource() 용
    public RepositoryConfig addSQL(URL url) {
        try {
            return addSQL(url.toURI());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RepositoryConfig addSQL(URI uri) {
        return addSQL(Paths.get(uri));
    }

    public RepositoryConfig addSQL(Path file) {
        try {
            return addSQL(Files.readAllLines(file));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RepositoryConfig addSQL(InputStream is) {
        return addSQL(is, "utf-8");
    }

    public RepositoryConfig addSQL(InputStream is, String charset) {
        try (InputStream os = is;
             InputStreamReader reader = new InputStreamReader(os, charset);
             BufferedReader br = new BufferedReader(reader)) {
            List<String> lines = new ArrayList<>();
            String line = null;
            while ((line = br.readLine()) != null) lines.add(line);
            return addSQL(lines);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RepositoryConfig addSQL(List<String> list) {
        int i = 0, size = list.size(), index = -1;
        String key, line = null;
        List<String> lines = null;


        for (; i < size; i++) {
            line = list.get(i);

            if (line.startsWith("#")) {
                key = line.substring(1).trim();
                lines = new ArrayList<>();
                i++;


                for (; i < size; i++) {
                    line = list.get(i);
                    index = line.indexOf(";");


                    // 끝나는 부호가 있으면 담는다.
                    if (index != -1) {
                        lines.add(line.substring(0, index));
                        sqlMap.put(key, String.join("\n", lines));
                        break;
                    }
                    // #가 여러줄일때는 넘긴다.
                    else if (line.startsWith("#")) {
                        i--;
                        break;
                    } else lines.add(line);
                }
            }
        }
        return this;
    }

}
