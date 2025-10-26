package app.soundlab;

import app.soundlab.audit.AuditLog;
import app.soundlab.audit.ConsoleOperationLogger;
import app.soundlab.audit.EventSink;
import app.soundlab.audit.OpenOperationLogger;

import java.io.File;

public class ObserverDemo {
    public static void main(String[] args) {
        AuditLog auditLog = new AuditLog();

        EventSink persistentObserver = new OpenOperationLogger(new File("audit/logs.txt"));
        EventSink consoleObserver = new ConsoleOperationLogger("Console observer");

        auditLog.subscribeToOpen(persistentObserver);
        auditLog.subscribeToOpen(consoleObserver);

        System.out.println("First open notifies both observers.");
        auditLog.fileOpen();

        auditLog.unsubscribeFromOpen(consoleObserver);
        System.out.println("Second open notifies only the file logger.");
        auditLog.fileOpen();
    }
}

