package com.boosteel.util.ffmpeg;


import com.boosteel.util.support.Patterns;

import java.nio.file.Path;
import java.util.regex.Pattern;

public class FFMEPG_INFO {

    private static final Pattern
            r_info = Pattern.compile("(?i)Duration:.*?([\\d:]+).*?bitrate:.*?(\\d+).*?(\\d{2,}x\\d{2,})");

    private Path file;

    private String filename;
    private String filetype;

    private String duration;
    private int second;

    private int bitrate;
    private int width;
    private int height;

    public static final FFMEPG_INFO create(Path file) throws Exception {
        String lines = String.join(" ", FFMPEG.info(file.toString()));
        String[] values = Patterns.exec(r_info, lines),
                size;
        if (values == null) return null;
        size = values[3].split("x");

        return new FFMEPG_INFO(file).setDuration(values[1])
                .setBitrate(Integer.parseInt(values[2]))
                .setWidth(Integer.parseInt(size[0]))
                .setHeight(Integer.parseInt(size[1]));
    }

    private FFMEPG_INFO(Path file) {
        String[] names = Patterns.exec("^(.*)\\.([^\\.]+)$", file.getFileName().toString());
        this.file = file;
        this.filename = names[1];
        this.filetype = names[2];
    }

    public Path getFile() {
        return file;
    }

    public String getFilename() {
        return filename;
    }

    public String getFiletype() {
        return filetype;
    }

    public String getDuration() {
        return duration;
    }

    public int getSecond() {
        return second;
    }

    private FFMEPG_INFO setDuration(String duration) {
        this.duration = duration;
        this.second = FFMPEG.stringToInt(duration);
        return this;
    }

    public int getBitrate() {
        return bitrate;
    }

    private FFMEPG_INFO setBitrate(int bitrate) {
        this.bitrate = bitrate;
        return this;
    }

    public int getWidth() {
        return width;
    }

    private FFMEPG_INFO setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    private FFMEPG_INFO setHeight(int height) {
        this.height = height;
        return this;
    }

    @Override
    public String toString() {
        return String.join(", ", new String[]{
                "[" + width + "x" + height + "]",
                "duration=" + duration,
                "bitrate=" + bitrate
        });
    }
}
