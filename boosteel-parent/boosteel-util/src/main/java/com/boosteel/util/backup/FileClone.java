package com.boosteel.util.backup;

import com.boosteel.util.support.PathUtil;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class FileClone {


    // *********************** ▼ 파일정보 가지고 오기 ▼ *********************** //
    /*
     *  파일 정보 가지고 오기
     */
    public static final Map<String, Long> getMap(String sourceDir) throws Exception {
        return getMap(Paths.get(sourceDir));
    }

    public static final Map<String, Long> getMap(Path sourceDir) throws Exception {
        Map<String, Long> map = new TreeMap<>();
        getMap(sourceDir, map);
        return map;
    }


    public static final Map<String, Long> getMap(FTPManager client) throws Exception {
        Map<String, Long> result = new HashMap<>();
        client.run(ftp -> {
            ftpTour(ftp, "", (path, name, file) -> {
                result.put(concat(path, name), file.getSize());
            });
        });

        return result;
    }

    private static final void getMap(Path sourcePath, Map<String, Long> map) throws Exception {
        int pos = sourcePath.getNameCount();
        PathUtil.list(sourcePath, true, (dir, set) -> {
            for (Path f : set)
                map.put(f
                        .subpath(pos, f.getNameCount())
                        .toString()
                        .replaceAll("\\\\", "/"), Files.size(f));
        });
    }


    public static final List<FTPFile> deleteList(Path path, FTPManager client) throws Exception {

        List<FTPFile> list = new ArrayList<>();
        Map<String, Long> map = getMap(path);

        client.run(ftp -> {
            ftpTour(ftp, "", (p, name, file) -> {
                if(!map.containsKey(concat(p, name)))
                    list.add(file);
            });
        });

        return list;
    }

    private static final void ftpTour(FTPClient client, String path, RemoteHandler handler) throws Exception {

        List<FTPFile> list = new ArrayList<>();
        for (FTPFile file : client.listFiles()) {
            if (file.isDirectory()) {
                list.add(file);
            } else {
                handler.accept(path, file.getName(), file);
            }
        }

        for (FTPFile dir : list) {
            client.changeWorkingDirectory(dir.getName());
            ftpTour(client, concat(path, dir.getName()), handler);
            client.changeWorkingDirectory("../");
        }
    }

    private static final String concat(String path, String filename) {
        return (path == null || path.isEmpty() ? "" : path + "/") + filename;
    }
    // *********************** ▲ 파일정보 가지고 오기 ▲ *********************** //


    private static final void write(Map<String, Long> map, Path file) throws IOException {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Long> entry : map.entrySet())
            list.add(entry.getKey() + "\t" + entry.getValue());

        Files.write(file, list, StandardOpenOption.TRUNCATE_EXISTING);
    }


    public static final Path check(String targetDir) throws Exception {
        return check(Paths.get(targetDir));
    }

    public static final Path check(Path targetPath) throws Exception {
        Path files = targetPath.resolve(".files.txt"),
                change = targetPath.resolve(".change.txt");

        Map<String, Long> filesMap = new HashMap<>(),
                changeMap = new HashMap<>();

        int pos = targetPath.getNameCount();

        List<String> lines = Files.readAllLines(files);
        String[] values;
        for (String line : lines) {
            if (line.contains("\t")) {
                values = line.split("\t");
                filesMap.put(values[0], Long.valueOf(values[1]));
            }
        }


        Files.createFile(change);
        PathUtil.list(targetPath, true, (f) -> {
            String path = f
                    .subpath(pos, f.getNameCount())
                    .toString()
                    .replaceAll("\\\\", "/");
            Long size = filesMap.get(path);
            if (size == null) changeMap.put(path, Files.size(f));
            else {
                long s = Files.size(f);
                if (!size.equals(s))
                    changeMap.put(path, s);
            }
        });

        write(changeMap, change);
        return change;

    }

    public static final List<String> check(Path sourcePath, Path targetPath) throws Exception {
        return check(getMap(sourcePath), getMap(targetPath));
    }

    public static final List<String> check(Path targetPath, FTPManager client) throws Exception {
        Map<String, Long> remote = getMap(client),
                source = getMap(targetPath);
        return check(source, remote);
    }

    public static final List<String> check(Map<String, Long> source, Map<String, Long> target) throws Exception {
        List<String> result = new ArrayList<>();
        Long rv, sv;
        for (Map.Entry<String, Long> entry : source.entrySet()) {
            sv = entry.getValue();
            rv = target.get(entry.getKey());
            if (rv == null || !sv.equals(rv))
                result.add(entry.getKey());
        }
        return result;
    }



    public static final void copy(String path, Path source, Path target) throws IOException {
        source = source.resolve(source);
        target = target.resolve(path);

        Path rDir = target.getParent();
        if (!Files.exists(rDir))
            Files.createDirectories(rDir);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
    }

    public static final void copy(String path, Path source, FTPManager client) throws Exception {
        source = source.resolve(path);
        FTPClient ftp = client.getClient();
        if (path.contains("/")) {
            ftp.mkd(path.substring(0, path.lastIndexOf("/")));
        }
        ftp.storeFile(path, Files.newInputStream(source));
    }

    public static final Map<String, Integer> local(Path sourceDir, Path... targetDirs) throws Exception {
        return local(sourceDir, Arrays.asList(targetDirs));
    }

    public static final Map<String, Integer> local(Path sourceDir, List<Path> targetDirs) throws Exception {
        Map<String, Integer> map = new HashMap<>();

        // 폴더 생성
        for (Path f : targetDirs)
            if (!Files.exists(f))
                Files.createDirectories(f);

        // 카운더 집계
        int length = targetDirs.size();
        int[] count = new int[length];
        while (length-- > 0)
            count[length] = 0;

        // 이동
        local((file, index, isNew) -> {
            count[index]++;
        }, sourceDir, targetDirs);

        // 결과 맵
        length = targetDirs.size();
        while (length-- > 0)
            map.put(targetDirs.get(length).toString(), count[length]);

        return map;
    }


    public static final void local(CopyHandler handler, Path sourceDir, List<Path> targetDirs) throws Exception {

        // if(sourceDir.getFileName().toString().startsWith(".")) return;

        boolean exists;
        Path targetFile;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
            for (Path sourceFile : stream) {

                if (Files.isDirectory(sourceFile)) {
                    String dirName = sourceFile.getFileName().toString();
                    List<Path> dirs = new ArrayList<>(targetDirs.size());
                    int i = 0;
                    for (Path d : targetDirs)
                        dirs.add(i++, dir(dirName, d));
                    local(handler, sourceFile, dirs);
                } else {

                    long size = Files.size(sourceFile);
                    int i = 0;

                    for (Path d : targetDirs) {

                        targetFile = d.resolve(sourceFile.getFileName());

                        /*
                         *
                         *  lastModifiedTime의 경우 완전 복제가 안되는 듯 싶다.
                         *  초 단위가 반올림되서 적용되는 것 같다.
                         *  따라서 10초를 기준으로 둔다.
                         *
                         *  다른 파일이라고 판단하는 기준
                         *  ① 용량이 다를때
                         *  ② lastModifiedTime이 10초 차이가 날때
                         *
                         *  || Math.abs(modifiedTime - modified(targetFile)) > 10000
                         */
                        if (!(exists = Files.exists(targetFile)) ||
                                size != Files.size(targetFile)) {
                            Files.copy(sourceFile, targetFile,
                                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                            handler.accept(targetFile, i, !exists);
                        }
                        i++;
                    }
                }
            }
        }
    }

    private static final long modified(Path path) throws IOException {
        return Files.getLastModifiedTime(path).toMillis();
    }

    private static final Path dir(String dir, Path target) throws IOException {
        target = target.resolve(dir);
        if (!Files.exists(target)) {
            Files.createDirectories(target);
            out("--> mkd : " + target);
        }

        return target;
    }


    public interface RemoteHandler {
        void accept(String path, String filename, FTPFile file) throws Exception;
    }

    public interface CopyHandler {
        void accept(Path file, int index, boolean isNew) throws Exception;
    }

    private static final void out(Object obj) {
        System.out.println(obj);
    }
}
