package app.soundlab.audiotrack;

import java.io.IOException;
import java.io.InputStream;

public class ConsoleAudioEncoder {

    public void export(EncodingRequest request) {
        System.out.println("Encoding job started:");
        System.out.println("Summary: " + request.summary());
        System.out.println("Target codec: " + request.targetCodec());
        System.out.println("Duration (s): " + request.durationSeconds());

        int bytes = consume(request.dataStream());
        System.out.println("Streamed bytes: " + bytes);
        System.out.println("=== Encoding job finished ===");
    }

    private int consume(InputStream inputStream) {
        byte[] buffer = new byte[1024];
        int total = 0;
        int read;
        try (InputStream stream = inputStream) {
            while ((read = stream.read(buffer)) != -1) {
                total += read;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to consume stream: " + e.getMessage(), e);
        }
        return total;
    }
}

