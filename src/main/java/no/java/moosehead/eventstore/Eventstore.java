package no.java.moosehead.eventstore;

import no.java.moosehead.aggregate.WorkshopAggregate;

import java.util.ArrayList;

public class Eventstore {
    private ArrayList<Event> eventstorage = new ArrayList<>();
    private ArrayList<EventListener> eventListeners = new ArrayList<>();

    public void addEvent(Event workshopAddedByAdmin) {
        eventstorage.add(workshopAddedByAdmin);
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
