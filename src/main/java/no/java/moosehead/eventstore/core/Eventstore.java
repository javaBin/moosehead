package no.java.moosehead.eventstore.core;

import no.java.moosehead.aggregate.WorkshopAggregate;
import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.WorkshopAddedByAdmin;
import no.java.moosehead.eventstore.system.SystemBootstrapDone;
import no.java.moosehead.eventstore.utils.ClassSerializer;
import no.java.moosehead.eventstore.utils.FileHandler;
import no.java.moosehead.eventstore.utils.RevisionGenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Eventstore {

    private FileHandler fileHandler;
    private ClassSerializer classSerializer = new ClassSerializer();
    private ArrayList<AbstractEvent> eventstorage = new ArrayList<>();
    private ArrayList<EventSubscription> eventSubscribers = new ArrayList<>();

    /**
     * Will persist all events. Boostraps the eventstore with events from the file.
     * Events will be passed to listeners when they subscribe to the EventStore.
     * @param fileHandler
     */
    public Eventstore(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
        initEventStoreWithFileHandler();
    }

    public Eventstore() {

    }

    public void playbackEventsToSubscribers() {
        for (AbstractEvent event: eventstorage) {
            for (EventSubscription eventSubscribers : this.eventSubscribers) {
                eventSubscribers.eventAdded(event);
            }
        }
        RevisionGenerator revisionGenerator = SystemSetup.instance().revisionGenerator();
        if (eventstorage.size() > 0) {
            revisionGenerator.resetRevision(eventstorage.size());
        }
        addEvent(new SystemBootstrapDone(revisionGenerator.nextRevisionId()));
    }


    public void addEvent(AbstractEvent event) {
        //System.out.println("Added event " + event.getClass() + "->" + event.getRevisionId());
        if ((!(event instanceof TransientEvent)) && fileHandler != null) {
            fileHandler.writeToFile(classSerializer.asString(event) + "\n");
        }

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

    public void addEventSubscriber(EventSubscription eventSubscriber) {
        eventSubscribers.add(eventSubscriber);
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
                AbstractEvent ev = classSerializer.asObject(line);
                eventstorage.add(ev);
            }
            buffStream.close();
            fileHandler.closeInputFile();

            fileHandler.openFileForOutput();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long numberOfWorkshops() {
        return eventstorage.stream().filter(ae -> ae instanceof WorkshopAddedByAdmin).count();
    }

    public List<AbstractEvent> getEventstorageCopy() {
        return new ArrayList<>(eventstorage);
    }
}
