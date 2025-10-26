package app.soundlab.audit;

public class AuditLog {
    private final EventsHub eventsHub;

    public AuditLog() {
        this.eventsHub = new EventsHub("openFile");
    }

    public void subscribeToOpen(EventSink sink) {
        eventsHub.subscribe(sink, "openFile");
    }

    public void unsubscribeFromOpen(EventSink sink) {
        eventsHub.unSubscribe(sink, "openFile");
    }

    public void fileOpen() {
        eventsHub.notifySubscribers("openFile");
    }
}
