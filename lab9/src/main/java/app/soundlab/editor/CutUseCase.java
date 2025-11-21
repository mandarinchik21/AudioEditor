package app.soundlab.editor;

import app.soundlab.audiotrack.SegmentEntity;

public class CutUseCase implements AudioEditor {
    @Override
    public void edit(SegmentEntity audiotrack) {
        System.out.println("CutUseCase: Cutting audio segment");
        audiotrack.cut();
    }
}


