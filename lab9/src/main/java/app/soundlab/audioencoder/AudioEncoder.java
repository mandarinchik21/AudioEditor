package app.soundlab.audioencoder;

import app.soundlab.audiotrack.SegmentEntity;

public interface AudioEncoder<T> {
    T encode(SegmentEntity audio);
}