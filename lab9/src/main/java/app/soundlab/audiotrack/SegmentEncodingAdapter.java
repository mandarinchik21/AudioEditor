package app.soundlab.audiotrack;

import java.io.File;

public class SegmentEncodingAdapter {
    private final SegmentEntity audiotrack;

    public SegmentEncodingAdapter(SegmentEntity audiotrack) {
        this.audiotrack = audiotrack;
    }

    public String adaptFormat() {
        return audiotrack.getFormat();
    }

    public File adaptFile() {
        return audiotrack.getFileLink();
    }
}

