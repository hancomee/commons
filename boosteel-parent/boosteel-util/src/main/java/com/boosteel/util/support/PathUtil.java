package com.boosteel.util.support;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;

public class PathUtil {


    public static final void stream(String root, DirAccept accept) throws Exception {
        stream(root, accept, true);
    }
    public static final void stream(String root, DirAccept accept, boolean subs) throws Exception {
        stream(Paths.get(root), accept, subs);
    }

    public static final void stream(Path root, DirAccept accept, boolean subs) throws Exception {
        stream(root, accept, subs, 0);
    }

    private static final void stream(Path root, DirAccept accept, boolean subs, int depth) throws Exception {

        Set<Path> dirs = new TreeSet<>();
        FileAccept fileAccept = accept.accept(root, depth);

        if (fileAccept != null) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
                int pos = 0;
                for (Path path : stream) {
                    if (Files.isDirectory(path)) {
                        if (subs && !Files.isSymbolicLink(path))
                            dirs.add(path);
                    } else {
                        fileAccept.accept(path, Files.probeContentType(path), pos++);
                    }
                }
            }
        }
        if (subs && !dirs.isEmpty()) {
            for (Path path : dirs) stream(path, accept, true, depth + 1);
        }
    }


    public static final Path createFile(String path) throws IOException {
        return createFile(Paths.get(path));
    }
    public static final Path createFile(Path path) throws IOException {
        Path dir = path.getParent();
        if(!Files.exists(dir))
            Files.createDirectories(dir);
        return Files.createFile(path);
    }


    public static final Set<Path> list(Path path, boolean sub) throws Exception {
        Set<Path> list = new TreeSet<>();
        list(path, sub, (dir, files) -> list.addAll(files));
        return list;
    }

    public static final void list(Path path, boolean sub, TOURS handler) throws Exception {
        Set<Path> dir = new TreeSet<>(),
                files = new TreeSet<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path f : stream) {
                if (Files.isDirectory(f)) {
                    if (sub)
                        dir.add(f);
                }
                else {
                    files.add(f);
                }

            }
        }

        handler.accept(path, files);
        for (Path f : dir)
            list(f, sub, handler);

    }

    public static final void list(Path path, boolean sub, TOUR handler) throws Exception {

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path f : stream) {
                if (Files.isDirectory(f)) {
                    if (sub)
                        list(f, sub, handler);
                }
                else {
                    handler.accept(f);
                }
            }
        }
    }

    /* ****************************** ▼ interface ▼ ****************************** */

    // null 반환시 종료
    public interface DirAccept {
        FileAccept accept(Path path, int depth) throws Exception;
    }

    public interface FileAccept {
        void accept(Path file, String contentType, int index) throws Exception;
    }

    public interface TOURS {
        void accept(Path dir, Set<Path> files) throws Exception;
    }

    public interface TOUR {
        void accept(Path f) throws Exception;
    }


}
