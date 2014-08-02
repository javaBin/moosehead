package no.java.moosehead.saga;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.EmailConfirmedByUser;
import no.java.moosehead.eventstore.ReservationAddedByUser;
import no.java.moosehead.eventstore.ReservationCancelledByUser;
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

        verify(emailSender).sendEmailConfirmation("darth@a.com", "2");
        verifyNoMoreInteractions(emailSender);
    }

    @Test
    public void shouldNotSendIfBootstrapNotDone() throws Exception {
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one"));

        verifyNoMoreInteractions(emailSender);
    }

    @Test
    public void shouldSendReservationConfirmation() throws Exception {
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one"));
        emailSaga.eventAdded(new SystemBootstrapDone(1L));
        emailSaga.eventAdded(new EmailConfirmedByUser("darth@a.com",System.currentTimeMillis(),3L));

        verify(emailSender).sendReservationConfirmation("darth@a.com","one");
    }

    @Test
    public void shouldSendCancellationConfirmation() throws Exception {
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one"));
        emailSaga.eventAdded(new SystemBootstrapDone(1L));
        emailSaga.eventAdded(new ReservationCancelledByUser(System.currentTimeMillis(), 3L, "darth@a.com", "one"));

        verify(emailSender).sendCancellationConfirmation("darth@a.com","one");

    }

    @Test
    public void shouldAskForEmailConfirmationOnlyOnce() throws Exception {
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one"));
        emailSaga.eventAdded(new EmailConfirmedByUser("darth@a.com", System.currentTimeMillis(), 3L));
        emailSaga.eventAdded(new SystemBootstrapDone(1L));
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 4L, "darth@a.com", "Darth", "two"));

        verify(emailSender).sendReservationConfirmation("darth@a.com", "two");
        verifyNoMoreInteractions(emailSender);
    }

    @Test
    public void shouldSendOneEmailForEachReservation() throws Exception {
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one"));
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 4L, "darth@a.com", "Darth", "two"));
        emailSaga.eventAdded(new SystemBootstrapDone(1L));
        emailSaga.eventAdded(new EmailConfirmedByUser("darth@a.com", System.currentTimeMillis(), 3L));

        verify(emailSender).sendReservationConfirmation("darth@a.com", "one");
        verify(emailSender).sendReservationConfirmation("darth@a.com", "two");

        verifyNoMoreInteractions(emailSender);
    }

    @Test
    public void shouldNotSendConfirmationsOnCancelledRegistrations() throws Exception {
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one"));
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 4L, "darth@a.com", "Darth", "two"));
        emailSaga.eventAdded(new ReservationCancelledByUser(System.currentTimeMillis(),6L,"darth@a.com","two"));
        emailSaga.eventAdded(new SystemBootstrapDone(1L));
        emailSaga.eventAdded(new EmailConfirmedByUser("darth@a.com", System.currentTimeMillis(), 3L));

        verify(emailSender).sendReservationConfirmation("darth@a.com", "one");

        verifyNoMoreInteractions(emailSender);

    }
}
