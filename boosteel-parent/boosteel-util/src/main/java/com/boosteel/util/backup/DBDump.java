package com.boosteel.util.backup;

import com.boosteel.util.support.DateUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBDump {


    public static String[] command(String root, String dbname) {
        return new String[]{
                "\"" + root + "/mysqldump\"",
                "--routines", "--trigger",
                "-uroot", "-pko9984", dbname,
        };
    }

    public static String[] command(String root, String dbname, String ip) {
        return new String[]{
                "\"" + root + "/mysqldump\"",
                "--routines", "--trigger",
                "-u", "root", "-pko9984", "-h", ip, "-P", "3306", dbname,
        };
    }

    public static void mysqldump(String root, String dbname, AcceptLog log) throws Exception {
        String[] command = command(root, dbname);
        $createReader(command, log);
    }

    public static Path mysqldump(String root, String dbname, Path file) throws Exception {
        String[] command = command(root, dbname);
        List<String> lines = new ArrayList<>();
        $createReader(command, (l) -> {
            lines.clear();
            lines.add(l);
            Files.write(file, lines, StandardOpenOption.APPEND);
        });

        return file;
    }


    public static Path mysqldump(String root, String dbname, Path dir, String prefix) throws Exception {
        prefix = prefix + "-" + dbname + "-" +
                DateUtil.toString(new Date(), "yyyy-MM-dd_HHmmss") + ".sql";
        return mysqldump(root, dbname, Files.createFile(dir.resolve(prefix)));
    }

    public static void $createReader(String[] exp, AcceptLog log) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(exp);
        Process p = builder.start();
        String line = null;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "utf-8"))) {
            while ((line = br.readLine()) != null) {
                log.accept(line);
            }
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream(), "utf-8"))) {
            while ((line = br.readLine()) != null) {
                log.accept(line);
            }
        }
    }

    interface AcceptLog {
        void accept(String line) throws Exception;
    }

    private void out(Object obj) {
        System.out.println(obj);
    }
}
