package app.soundlab.audioencoder;

import app.soundlab.audiotrack.SegmentEntity;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;
import java.io.IOException;

public class Mp3Encoder implements AudioEncoder<File> {
    private static volatile Mp3Encoder sharedReference;
    private final EncodingAttributes encodingAttributes;
    private final Encoder encoder;

    private Mp3Encoder() {
        encodingAttributes = new EncodingAttributes();
        encodingAttributes.setOutputFormat("mp3");
        encoder = new Encoder();
    }

    public static Mp3Encoder get() {
        Mp3Encoder current = sharedReference;
        if (current == null) {
            synchronized (Mp3Encoder.class) {
                current = sharedReference;
                if (current == null) {
                    current = new Mp3Encoder();
                    sharedReference = current;
                }
            }
        }
        return current;
    }

    @Override
    public File encode(SegmentEntity audio) {
        try {
            File inputFile = audio.getFileLink();
            File outputFile = new File(inputFile.getParent(), inputFile.getName() + " (converted to mp3).mp3");
            if (!outputFile.createNewFile()) {
                throw new IOException("Such a file already exists: " + outputFile.getAbsolutePath());
            }
            AudioAttributes audioAttrs = new AudioAttributes();
            audioAttrs.setCodec("libmp3lame");
            audioAttrs.setBitRate(128000);
            audioAttrs.setChannels(2);
            audioAttrs.setSamplingRate(44100);
            encodingAttributes.setAudioAttributes(audioAttrs);
            encoder.encode(new MultimediaObject(inputFile), outputFile, encodingAttributes);
            return outputFile;
        } catch (IOException | EncoderException e) {
            throw new RuntimeException("Converter error: " + e.getMessage(), e);
        }
    }
}

