package app.soundlab.audit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventsHub {
    private final Map<String, List<EventSink>> listeners = new HashMap<>();

    public EventsHub(String... operations) {
        for (String operation : operations) {
            listeners.put(operation, new ArrayList<>());
        }
    }

    public void subscribe(EventSink eventSink, String event) {
        listeners.computeIfAbsent(event, key -> new ArrayList<>()).add(eventSink);
    }

    public void unSubscribe(EventSink subscriber, String event) {
        List<EventSink> users = listeners.get(event);
        if (users != null) {
            users.remove(subscriber);
        }
    }

    public void notifySubscribers(String event) {
        List<EventSink> users = listeners.get(event);
        if (users == null) {
            return;
        }
        for (EventSink listener : users) {
            listener.handleEvent();
        }
    }
}
