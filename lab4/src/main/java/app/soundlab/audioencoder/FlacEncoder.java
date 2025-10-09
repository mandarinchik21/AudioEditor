package app.soundlab.audioencoder;

public class FlacEncoder implements AudioEncoder {
    private static volatile FlacEncoder sharedReference;
    private final String formatName = "FLAC";

    private FlacEncoder() {
    }

    public static FlacEncoder get() {
        FlacEncoder current = sharedReference;
        if (current == null) {
            synchronized (FlacEncoder.class) {
                current = sharedReference;
                if (current == null) {
                    current = new FlacEncoder();
                    sharedReference = current;
                }
            }
        }
        return current;
    }

    @Override
    public String encode(String resourceName) {
        return "[FlacEncoder] Converted '%s' to %s format.".formatted(resourceName, formatName);
    }
}
