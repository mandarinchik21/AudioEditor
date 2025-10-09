package app.soundlab.audioencoder;

public class OggEncoder implements AudioEncoder {
    private static volatile OggEncoder sharedReference;
    private final String formatName = "OGG";

    private OggEncoder() {
    }

    public static OggEncoder get() {
        OggEncoder current = sharedReference;
        if (current == null) {
            synchronized (OggEncoder.class) {
                current = sharedReference;
                if (current == null) {
                    current = new OggEncoder();
                    sharedReference = current;
                }
            }
        }
        return current;
    }

    @Override
    public String encode(String resourceName) {
        return "[OggEncoder] Converted '%s' to %s format.".formatted(resourceName, formatName);
    }
}

