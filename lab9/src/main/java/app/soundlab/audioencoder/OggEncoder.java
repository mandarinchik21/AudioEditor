package app.soundlab.audioencoder;

import app.soundlab.audiotrack.SegmentEntity;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;
import java.io.IOException;

public class OggEncoder implements AudioEncoder<File> {
    private static volatile OggEncoder sharedReference;
    private final EncodingAttributes encodingAttributes;
    private final Encoder encoder;

    private OggEncoder() {
        encodingAttributes = new EncodingAttributes();
        encodingAttributes.setOutputFormat("ogg");
        encoder = new Encoder();
    }

    public static OggEncoder get() {
        OggEncoder current = sharedReference;
        if (current == null) {
            synchronized (OggEncoder.class) {
                current = sharedReference;
                if (current == null) {
                    current = new OggEncoder();
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
            File outputFile = new File(inputFile.getParent(), inputFile.getName() + " (converted to ogg).ogg");
            if (!outputFile.createNewFile()) {
                throw new IOException("Such a file already exists: " + outputFile.getAbsolutePath());
            }
            AudioAttributes audioAttrs = new AudioAttributes();
            audioAttrs.setCodec("libvorbis");
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

