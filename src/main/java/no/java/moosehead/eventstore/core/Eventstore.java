package no.java.moosehead.eventstore.core;

import no.java.moosehead.aggregate.WorkshopAggregate;
import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.WorkshopAddedBySystem;
import no.java.moosehead.eventstore.WorkshopAddedEvent;
import no.java.moosehead.eventstore.system.SystemBootstrapDone;
import no.java.moosehead.eventstore.utils.FileHandler;
import no.java.moosehead.eventstore.utils.TokenGenerator;
import no.java.moosehead.web.Configuration;

import java.util.List;

public interface Eventstore {

    void addEventSubscriber(EventSubscription eventSubscription);

    List<AbstractEvent> getEventstorageCopy();

    void addEvent(AbstractEvent event);

    default long numberOfWorkshops() {
        return getEventstorageCopy().stream().filter(ae -> ae instanceof WorkshopAddedBySystem).count();
    }

    static Eventstore create() {
        if (Configuration.dbName() != null) {
            return new DbEventStore();
        }
        if (Configuration.eventstoreFilename() != null) {
            return new FilehandlerEventstore(new FileHandler(Configuration.eventstoreFilename()));
        }
        return new FilehandlerEventstore();
    }

    default void playbackEventsToSubscribers() {
        List<AbstractEvent> eventstorage = getEventstorageCopy();
        for (AbstractEvent event: eventstorage) {
            for (EventSubscription eventSubscribers : getEventSubscribers()) {
                eventSubscribers.eventAdded(event);
            }
        }
        TokenGenerator tokenGenerator = SystemSetup.instance().revisionGenerator();
        if (eventstorage.size() > 0) {
            tokenGenerator.resetRevision(eventstorage.size());
        }
        addEvent(new SystemBootstrapDone(tokenGenerator.nextRevisionId()));
    }

    List<EventSubscription> getEventSubscribers();
}
