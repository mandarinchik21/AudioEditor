package app.soundlab.audit;

import java.io.File;

public class AuditLog {
    EventsHub eventManager;
    final File logFile = new File("audit/logs.txt");
    OpenOperationLogger operationLogger;

    public AuditLog() {
        eventManager = new EventsHub("openFile");

        operationLogger = new OpenOperationLogger(logFile);
        eventManager.subscribe(operationLogger, "openFile");
    }

    public void fileOpen() {
        eventManager.notifySubscribers("openFile");
    }
}
