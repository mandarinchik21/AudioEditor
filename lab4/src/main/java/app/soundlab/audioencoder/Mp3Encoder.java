package app.soundlab.audioencoder;

public class Mp3Encoder implements AudioEncoder {
    private static volatile Mp3Encoder sharedReference;
    private final String formatName = "MP3";

    private Mp3Encoder() {
    }

    public static Mp3Encoder get() {
        Mp3Encoder current = sharedReference;
        if (current == null) {
            synchronized (Mp3Encoder.class) {
                current = sharedReference;
                if (current == null) {
                    current = new Mp3Encoder();
                    sharedReference = current;
                }
            }
        }
        return current;
    }

    @Override
    public String encode(String resourceName) {
        return "[Mp3Encoder] Converted '%s' to %s format.".formatted(resourceName, formatName);
    }
}

