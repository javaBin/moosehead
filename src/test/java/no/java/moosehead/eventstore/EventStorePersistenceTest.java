package no.java.moosehead.eventstore;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.core.Eventstore;
import no.java.moosehead.eventstore.utils.FileHandler;
import no.java.moosehead.eventstore.utils.TokenGenerator;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventStorePersistenceTest {
    @Before
    public void setUp() throws Exception {
        SystemSetup systemSetup = mock(SystemSetup.class);
        TokenGenerator tokenGenerator = new TokenGenerator();
        when(systemSetup.revisionGenerator()).thenReturn(tokenGenerator);
        SystemSetup.setSetup(systemSetup);

    }

    @Test
    public void testSerialiseringAvEventer() throws IOException {
        File f = File.createTempFile("tmp", null);
        f.deleteOnExit();
        Eventstore eventstore = new Eventstore(new FileHandler(f.getCanonicalPath()));
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, "W1", 0));
        assertThat(f.length()).isEqualTo(160);
    }

    @Test
    public void testDeSerialiseringAvEventer() throws IOException {
        Eventstore eventstore = new Eventstore(new FileHandler("src/test/resources/EventStorePersistenceTest.txt"));
        eventstore.playbackEventsToSubscribers();
        assertThat(eventstore.numberOfEvents()).isEqualTo(1);

    }
}
