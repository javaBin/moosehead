package no.java.moosehead.saga;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.*;
import no.java.moosehead.eventstore.system.SystemBootstrapDone;
import no.java.moosehead.web.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
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

        verify(emailSender).sendEmailConfirmation("darth@a.com", reservationAddedByUser.getReservationToken(), "one");
        verifyNoMoreInteractions(emailSender);
    }

    @Test
    public void shouldNotSendIfBootstrapNotDone() throws Exception {
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one", Optional.empty(),1));

        verifyNoMoreInteractions(emailSender);
    }

    @Test
    public void shouldSendReservationConfirmation() throws Exception {
        emailSaga.eventAdded(new WorkshopAddedBySystem(System.currentTimeMillis(),0L,"one",10));
        ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one",Optional.empty(),1);
        emailSaga.eventAdded(reservationAddedByUser);
        emailSaga.eventAdded(new SystemBootstrapDone(1L));
        emailSaga.eventAdded(new EmailConfirmedByUser("darth@a.com",System.currentTimeMillis(),3L));

        verify(emailSender).sendReservationConfirmation("darth@a.com", "one", reservationAddedByUser.getReservationToken());
    }

    @Test
    public void shouldSendCancellationConfirmation() throws Exception {
        ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one",Optional.empty(),1);
        emailSaga.eventAdded(reservationAddedByUser);
        emailSaga.eventAdded(new SystemBootstrapDone(1L));
        emailSaga.eventAdded(new ReservationCancelledByUser(System.currentTimeMillis(), 3L, "darth@a.com", "one"));

        verify(emailSender).sendCancellationConfirmation("darth@a.com", "one");

    }

    @Test
    public void shouldAskForEmailConfirmationOnlyOnce() throws Exception {
        emailSaga.eventAdded(new WorkshopAddedBySystem(System.currentTimeMillis(),0L,"one",10));
        emailSaga.eventAdded(new WorkshopAddedBySystem(System.currentTimeMillis(),0L,"two",10));
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
        emailSaga.eventAdded(new WorkshopAddedBySystem(System.currentTimeMillis(),0L,"one",10));
        emailSaga.eventAdded(new WorkshopAddedBySystem(System.currentTimeMillis(),0L,"two",10));
        final ReservationAddedByUser reservationAddedByUser1 = new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one",Optional.empty(),1);
        final ReservationAddedByUser reservationAddedByUser2 = new ReservationAddedByUser(System.currentTimeMillis(), 4L, "darth@a.com", "Darth", "two",Optional.empty(),1);
        emailSaga.eventAdded(reservationAddedByUser1);
        emailSaga.eventAdded(reservationAddedByUser2);
        emailSaga.eventAdded(new SystemBootstrapDone(1L));
        emailSaga.eventAdded(new EmailConfirmedByUser("darth@a.com", System.currentTimeMillis(), 3L));

        verify(emailSender,atLeastOnce()).sendReservationConfirmation("darth@a.com","one",reservationAddedByUser1.getReservationToken());
        verify(emailSender,atLeastOnce()).sendReservationConfirmation("darth@a.com", "two", reservationAddedByUser2.getReservationToken());

    }

    @Test
    public void shouldNotSendConfirmationsOnCancelledRegistrations() throws Exception {
        emailSaga.eventAdded(new WorkshopAddedBySystem(System.currentTimeMillis(),1L,"one",2));
        emailSaga.eventAdded(new WorkshopAddedBySystem(System.currentTimeMillis(),2L,"two",2));

        final ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(System.currentTimeMillis(), 3L, "darth@a.com", "Darth", "one",Optional.empty(),1);
        emailSaga.eventAdded(reservationAddedByUser);
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 4L, "darth@a.com", "Darth", "two",Optional.empty(),1));
        emailSaga.eventAdded(new ReservationCancelledByUser(System.currentTimeMillis(),6L,"darth@a.com","two"));
        emailSaga.eventAdded(new SystemBootstrapDone(7L));

        emailSaga.eventAdded(new EmailConfirmedByUser("darth@a.com", System.currentTimeMillis(), 10L));

        verify(emailSender).sendReservationConfirmation("darth@a.com", "one", reservationAddedByUser.getReservationToken());

        verifyNoMoreInteractions(emailSender);
    }

    @Test
    public void shouldSendWaitingListInfoWhenWorkshopIsFull() throws Exception {
        emailSaga.eventAdded(new WorkshopAddedBySystem(System.currentTimeMillis(),0L,"one",2));
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "one",Optional.empty(),1));
        emailSaga.eventAdded(new EmailConfirmedByUser("darth@a.com",System.currentTimeMillis(), 2L));
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 4L, "luke@a.com", "Luke", "one",Optional.empty(),1));
        emailSaga.eventAdded(new EmailConfirmedByUser("luke@a.com",System.currentTimeMillis(), 2L));
        emailSaga.eventAdded(new ReservationAddedByUser(System.currentTimeMillis(), 5L, "jarjar@a.com", "JarJar", "one",Optional.empty(),1));
        emailSaga.eventAdded(new SystemBootstrapDone(1L));

        emailSaga.eventAdded(new EmailConfirmedByUser("jarjar@a.com", System.currentTimeMillis(), 2L));

        verify(emailSender).sendWaitingListInfo("jarjar@a.com", "one");

        verifyNoMoreInteractions(emailSender);
    }

    @Test
    public void shouldSendConfirmationWhenPlaceBecomesAvailible() throws Exception {
        emailSaga.eventAdded(new WorkshopAddedBySystem(System.currentTimeMillis(),0L,"one",2));
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

    @Test
    public void workshopCanHaveDifferentSpaces() throws Exception {
        emailSaga.eventAdded(new WorkshopAddedByAdmin(System.currentTimeMillis(), 1L, "wsone", 3));
        ReservationAddedByUser reservationOne = new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "wsone", Optional.of("darth@a.com"), 3);
        emailSaga.eventAdded(reservationOne);
        emailSaga.eventAdded(new SystemBootstrapDone(3L));

        ReservationAddedByUser reservationTwo = new ReservationAddedByUser(System.currentTimeMillis(), 2L, "luke@a.com", "Luke", "wsone", Optional.of("luke@a.com"), 3);
        emailSaga.eventAdded(reservationTwo);

        verify(emailSender).sendWaitingListInfo("luke@a.com", "wsone");
    }

    @Test
    public void shouldSendConfirmWithCancellationHandleSpecificSpaces() throws Exception {
        emailSaga.eventAdded(new WorkshopAddedByAdmin(System.currentTimeMillis(), 1L, "wsone", 3));
        ReservationAddedByUser reservationOne = new ReservationAddedByUser(System.currentTimeMillis(), 2L, "darth@a.com", "Darth", "wsone", Optional.of("darth@a.com"), 3);
        emailSaga.eventAdded(reservationOne);

        ReservationAddedByUser reservationTwo = new ReservationAddedByUser(System.currentTimeMillis(), 3L, "luke@a.com", "Luke", "wsone", Optional.of("luke@a.com"), 3);
        emailSaga.eventAdded(reservationTwo);
        emailSaga.eventAdded(new SystemBootstrapDone(4L));

        ReservationCancelledByUser cancelledOne = new ReservationCancelledByUser(System.currentTimeMillis(), 4L, "darth@a.com", "wsone");
        emailSaga.eventAdded(cancelledOne);

        verify(emailSender).sendCancellationConfirmation("darth@a.com", "wsone");
        verify(emailSender).sendReservationConfirmation("luke@a.com", "wsone", reservationTwo.getReservationToken());

    }

}
