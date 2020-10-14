package com.bioforceanalytics.dashboard;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import com.github.kokorin.jaffree.ffprobe.Stream;

/**
 * Handles converting various file formats to MP4 for use with JavaFX Media.
 * <hr>
 * TODO -- this process is necessary due to a limitation with the JavaFX API;
 * this could be solved by switching to a 3rd party video player such as
 * <code>vlcj-javafx</code>, a JavaFX-compatible component that wraps VLC Media
 * Player's functionality.
 * 
 * However, the base requirement for <code>vlcj-javafx</code> is JavaFX 14 and
 * JDK 11, which the Dashboard's codebase does not support (on JDK 8 currently).
 * 
 * This migration could be done later down the line, but would require
 * substantial rework of various sections of the codebase, specifically all
 * video interactions in the SINC+DAG rework of SINC Technology.
 */
public class MediaConverter {

    public static void main(String[] args) {
       System.out.println(MediaConverter.getCodec("C:\\Users\\shrey\\Downloads\\IMG_0690.mov"));
       System.out.println(MediaConverter.getCodec("C:\\Users\\shrey\\Downloads\\IMG_0690.mp4"));
       System.out.println(MediaConverter.getCodec("C:\\Users\\shrey\\Downloads\\jellyfish-3-mbps-hd-hevc.mp4"));
       System.out.println(MediaConverter.getCodec("C:\\Users\\shrey\\Downloads\\IMG_7452.mp4"));
    }

    /**
     * Converts a given file to an .mp4 and saves the converted version alongside the original.
     * @param videoFilePath the file path of the video to convert
     * @return the file path of the converted video
     */
    public static String convertToMP4(String videoFilePath) throws RuntimeException {

        // fetch location of FFmpeg binary and input file
        FfmpegSystemWrapper wrapper = new FfmpegSystemWrapper();
        Path BIN = Paths.get(wrapper.getBinRoot());
        
        // define input and output files for FFmpeg
        Path INPUT = Paths.get(videoFilePath);
        Path OUTPUT = Paths.get(convertFileExt(videoFilePath));

        // initialize FFmpeg command chain
        FFmpeg cmd = FFmpeg.atPath(BIN);

        // initialize output object
        UrlOutput FINAL_OUT = UrlOutput.toPath(OUTPUT);

        // if video stream is h264, simply copy codec to new file
        if (getCodec(videoFilePath).equals("h264")) {
            FINAL_OUT.copyAllCodecs();
        }

        cmd
            // set input to "videoFilePath"
            .addInput(UrlInput.fromPath(INPUT))
            // set output to "videoFilePath" with .mp4 extension
            .addOutput(FINAL_OUT)
            .execute();

        // return file path of converted video for UI purposes
        return OUTPUT.toString();
    }

    /**
     * Converts a given file path's extension to ".mp4".
     * @param filePath the file path to convert
     * @return the converted file path
     */
    public static String convertFileExt(String filePath) {

        // split file extension from name
        // (will also split at all periods in file name, this is accounted for)
        String[] arr = filePath.split("\\.");

        // remove the last entry (file extension) from array
        arr = Arrays.copyOf(arr, arr.length-1);

        String newFilePath = "";

        // recreate file path (add back any periods)
        for (String s : arr) {
            newFilePath += s + ".";
        }

        // if file already exists, add "converted" to avoid overwriting
        if (new File(newFilePath + "mp4").exists()) {
            newFilePath += "converted.";
        }

        // append new file extension
        // (period is already there from the` for` loop)
        newFilePath += "mp4";

        return newFilePath;

    }

    /**
     * Retrieves the video codec for a given file.
     * @param filePath the path to the video
     * @return the codec of the video
     */
    public static String getCodec(String filePath) {

        // fetch location of FFprobe binary and input file
        FfmpegSystemWrapper wrapper = new FfmpegSystemWrapper();
        Path BIN = Paths.get(wrapper.getBinRoot());

        // define input and output files for FFprobe
        Path INPUT = Paths.get(filePath);

        // initialize FFprobe command chain        
        FFprobeResult result = FFprobe.atPath(BIN)
            // show info about each media stream
            .setShowStreams(true)
            // set input to "filePath"
            .setInput(INPUT)
            .execute();
        
        String codec = "";

        // find the codec for the video stream
        for (Stream stream : result.getStreams()) {
            if (stream.getCodecType() == StreamType.VIDEO) {
                codec = stream.getCodecName();
            }
        }

        return codec;

    }

}
