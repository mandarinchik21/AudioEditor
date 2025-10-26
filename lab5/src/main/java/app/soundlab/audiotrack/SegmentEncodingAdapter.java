package app.soundlab.audiotrack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class SegmentEncodingAdapter implements EncodingRequest {

    private final SegmentEntity legacySegment;
    private final String desiredCodec;

    public SegmentEncodingAdapter(SegmentEntity legacySegment, String desiredCodec) {
        this.legacySegment = legacySegment;
        this.desiredCodec = desiredCodec;
    }

    @Override
    public String targetCodec() {
        return desiredCodec;
    }

    @Override
    public InputStream dataStream() {
        byte[] legacySamples = legacySegment.dumpSamples();
        ByteArrayOutputStream normalized = new ByteArrayOutputStream(legacySamples.length * 2);
        for (byte sample : legacySamples) {
            normalized.write(sample);
            normalized.write(0);
        }
        return new ByteArrayInputStream(normalized.toByteArray());
    }

    @Override
    public int durationSeconds() {
        int seconds = legacySegment.rawSampleCount() / legacySegment.legacySampleRate();
        return Math.max(1, seconds);
    }

    @Override
    public String summary() {
        return legacySegment.displayName() +
                " captured as " + legacySegment.getRecorderFormat() +
                ", adapted for " + desiredCodec +
                " by " + legacySegment.supervisedBy();
    }
}

