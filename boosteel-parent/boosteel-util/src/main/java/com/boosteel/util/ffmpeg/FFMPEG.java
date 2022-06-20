package com.boosteel.util.ffmpeg;


import com.boosteel.util.support.Patterns;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FFMPEG {

    public static final String FFMPEG_PATH = "ffmpeg";

    public static List<String> info(String source) throws Exception {
        return BuilderStart.run(new String[]{
                FFMPEG_PATH, "-i", source
        }, false);
    }


    /*
     *  해당 시간에 해당하는 썸네일 이미지 만들기
     *  영상파일명과 같은 제목의 jpg 파일
     */

    public static final void createThumbnails(String dir) throws Exception {
        createThumbnails(Paths.get(dir));
    }

    public static final void createThumbnails(Path dir) throws Exception {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path p : stream) {
                if (Files.isRegularFile(p)) {
                    String type = Files.probeContentType(p);
                    if (type != null && type.startsWith("video")) {
                        createThumbnail(p);
                    }
                }
            }
        }
    }

    public static final Path createThumbnail(String _source, String time) throws Exception {
        return createThumbnail(Paths.get(_source), time);
    }

    public static final Path createThumbnail(Path _source) throws Exception {
        String filename = _source.getFileName().toString();
        filename = filename.substring(0, filename.lastIndexOf("."));
        Path _target = _source.resolve(filename + ".jpg");
        if (Files.exists(_target)) return _target;

        int duration = stringToInt(FFMEPG_INFO.create(_source).getDuration());
        return createThumbnail(_source, _source.getParent(), intToString(duration / 2));
    }

    public static final Path createThumbnail(Path _source, String time) throws Exception {
        return createThumbnail(_source, _source.getParent(), time);
    }

    public static final Path createThumbnail(Path _source, Path _targetDir, String time) throws Exception {

        String filename = _source.getFileName().toString();
        int pos = filename.lastIndexOf(".");
        filename = filename.substring(0, pos);
        Path _target = _targetDir.resolve(filename + ".jpg");

        if (Files.exists(_target))
            return _target;

        String source = _source.toString(),
                target = _target.toString();

        String[] str = {
                FFMPEG_PATH,
                "-ss", "\"" + time + "\"",
                "-i", "\"" + source + "\"",
                "-y", "-vframes", "1",
                "-an", "\"" + target + "\""
        };

        out(_target);
        BuilderStart.run(str);
        return _target;
    }

    public static final String[] m3u8(String url, String output) {
        return new String[]{
                FFMPEG_PATH,
                "-i", "\"" + url + "\"", "-y",
                "-c", "copy",
                "-bsf:a", "aac_adtstoasc",
                "\"" + output + "\""
        };
    }


    private static Pattern r_mp4 = Pattern.compile("time=([^\\s\\.]+)");

    public static List<String> read(Path file) throws Exception {
        String
                filename = file.toString(),
                filetype;

        String[] command = new String[]{
                FFMPEG_PATH,
                "-i", "\"" + filename + "\""
        };

        List<String> lines = new ArrayList<>();
        BuilderStart.run(command, (line, idx) -> {
            lines.add(line);
        }, false);
        return lines;
    }


    public static void $toMP4(String file) throws Exception {
        $toMP4(Paths.get(file));
    }

    public static void $toMP4(Path file) throws Exception {
        FFMEPG_INFO info = FFMEPG_INFO.create(file);
        if (info != null) {
            $toMP4(file, info);
        }
    }

    public static void $toMP4(Path file, FFMEPG_INFO info) throws Exception {
        String
                duration = info.getDuration(),
                target,
                filename = target = file.toString(),
                filetype;
        int i = filename.lastIndexOf(".");
        filetype = filename.substring(i + 1, filename.length());
        filename = filename.substring(0, i);

        out("\"" + filename + "." + filetype + "\"");

        String[] command = new String[]{
                FFMPEG_PATH,
                "-i", "\"" + filename + "." + filetype + "\"",
                "\"" + filename + ".mp4\""
        };

        BuilderStart.run(command, (line, idx) -> {
            String[] d = Patterns.exec(r_mp4, line);
            if (d != null)
                out(d[1] + " / " + duration + " :: " + target);
        }, false);
    }


    // 02:29:41 ==>  8981
    public static final int stringToInt(String duration) {
        String[] values = duration.split(":");
        int i = Integer.parseInt(values[2]);
        i += 60 * Integer.parseInt(values[1]);
        i += 3600 * Integer.parseInt(values[0]);
        return i;
    }

    // 8981 ==> 02:29:41
    public static final String intToString(int num) {
        int hour = num / (3600),
                minute = (num - (hour * 3600)) / 60,
                second = num - (hour * 3600) - (minute * 60);

        return (hour < 10 ? "0" : "") + hour + ":" +
                (minute < 10 ? "0" : "") + minute + ":" +
                (second < 10 ? "0" : "") + second;
    }

    private static final void out(Object obj) {
        System.out.println(obj);
    }

}
