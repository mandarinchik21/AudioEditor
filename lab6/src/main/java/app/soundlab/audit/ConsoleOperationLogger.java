package app.soundlab.audit;

import java.time.LocalDateTime;

public class ConsoleOperationLogger implements EventSink {
    private final String observerName;

    public ConsoleOperationLogger(String observerName) {
        this.observerName = observerName;
    }

    @Override
    public void handleEvent() {
        System.out.println(observerName + " recorded file opening at " + LocalDateTime.now());
    }
}

