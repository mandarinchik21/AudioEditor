package app.soundlab.editor;

import app.soundlab.audiotrack.SegmentEntity;

public class CopyUseCase implements AudioEditor {
    @Override
    public void edit(SegmentEntity audiotrack) {
        System.out.println("CopyUseCase: Copying audio segment");
        audiotrack.copy();
    }
}


