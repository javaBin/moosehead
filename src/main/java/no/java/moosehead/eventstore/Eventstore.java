package no.java.moosehead.eventstore;

import no.java.moosehead.aggregate.WorkshopAggregate;

import java.util.ArrayList;

public class Eventstore {

    private ArrayList<AbstractEvent> eventstorage = new ArrayList<>();
    private ArrayList<EventListener> eventListeners = new ArrayList<>();

    public void addEvent(AbstractEvent event) {
        eventstorage.add(event);
        for (EventListener eventListener : eventListeners) {
            eventListener.eventAdded(event);
        }
    }

    public int numberOfEvents() {
        return eventstorage.size();
    }

    public int numberOfListeners() {
        return eventListeners.size();
    }

    public void addEventListener(WorkshopAggregate workshopAggregate) {
        eventListeners.add(workshopAggregate);
    }
}
