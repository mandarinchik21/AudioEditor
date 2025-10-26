package app.soundlab.audit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

public class OpenOperationLogger implements EventSink {
    private final File logFile;

    public OpenOperationLogger(File logFile) {
       this.logFile = logFile;
    }

    @Override
    public void handleEvent() {
        File parent = logFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write("File was opened at " + LocalTime.now() + " " + LocalDate.now() + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
