package no.java.moosehead.eventstore;

import org.junit.Test;

import java.io.*;

import static org.fest.assertions.Assertions.assertThat;

public class EventStorePersitenceTest {

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
        Eventstore eventstore = new Eventstore(new FileHandler("src/test/resources/EventStorePersitenceTest.txt"));
        assertThat(eventstore.numberOfEvents()).isEqualTo(1);

    }
}
