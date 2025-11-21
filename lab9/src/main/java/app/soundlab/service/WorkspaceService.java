package app.soundlab.service;

import app.soundlab.audiotrack.SegmentEntity;
import app.soundlab.editor.AudioEditor;

public class WorkspaceService {
    SegmentEntity audiotrack;
    AudioEditor selector;
    AudioEditor copier;
    AudioEditor paste;
    AudioEditor cut;

    SegmentEntity select(SegmentEntity audiotrack, int length) {
        return null;
    }

    SegmentEntity copy(SegmentEntity audiotrack) {
        return null;
    }

    SegmentEntity paste(SegmentEntity audiotrack) {
        return null;
    }

    void cut(SegmentEntity audiotrack) {

    }
}

