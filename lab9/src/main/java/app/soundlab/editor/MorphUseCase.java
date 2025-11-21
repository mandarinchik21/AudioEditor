package app.soundlab.editor;

import app.soundlab.audiotrack.SegmentEntity;

public class MorphUseCase implements AudioEditor {
    private final double factor;

    public MorphUseCase(double factor) {
        this.factor = factor;
    }

    @Override
    public void edit(SegmentEntity audiotrack) {
        System.out.println("MorphUseCase: Deforming segment (factor=" + factor + ")");
        audiotrack.morph(factor);
    }
}


