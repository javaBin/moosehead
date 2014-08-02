package no.java.moosehead.eventstore;

import no.java.moosehead.eventstore.core.Eventstore;
import no.java.moosehead.eventstore.utils.FileHandler;
import org.junit.Test;

import java.io.*;

import static org.fest.assertions.Assertions.assertThat;

public class EventStorePersistenceTest {

    @Test
    public void testSerialiseringAvEventer() throws IOException {
        File f = File.createTempFile("tmp", null);
        f.deleteOnExit();
        Eventstore eventstore = new Eventstore(new FileHandler(f.getCanonicalPath()));
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, "W1", 0));
        assertThat(f.length()).isEqualTo(127);
    }

    @Test
    public void testDeSerialiseringAvEventer() throws IOException {
        Eventstore eventstore = new Eventstore(new FileHandler("src/test/resources/EventStorePersistenceTest.txt"));
        eventstore.playbackEventsToSubscribers();
        assertThat(eventstore.numberOfEvents()).isEqualTo(1);

    }
}
