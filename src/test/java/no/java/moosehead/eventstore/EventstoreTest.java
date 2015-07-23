package no.java.moosehead.eventstore;

import no.java.moosehead.aggregate.WorkshopAggregate;
import no.java.moosehead.eventstore.core.Eventstore;
import no.java.moosehead.eventstore.utils.FileHandler;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class EventstoreTest {

    @Test
    public void shouldCreateEvent() throws Exception {
        Eventstore eventstore = new Eventstore(new FileHandler());
        assertThat(eventstore).isNotNull();
    }

    @Test
    public void shouldAddWorkshopToEventstore() {
        Eventstore eventstore = new Eventstore(new FileHandler());
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, "W1", 0));
        assertThat(eventstore.numberOfEvents()).isGreaterThan(0);
    }

    @Test
    public void shouldAddProjectionToEventStore() {
        Eventstore eventstore = new Eventstore(new FileHandler());
        eventstore.addEventSubscriber(new WorkshopAggregate());
        assertThat(eventstore.numberOfListeners()).isGreaterThan(0);
    }


}
