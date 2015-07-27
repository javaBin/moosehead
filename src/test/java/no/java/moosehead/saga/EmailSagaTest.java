package no.java.moosehead.saga;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.EmailConfirmedByUser;
import no.java.moosehead.eventstore.ReservationAddedByUser;
import no.java.moosehead.eventstore.ReservationCancelledByUser;
import no.java.moosehead.eventstore.system.SystemBootstrapDone;
import no.java.moosehead.web.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

        Map<String,String> conf = new HashMap<>();
        conf.put("placesPerWorkshop","2");
        Configuration.initData(conf);

        emailSaga = new EmailSaga();
    }

    @After
    public void tearDown() throws Exception {
        SystemSetup.setSetup(null);
        Configuration.initData(null);
    }

    @Test
    public void shouldSendEmailOnConfirm() throws Exception {
        emailSaga.eventAdded(new SystemBootstrapDone(1L));
        final ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one",Optional.empty(),1);
        emailSaga.eventAdded(reservationAddedByUser);

        verify(emailSender).sendEmailConfirmation("darth@a.com", reservationAddedByUser.getReservationToken(),"one");
        verifyNoMoreInteractions(emailSender);
    }

    @Test
    public void shouldNotSendIfBootstrapNotDone() throws Exception {
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one", Optional.empty(),1));

        verifyNoMoreInteractions(emailSender);
    }

    @Test
    public void shouldSendReservationConfirmation() throws Exception {
        ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one",Optional.empty(),1);
        emailSaga.eventAdded(reservationAddedByUser);
        emailSaga.eventAdded(new SystemBootstrapDone(1L));
        emailSaga.eventAdded(new EmailConfirmedByUser("darth@a.com",System.currentTimeMillis(),3L));

        verify(emailSender).sendReservationConfirmation("darth@a.com","one",reservationAddedByUser.getReservationToken());
    }

    @Test
    public void shouldSendCancellationConfirmation() throws Exception {
        ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one",Optional.empty(),1);
        emailSaga.eventAdded(reservationAddedByUser);
        emailSaga.eventAdded(new SystemBootstrapDone(1L));
        emailSaga.eventAdded(new ReservationCancelledByUser(System.currentTimeMillis(), 3L, "darth@a.com", "one"));

        verify(emailSender).sendCancellationConfirmation("darth@a.com","one");

    }

    @Test
    public void shouldAskForEmailConfirmationOnlyOnce() throws Exception {
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one",Optional.empty(),1));
        emailSaga.eventAdded(new EmailConfirmedByUser("darth@a.com", System.currentTimeMillis(), 3L));
        emailSaga.eventAdded(new SystemBootstrapDone(1L));

        final ReservationAddedByUser reservationAddedByUser =new ReservationAddedByUser(System.currentTimeMillis(), 4L, "darth@a.com", "Darth", "two",Optional.empty(),1);
        emailSaga.eventAdded(reservationAddedByUser);
        verify(emailSender).sendReservationConfirmation("darth@a.com", "two",reservationAddedByUser.getReservationToken());
        verifyNoMoreInteractions(emailSender);
    }

    @Test
    public void shouldSendOneEmailForEachReservation() throws Exception {
        final ReservationAddedByUser reservationAddedByUser1 = new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one",Optional.empty(),1);
        final ReservationAddedByUser reservationAddedByUser2 = new ReservationAddedByUser(System.currentTimeMillis(), 4L, "darth@a.com", "Darth", "two",Optional.empty(),1);
        emailSaga.eventAdded(reservationAddedByUser1);
        emailSaga.eventAdded(reservationAddedByUser2);
        emailSaga.eventAdded(new SystemBootstrapDone(1L));
        emailSaga.eventAdded(new EmailConfirmedByUser("darth@a.com", System.currentTimeMillis(), 3L));

        verify(emailSender,atLeastOnce()).sendReservationConfirmation("darth@a.com","one",reservationAddedByUser1.getReservationToken());
        verify(emailSender,atLeastOnce()).sendReservationConfirmation("darth@a.com","two",reservationAddedByUser2.getReservationToken());

    }

    @Test
    public void shouldNotSendConfirmationsOnCancelledRegistrations() throws Exception {
        final ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one",Optional.empty(),1);
        emailSaga.eventAdded(reservationAddedByUser);
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 4L, "darth@a.com", "Darth", "two",Optional.empty(),1));
        emailSaga.eventAdded(new ReservationCancelledByUser(System.currentTimeMillis(),6L,"darth@a.com","two"));
        emailSaga.eventAdded(new SystemBootstrapDone(1L));

        emailSaga.eventAdded(new EmailConfirmedByUser("darth@a.com", System.currentTimeMillis(), 3L));

        verify(emailSender).sendReservationConfirmation("darth@a.com", "one",reservationAddedByUser.getReservationToken());

        verifyNoMoreInteractions(emailSender);
    }

    @Test
    public void shouldSendWaitingListInfoWhenWorkshopIsFull() throws Exception {
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one",Optional.empty(),1));
        emailSaga.eventAdded(new EmailConfirmedByUser("darth@a.com",System.currentTimeMillis(), 2L));
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 4L, "luke@a.com", "Luke", "one",Optional.empty(),1));
        emailSaga.eventAdded(new EmailConfirmedByUser("luke@a.com",System.currentTimeMillis(), 2L));
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 5L, "jarjar@a.com", "JarJar", "one",Optional.empty(),1));
        emailSaga.eventAdded(new SystemBootstrapDone(1L));

        emailSaga.eventAdded(new EmailConfirmedByUser("jarjar@a.com",System.currentTimeMillis(), 2L));

        verify(emailSender).sendWaitingListInfo("jarjar@a.com", "one");

        verifyNoMoreInteractions(emailSender);
    }

    @Test
    public void shouldSendConfirmationWhenPlaceBecomesAvailible() throws Exception {
        final ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(System.currentTimeMillis(), 5L, "jarjar@a.com", "JarJar", "one",Optional.empty(),1);
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one",Optional.empty(),1));
        emailSaga.eventAdded(new EmailConfirmedByUser("darth@a.com",System.currentTimeMillis(), 2L));
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 4L, "luke@a.com", "Luke", "one",Optional.empty(),1));
        emailSaga.eventAdded(new EmailConfirmedByUser("luke@a.com",System.currentTimeMillis(), 2L));
        emailSaga.eventAdded(reservationAddedByUser);
        emailSaga.eventAdded(new EmailConfirmedByUser("jarjar@a.com",System.currentTimeMillis(), 2L));
        emailSaga.eventAdded(new SystemBootstrapDone(1L));

        emailSaga.eventAdded(new ReservationCancelledByUser(System.currentTimeMillis(),7L,"darth@a.com","one"));

        verify(emailSender).sendCancellationConfirmation("darth@a.com","one");
        verify(emailSender).sendReservationConfirmation("jarjar@a.com","one",reservationAddedByUser.getReservationToken());

        verifyNoMoreInteractions(emailSender);

    }
}
