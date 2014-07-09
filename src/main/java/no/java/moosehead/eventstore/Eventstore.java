package no.java.moosehead.eventstore;

import no.java.moosehead.aggregate.WorkshopAggregate;

import java.io.*;
import java.util.ArrayList;

public class Eventstore {

    FileHandler fileHandler;
    ClassSerializer classSerializer = new ClassSerializer();

    /**
     * Will persist all events. Boostraps the eventstore with events from the file.
     * Events will be passed to listeners when they subscribe to the EventStore.
     * @param fileHandler
     */
    public Eventstore(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
        initEventStoreWithFileHandler();
    }

    private ArrayList<AbstractEvent> eventstorage = new ArrayList<>();
    private ArrayList<EventSubscription> eventSubscribers = new ArrayList<>();

    public void addEvent(AbstractEvent event) {
        fileHandler.writeToFile(classSerializer.asString(event));
        eventstorage.add(event);
        for (EventSubscription eventSubscribers : this.eventSubscribers) {
            eventSubscribers.eventAdded(event);
        }
    }

    public int numberOfEvents() {
        return eventstorage.size();
    }

    public int numberOfListeners() {
        return eventSubscribers.size();
    }

    public void addEventSubscriber(WorkshopAggregate workshopAggregate) {
        eventSubscribers.add(workshopAggregate);
    }

    /**
     * Reads the file and creates Events
     */
    private void initEventStoreWithFileHandler() {
        try {
            String line;

            fileHandler.openFileForInput();
            BufferedReader buffStream = new BufferedReader(fileHandler.getInputStreamReader());
            while((line = buffStream.readLine()) != null) {
                eventstorage.add(classSerializer.asObject(line));
            }
            buffStream.close();
            fileHandler.closeInputFile();

            fileHandler.openFileForOutput();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
