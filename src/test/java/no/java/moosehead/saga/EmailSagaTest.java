package no.java.moosehead.saga;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.ReservationAddedByUser;
import no.java.moosehead.eventstore.system.SystemBootstrapDone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class EmailSagaTest {

    private EmailSender emailSender;
    private EmailSaga emailSaga;

    @Before
    public void setUp() throws Exception {
        emailSender = mock(EmailSender.class);
        SystemSetup setup = mock(SystemSetup.class);
        when(setup.emailSender()).thenReturn(emailSender);
        SystemSetup.setSetup(setup);

        emailSaga = new EmailSaga();
    }

    @After
    public void tearDown() throws Exception {
        SystemSetup.setSetup(null);

    }

    @Test
    public void shouldSendEmailOnConfirm() throws Exception {
        emailSaga.eventAdded(new SystemBootstrapDone(1L));
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one"));

        verify(emailSender).sendEmailConfirmation("darth@a.com","2");
        verifyNoMoreInteractions(emailSender);
    }

    @Test
    public void shouldNotSendIfBootstrapNotDone() throws Exception {
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one"));

        verifyNoMoreInteractions(emailSender);

    }
}
