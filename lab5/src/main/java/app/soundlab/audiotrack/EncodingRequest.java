package app.soundlab.audiotrack;

import java.io.InputStream;

public interface EncodingRequest {

    String targetCodec();

    InputStream dataStream();

    int durationSeconds();

    String summary();
}

