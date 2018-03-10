package no.java.moosehead.saga;

import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.domain.WorkshopReservation;
import no.java.moosehead.eventstore.*;
import no.java.moosehead.eventstore.system.SystemBootstrapDone;
import no.java.moosehead.web.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
        final ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("darth@a.com")
                        .setFullname("Darth")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                );
        emailSaga.eventAdded(reservationAddedByUser);

        verify(emailSender).sendEmailConfirmation("darth@a.com", reservationAddedByUser.getReservationToken(), "one");
        verifyNoMoreInteractions(emailSender);
    }

    @Test
    public void shouldNotSendIfBootstrapNotDone() throws Exception {
        emailSaga.eventAdded(new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("darth@a.com")
                        .setFullname("Darth")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                ));

        verifyNoMoreInteractions(emailSender);
    }

    @Test
    public void shouldSendReservationConfirmation() throws Exception {
        emailSaga.eventAdded(new WorkshopAddedBySystem(System.currentTimeMillis(),0L,"one",10));
        ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("darth@a.com")
                        .setFullname("Darth")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                );
        emailSaga.eventAdded(reservationAddedByUser);
        emailSaga.eventAdded(new SystemBootstrapDone(1L));
        emailSaga.eventAdded(new EmailConfirmedByUser("darth@a.com",System.currentTimeMillis(),3L));

        verify(emailSender).sendReservationConfirmation("darth@a.com", "one", reservationAddedByUser.getReservationToken());
    }

    @Test
    public void shouldSendCancellationConfirmation() throws Exception {
        ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("darth@a.com")
                        .setFullname("Darth")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                );
        emailSaga.eventAdded(reservationAddedByUser);
        emailSaga.eventAdded(new SystemBootstrapDone(1L));
        emailSaga.eventAdded(new ReservationCancelledByUser(System.currentTimeMillis(), 3L, "darth@a.com", "one",1));

        verify(emailSender).sendCancellationConfirmation("darth@a.com", "one");

    }

    @Test
    public void shouldAskForEmailConfirmationOnlyOnce() throws Exception {
        emailSaga.eventAdded(new WorkshopAddedBySystem(System.currentTimeMillis(),0L,"one",10));
        emailSaga.eventAdded(new WorkshopAddedBySystem(System.currentTimeMillis(),0L,"two",10));
        emailSaga.eventAdded(new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("darth@a.com")
                        .setFullname("Darth")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                ));
        emailSaga.eventAdded(new EmailConfirmedByUser("darth@a.com", System.currentTimeMillis(), 3L));
        emailSaga.eventAdded(new SystemBootstrapDone(1L));

        final ReservationAddedByUser reservationAddedByUser =new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(4L)
                        .setEmail("darth@a.com")
                        .setFullname("Darth")
                        .setWorkshopId("two")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                );
        emailSaga.eventAdded(reservationAddedByUser);
        verify(emailSender).sendReservationConfirmation("darth@a.com", "two",reservationAddedByUser.getReservationToken());
        verifyNoMoreInteractions(emailSender);
    }

    @Test
    public void shouldSendOneEmailForEachReservation() throws Exception {
        emailSaga.eventAdded(new WorkshopAddedBySystem(System.currentTimeMillis(),0L,"one",10));
        emailSaga.eventAdded(new WorkshopAddedBySystem(System.currentTimeMillis(),0L,"two",10));
        final ReservationAddedByUser reservationAddedByUser1 = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("darth@a.com")
                        .setFullname("Darth")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                );
        final ReservationAddedByUser reservationAddedByUser2 = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(4L)
                        .setEmail("darth@a.com")
                        .setFullname("Darth")
                        .setWorkshopId("two")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                );
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

        final ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(3L)
                        .setEmail("darth@a.com")
                        .setFullname("Darth")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                );
        emailSaga.eventAdded(reservationAddedByUser);
        emailSaga.eventAdded(new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(4L)
                        .setEmail("darth@a.com")
                        .setFullname("Darth")
                        .setWorkshopId("two")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                ));
        emailSaga.eventAdded(new ReservationCancelledByUser(System.currentTimeMillis(),6L,"darth@a.com","two",1));
        emailSaga.eventAdded(new SystemBootstrapDone(7L));

        emailSaga.eventAdded(new EmailConfirmedByUser("darth@a.com", System.currentTimeMillis(), 10L));

        verify(emailSender).sendReservationConfirmation("darth@a.com", "one", reservationAddedByUser.getReservationToken());

        verifyNoMoreInteractions(emailSender);
    }

    @Test
    public void shouldSendWaitingListInfoWhenWorkshopIsFull() throws Exception {
        emailSaga.eventAdded(new WorkshopAddedBySystem(System.currentTimeMillis(),0L,"one",2));
        emailSaga.eventAdded(new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("darth@a.com")
                        .setFullname("Darth")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                ));
        emailSaga.eventAdded(new EmailConfirmedByUser("darth@a.com",System.currentTimeMillis(), 2L));
        emailSaga.eventAdded(new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(4L)
                        .setEmail("luke@a.com")
                        .setFullname("Luke")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                ));
        emailSaga.eventAdded(new EmailConfirmedByUser("luke@a.com",System.currentTimeMillis(), 2L));
        emailSaga.eventAdded(new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(5L)
                        .setEmail("jarjar@a.com")
                        .setFullname("JarJar")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                ));
        emailSaga.eventAdded(new SystemBootstrapDone(1L));

        emailSaga.eventAdded(new EmailConfirmedByUser("jarjar@a.com", System.currentTimeMillis(), 2L));

        verify(emailSender).sendWaitingListInfo("jarjar@a.com", "one");

        verifyNoMoreInteractions(emailSender);
    }

    @Test
    public void shouldSendConfirmationWhenPlaceBecomesAvailible() throws Exception {
        emailSaga.eventAdded(new WorkshopAddedBySystem(System.currentTimeMillis(),0L,"one",2));
        final ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(5L)
                        .setEmail("jarjar@a.com")
                        .setFullname("JarJar")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                );
        emailSaga.eventAdded(new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("darth@a.com")
                        .setFullname("Darth")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                ));
        emailSaga.eventAdded(new EmailConfirmedByUser("darth@a.com",System.currentTimeMillis(), 2L));
        emailSaga.eventAdded(new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(4L)
                        .setEmail("luke@a.com")
                        .setFullname("Luke")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                ));
        emailSaga.eventAdded(new EmailConfirmedByUser("luke@a.com",System.currentTimeMillis(), 2L));
        emailSaga.eventAdded(reservationAddedByUser);
        emailSaga.eventAdded(new EmailConfirmedByUser("jarjar@a.com",System.currentTimeMillis(), 2L));
        emailSaga.eventAdded(new SystemBootstrapDone(1L));

        emailSaga.eventAdded(new ReservationCancelledByUser(System.currentTimeMillis(),7L,"darth@a.com","one",1));

        verify(emailSender).sendCancellationConfirmation("darth@a.com","one");
        verify(emailSender).sendReservationConfirmation("jarjar@a.com","one",reservationAddedByUser.getReservationToken());

        verifyNoMoreInteractions(emailSender);

    }

    @Test
    public void workshopCanHaveDifferentSpaces() throws Exception {
        emailSaga.eventAdded(new WorkshopAddedByAdmin(System.currentTimeMillis(), 1L, "wsone", 3));
        ReservationAddedByUser reservationOne = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("darth@a.com")
                        .setFullname("Darth")
                        .setWorkshopId("wsone")
                        .setGoogleUserEmail(Optional.of("darth@a.com"))
                        .setNumberOfSeatsReserved(3)
                        .create()
                );
        emailSaga.eventAdded(reservationOne);
        emailSaga.eventAdded(new SystemBootstrapDone(3L));

        ReservationAddedByUser reservationTwo = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("luke@a.com")
                        .setFullname("Luke")
                        .setWorkshopId("wsone")
                        .setGoogleUserEmail(Optional.of("luke@a.com"))
                        .setNumberOfSeatsReserved(3)
                        .create()
                );
        emailSaga.eventAdded(reservationTwo);

        verify(emailSender).sendWaitingListInfo("luke@a.com", "wsone");
    }

    @Test
    public void shouldSendConfirmWithCancellationHandleSpecificSpaces() throws Exception {
        emailSaga.eventAdded(new WorkshopAddedByAdmin(System.currentTimeMillis(), 1L, "wsone", 3));
        ReservationAddedByUser reservationOne = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("darth@a.com")
                        .setFullname("Darth")
                        .setWorkshopId("wsone")
                        .setGoogleUserEmail(Optional.of("darth@a.com"))
                        .setNumberOfSeatsReserved(3)
                        .create()
                );
        emailSaga.eventAdded(reservationOne);

        ReservationAddedByUser reservationTwo = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(3L)
                        .setEmail("luke@a.com")
                        .setFullname("Luke")
                        .setWorkshopId("wsone")
                        .setGoogleUserEmail(Optional.of("luke@a.com"))
                        .setNumberOfSeatsReserved(3)
                        .create()
                );
        emailSaga.eventAdded(reservationTwo);
        emailSaga.eventAdded(new SystemBootstrapDone(4L));

        ReservationCancelledByUser cancelledOne = new ReservationCancelledByUser(System.currentTimeMillis(), 4L, "darth@a.com", "wsone",1);
        emailSaga.eventAdded(cancelledOne);

        verify(emailSender).sendCancellationConfirmation("darth@a.com", "wsone");
        verify(emailSender).sendReservationConfirmation("luke@a.com", "wsone", reservationTwo.getReservationToken());
    }

    @Test
    public void shouldBeAbleToExtendSizeOfWorkshop() {
        emailSaga.eventAdded(new SystemBootstrapDone(1L));
        emailSaga.eventAdded(new WorkshopAddedByAdmin(System.currentTimeMillis(), 1L, "wsone", 2));

        ReservationAddedByUser reservationOne = new ReservationAddedByUser(WorkshopReservation.builder()
                .setSystemTimeInMillis(System.currentTimeMillis())
                .setRevisionId(2L)
                .setEmail("luke@a.com")
                .setFullname("Luke")
                .setWorkshopId("wsone")
                .setGoogleUserEmail(Optional.of("luke@a.com"))
                .setNumberOfSeatsReserved(2)
                .create()
        );
        emailSaga.eventAdded(reservationOne);
        verify(emailSender).sendReservationConfirmation("luke@a.com", "wsone", reservationOne.getReservationToken());

        ReservationAddedByUser reservationTwo = new ReservationAddedByUser(WorkshopReservation.builder()
                .setSystemTimeInMillis(System.currentTimeMillis())
                .setRevisionId(3L)
                .setEmail("darth@a.com")
                .setFullname("Darth")
                .setWorkshopId("wsone")
                .setGoogleUserEmail(Optional.of("darth@a.com"))
                .setNumberOfSeatsReserved(1)
                .create()
        );
        emailSaga.eventAdded(reservationTwo);

        verify(emailSender).sendWaitingListInfo("darth@a.com", "wsone");

        WorkshopSizeChangedByAdmin sizeChangedByAdmin = new WorkshopSizeChangedByAdmin(System.currentTimeMillis(), 4L, "wsone", 10);
        emailSaga.eventAdded(sizeChangedByAdmin);


        verify(emailSender).sendReservationConfirmation("darth@a.com", "wsone",reservationTwo.getReservationToken());


    }

    @Test
    public void shouldSendConfirmationWhenParticallyCanceled() throws Exception {
        emailSaga.eventAdded(new WorkshopAddedByAdmin(System.currentTimeMillis(), 1L, "wsone", 3));
        ReservationAddedByUser reservationOne = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("darth@a.com")
                        .setFullname("Darth")
                        .setWorkshopId("wsone")
                        .setGoogleUserEmail(Optional.of("darth@a.com"))
                        .setNumberOfSeatsReserved(3)
                        .create()
                );
        emailSaga.eventAdded(reservationOne);

        ReservationAddedByUser reservationTwo = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(3L)
                        .setEmail("luke@a.com")
                        .setFullname("Luke")
                        .setWorkshopId("wsone")
                        .setGoogleUserEmail(Optional.of("luke@a.com"))
                        .setNumberOfSeatsReserved(1)
                        .create()
                );
        emailSaga.eventAdded(reservationTwo);
        emailSaga.eventAdded(new SystemBootstrapDone(4L));

        ReservationPartallyCancelled cancelledOne = new ReservationPartallyCancelled(System.currentTimeMillis(), 4L, "darth@a.com", "wsone",1);
        emailSaga.eventAdded(cancelledOne);

        verify(emailSender).sendReservationConfirmation("luke@a.com", "wsone", reservationTwo.getReservationToken());
        verifyNoMoreInteractions(emailSender);
    }
}
