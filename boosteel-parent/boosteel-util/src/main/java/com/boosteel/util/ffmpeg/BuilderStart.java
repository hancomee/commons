package com.boosteel.util.ffmpeg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public interface BuilderStart {
    void accept(String log, int idx) throws Exception;

    static List<String> run(String[] exp) throws Exception {
        return run(exp, false);
    }
    static List<String> run(String[] exp, boolean input) throws Exception {
        List<String> list = new ArrayList<>();
        run(exp, (line, i) -> list.add(line), input);
        return list;
    }

    static void run(String[] exp, BuilderStart log) throws Exception {
        run(exp, log, false);
    }

    static void run(String[] exp, BuilderStart log, boolean input) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(exp);
        Process p = builder.start();
        String line = null;
        int i = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                input ? p.getInputStream() : p.getErrorStream(),
                "utf-8"))) {
            while ((line = br.readLine()) != null) {
                log.accept(line, i++);
            }
        }
    }
}
