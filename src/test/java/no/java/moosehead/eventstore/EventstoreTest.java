package no.java.moosehead.eventstore;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class EventstoreTest {
    @Test
    public void shouldCreateEvent() throws Exception {
        Eventstore eventstore = new Eventstore();
        assertThat(eventstore).isNotNull();

    }
}
