package app.soundlab.audiotrack;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Random;

public final class SegmentEntity {

    private final String title;
    private final String engineer;
    private final LocalDateTime capturedAt;
    private final String recorderFormat;
    private final int sampleRate;
    private final byte[] rawSamples;

    private SegmentEntity(
            String title,
            String engineer,
            String recorderFormat,
            int sampleRate,
            byte[] rawSamples
    ) {
        this.title = title;
        this.engineer = engineer;
        this.recorderFormat = recorderFormat;
        this.sampleRate = sampleRate;
        this.rawSamples = rawSamples;
        this.capturedAt = LocalDateTime.now();
    }

    public static SegmentEntity microphoneCapture(String title, String engineer, int seconds) {
        int sampleRate = 44100;
        int length = Math.max(1, seconds) * sampleRate;
        byte[] samples = new byte[length];
        Random random = new Random(title.hashCode() ^ engineer.hashCode());
        for (int i = 0; i < length; i++) {
            double angle = (i % sampleRate) / (double) sampleRate * Math.PI * 2;
            double wave = Math.sin(angle * 3);
            samples[i] = (byte) (wave * 90 + random.nextInt(20) - 10);
        }
        return new SegmentEntity(title, engineer, "PCM_S16LE", sampleRate, samples);
    }

    // Legacy API -------------------------------------------------------------

    public String getRecorderFormat() {
        return recorderFormat;
    }

    public byte[] dumpSamples() {
        return Arrays.copyOf(rawSamples, rawSamples.length);
    }

    public int legacySampleRate() {
        return sampleRate;
    }

    public int rawSampleCount() {
        return rawSamples.length;
    }

    public String displayName() {
        return title;
    }

    public String supervisedBy() {
        return engineer;
    }

    public LocalDateTime capturedAt() {
        return capturedAt;
    }

    @Override
    public String toString() {
        return "SegmentEntity{" +
                "title='" + title + '\'' +
                ", engineer='" + engineer + '\'' +
                ", capturedAt=" + capturedAt +
                ", recorderFormat='" + recorderFormat + '\'' +
                ", sampleRate=" + sampleRate +
                ", samples=" + rawSamples.length +
                '}';
    }
}

