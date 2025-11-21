package app.soundlab.audit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventsHub {
    Map<String, List<EventSink>> listeners = new HashMap<>();

    public EventsHub(String... operations) {
        for (String operation : operations) {
            this.listeners.put(operation, new ArrayList<>());
        }
    }

    public void subscribe(EventSink eventSink, String event) {
        List<EventSink> users = listeners.get(event);
        users.add(eventSink);
    }

    public void unSubscribe(EventSink subscriber, String event) {
        List<EventSink> users = listeners.get(event);
        users.remove(subscriber);
    }

    public void notifySubscribers(String event) {
        List<EventSink> users = listeners.get(event);
        for (EventSink listener : users) {
            listener.handleEvent();
        }
    }
}
