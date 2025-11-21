package app.soundlab.audiotrack;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public abstract class SegmentEntity {
    private static final int SAMPLE_RATE = 44100;

    protected final String fileName;
    protected final List<Float> audioData = new ArrayList<>();
    protected final List<Float> clipboard = new ArrayList<>();
    protected int segmentStart = -1;
    protected int segmentEnd = -1;

    protected SegmentEntity(String fileName) {
        this.fileName = fileName;
    }

    public abstract String getFormat();

    public abstract File getFileLink();

    public void select(int start, int end) {
        ensureAudioLoaded();
        validateSegmentBounds(start, end);
        this.segmentStart = start;
        this.segmentEnd = end;
    }

    public void copy() {
        ensureAudioLoaded();
        validateActiveSegment();
        clipboard.clear();
        clipboard.addAll(audioData.subList(segmentStart, segmentEnd));
    }

    public void cut() {
        ensureAudioLoaded();
        validateActiveSegment();
        clipboard.clear();
        clipboard.addAll(audioData.subList(segmentStart, segmentEnd));
        audioData.subList(segmentStart, segmentEnd).clear();
    }

    public void paste(int positionInSeconds, int sampleRate) {
        if (clipboard.isEmpty()) {
            throw new IllegalStateException("Clipboard is empty. Copy or cut a segment first.");
        }
        int position = positionInSeconds * sampleRate;
        if (position < 0 || position > audioData.size()) {
            throw new IllegalArgumentException("Invalid paste position.");
        }
        audioData.addAll(position, clipboard);
    }

    public void morph(double factor) {
        ensureAudioLoaded();
        validateActiveSegment();
        for (int i = segmentStart; i < segmentEnd; i++) {
            float newValue = audioData.get(i) * (float) factor;
            newValue = Math.max(-32768, Math.min(32767, newValue));
            audioData.set(i, newValue);
        }
    }

    public int getSize() {
        return audioData.size();
    }

    public int getDurationSeconds() {
        return (audioData.size() / SAMPLE_RATE) / 2;
    }

    public int getSamplesBySeconds(int seconds) {
        return seconds * SAMPLE_RATE * 2;
    }

    public int getSecondsBySamples(int samples) {
        return (samples / SAMPLE_RATE) / 2;
    }

    public void saveAs(String outputFilePath) {
        try {
            File outputFile = new File(outputFilePath);
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 2, true, false);
            byte[] byteArray = new byte[audioData.size() * 2];
            for (int i = 0; i < audioData.size(); i++) {
                int sample = Math.round(audioData.get(i));
                byteArray[2 * i] = (byte) (sample & 0xFF);
                byteArray[2 * i + 1] = (byte) ((sample >> 8) & 0xFF);
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
            AudioInputStream audioStream = new AudioInputStream(bais, format, audioData.size());
            AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, outputFile);
            audioStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Error saving audio file: " + e.getMessage());
        }
    }

    private void ensureAudioLoaded() {
        if (audioData.isEmpty()) {
            throw new IllegalStateException("Audio data is empty. Load a valid file first.");
        }
    }

    private void validateSegmentBounds(int start, int end) {
        if (start < 0 || end > audioData.size() || start >= end) {
            throw new IllegalArgumentException(
                    String.format("Invalid segment bounds. Start: %d, End: %d, Max: %d.", start, end, audioData.size())
            );
        }
    }

    private void validateActiveSegment() {
        if (segmentStart < 0 || segmentEnd > audioData.size() || segmentStart >= segmentEnd) {
            throw new IllegalArgumentException("Invalid segment bounds.");
        }
    }
}

