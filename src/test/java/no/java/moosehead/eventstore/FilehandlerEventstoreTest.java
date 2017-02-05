package no.java.moosehead.eventstore;

import no.java.moosehead.aggregate.WorkshopAggregate;
import no.java.moosehead.eventstore.core.FilehandlerEventstore;
import no.java.moosehead.eventstore.utils.FileHandler;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class FilehandlerEventstoreTest {

    @Test
    public void shouldCreateEvent() throws Exception {
        FilehandlerEventstore eventstore = new FilehandlerEventstore(new FileHandler());
        assertThat(eventstore).isNotNull();
    }

    @Test
    public void shouldAddWorkshopToEventstore() {
        FilehandlerEventstore eventstore = new FilehandlerEventstore(new FileHandler());
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, "W1", 0));
        assertThat(eventstore.numberOfEvents()).isGreaterThan(0);
    }

    @Test
    public void shouldAddProjectionToEventStore() {
        FilehandlerEventstore eventstore = new FilehandlerEventstore(new FileHandler());
        eventstore.addEventSubscriber(new WorkshopAggregate());
        assertThat(eventstore.numberOfListeners()).isGreaterThan(0);
    }


}
