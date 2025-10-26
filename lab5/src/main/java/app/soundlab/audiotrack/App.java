package app.soundlab.audiotrack;

public class App {

    public static void main(String[] args) {
        SegmentEntity legacySegment = SegmentEntity.microphoneCapture(
                "Lab 5 Groove",
                "Engineer Ola",
                5
        );

        EncodingRequest adapter = new SegmentEncodingAdapter(legacySegment, "mp3");
        ConsoleAudioEncoder encoder = new ConsoleAudioEncoder();
        encoder.export(adapter);
    }
}

