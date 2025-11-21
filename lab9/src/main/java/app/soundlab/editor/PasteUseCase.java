package app.soundlab.editor;

import app.soundlab.audiotrack.SegmentEntity;

public class PasteUseCase implements AudioEditor {
    private final int positionInSecond;
    private final int sampleRate;

    public PasteUseCase(int positionInSecond, int sampleRate) {
        this.positionInSecond = positionInSecond;
        this.sampleRate = sampleRate;
        System.out.println("PasteUseCase created for position: " + positionInSecond + " seconds");
    }

    @Override
    public void edit(SegmentEntity audiotrack) {
        System.out.println("PasteUseCase: Pasting audio segment at position " + positionInSecond + " seconds");
        audiotrack.paste(positionInSecond, sampleRate);
    }
}


