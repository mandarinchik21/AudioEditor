package app.soundlab.editor;

import app.soundlab.audiotrack.SegmentEntity;

public class SelectUseCase implements AudioEditor {
    private final int start;
    private final int end;

    public SelectUseCase(int start, int end) {
        this.start = start;
        this.end = end;
        System.out.println("SelectUseCase created for segment: " + start + " to " + end);
    }

    @Override
    public void edit(SegmentEntity audiotrack) {
        System.out.println("SelectUseCase: Selecting segment " + start + " to " + end);
        audiotrack.select(start, end);
    }
}


