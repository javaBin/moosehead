package no.java.moosehead.controller;

import no.java.moosehead.MoosheadException;
import no.java.moosehead.aggregate.WorkshopAggregate;
import no.java.moosehead.api.ParticipantActionResult;
import no.java.moosehead.api.WorkshopInfo;
import no.java.moosehead.api.WorkshopStatus;
import no.java.moosehead.commands.*;
import no.java.moosehead.domain.WorkshopReservation;
import no.java.moosehead.eventstore.EmailConfirmedByUser;
import no.java.moosehead.eventstore.ReservationAddedByUser;
import no.java.moosehead.eventstore.ReservationCancelledByUser;
import no.java.moosehead.eventstore.core.FilehandlerEventstore;
import no.java.moosehead.eventstore.utils.TokenGenerator;
import no.java.moosehead.projections.Participant;
import no.java.moosehead.projections.Workshop;
import no.java.moosehead.projections.WorkshopListProjection;
import no.java.moosehead.repository.WorkshopData;
import no.java.moosehead.web.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class WorkshopControllerTest {

    private SystemSetup systemSetup;
    private WorkshopController workshopController;
    private WorkshopListProjection workshopListProjection;
    private WorkshopAggregate workshopAggregate;
    private FilehandlerEventstore eventstore;
    private final String w1 = "W1";

    @Before
    public void setUp() throws Exception {
        systemSetup = mock(SystemSetup.class);
        SystemSetup.setSetup(systemSetup);
        workshopController = new WorkshopController();
        workshopListProjection = mock(WorkshopListProjection.class);
        when(systemSetup.workshopListProjection()).thenReturn(workshopListProjection);
        workshopAggregate = mock(WorkshopAggregate.class);
        when(systemSetup.workshopAggregate()).thenReturn(workshopAggregate);
        eventstore = mock(FilehandlerEventstore.class);
        when(systemSetup.eventstore()).thenReturn(eventstore);
        TokenGenerator tokenGenerator = new TokenGenerator();
        when(systemSetup.revisionGenerator()).thenReturn(tokenGenerator);
    }

    @After
    public void tearDown() throws Exception {
        SystemSetup.setSetup(null);
    }

    @Test
    public void shouldCalculateCorrectStatusFewSpots() {
        ReservationAddedByUser r1 = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(1L)
                        .setEmail("bal@gmail.com")
                        .setFullname("Darth Vader")
                        .setWorkshopId(w1)
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(20)
                        .create()
                );
        ReservationAddedByUser r2 = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("laban@gmail.com")
                        .setFullname("Darth Laber")
                        .setWorkshopId(w1)
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(9)
                        .create()
                );
        Workshop ws = new Workshop(new WorkshopData("one", "title", "description"), 30);
        ws.addParticipant(Participant.confirmedParticipant(r1,ws));
        ws.addParticipant(Participant.confirmedParticipant(r2,ws));
        WorkshopStatus workshopStatus = workshopController.computeWorkshopStatus(ws);
        assertThat(workshopStatus).isEqualTo(WorkshopStatus.FEW_SPOTS);
    }

    @Test
    public void shouldCalculateCorrectStatusFull() {
        ReservationAddedByUser r1 = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(1L)
                        .setEmail("bal@gmail.com")
                        .setFullname("Darth Vader")
                        .setWorkshopId(w1)
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(20)
                        .create()
                );
        ReservationAddedByUser r2 = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("laban@gmail.com")
                        .setFullname("Darth Laber")
                        .setWorkshopId(w1)
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(10)
                        .create()
                );
        Workshop ws = new Workshop(new WorkshopData("one", "title", "description"), 30);
        ws.addParticipant(Participant.confirmedParticipant(r1,ws));
        ws.addParticipant(Participant.confirmedParticipant(r2,ws));
        WorkshopStatus workshopStatus = workshopController.computeWorkshopStatus(ws);
        assertThat(workshopStatus).isEqualTo(WorkshopStatus.FULL);
    }

    @Test
    public void shouldOverrideOpeningDate() throws Exception {
        Instant before = Instant.now().minusSeconds(60 * 30);

        String later = OffsetDateTime.now().plusMinutes(30).format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        Map<String, String> confdata = new HashMap<>();
        confdata.put("openTime", later);
        Configuration.setConfdata(confdata);


        WorkshopData workshopData = new WorkshopData(
                "one",
                "title",
                "description",
                OffsetDateTime.now().plusDays(5).toInstant(),
                OffsetDateTime.now().plusDays(6).toInstant(),
                Optional.of(before),
                WorkshopTypeEnum.KIDSAKODER_WORKSHOP);
        Workshop ws = new Workshop(workshopData, 30);

        WorkshopStatus workshopStatus = workshopController.computeWorkshopStatus(ws);

        Configuration.setConfdata(null);

        assertThat(workshopStatus).isEqualTo(WorkshopStatus.FREE_SPOTS);
    }

    @Test
    public void shouldReturnWorkshopList() throws Exception {
        when(workshopListProjection.getWorkshops()).thenReturn(Arrays.asList(new Workshop(new WorkshopData("one", "title", "description"), 30)));

        List<WorkshopInfo> workshops = workshopController.workshops();

        assertThat(workshops).hasSize(1);

        WorkshopInfo workshopInfo = workshops.get(0);

        assertThat(workshopInfo.getId()).isEqualTo("one");
        assertThat(workshopInfo.getTitle()).isEqualTo("title");
        assertThat(workshopInfo.getDescription()).isEqualTo("description");
    }

    @Test
    public void shouldHandleRegistration() throws Exception {

        ReservationAddedByUser rad = new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(5L)
                        .setEmail("darth@deathstar.com")
                        .setFullname("Darth Vader")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                );
        ArgumentCaptor<AddReservationCommand> resCmndCapture = ArgumentCaptor.forClass(AddReservationCommand.class);
        when(workshopAggregate.createEvent(resCmndCapture.capture())).thenReturn(rad);

        when(workshopListProjection.isEmailConfirmed("darth@deathstar.com")).thenReturn(false);

        WorkshopReservation workshopReservation = WorkshopReservation.builder()
                .setWorkshopId("one")
                .setEmail("darth@deathstar.com")
                .setFullname("Darth Vader")
                .create();
        ParticipantActionResult result = workshopController.reservation(workshopReservation,AuthorEnum.USER);

        assertThat(result.getStatus()).isEqualTo(ParticipantActionResult.Status.CONFIRM_EMAIL);

        verify(eventstore).addEvent(rad);
        verify(workshopAggregate).createEvent(any(AddReservationCommand.class));
        AddReservationCommand value = resCmndCapture.getValue();

        assertThat(value.getWorkshopReservation().getEmail()).isEqualTo("darth@deathstar.com");
        assertThat(value.getWorkshopReservation().getFullname()).isEqualTo("Darth Vader");
        assertThat(value.getWorkshopReservation().getWorkshopId()).isEqualTo("one");
    }

    @Test
    public void shouldReturnErrorIfAggregateThrowsError() throws Exception {
        doThrow(new MoosheadException("This is errormessage")).when(workshopAggregate).createEvent(any(AddReservationCommand.class));

        WorkshopReservation workshopReservation = WorkshopReservation.builder()
                .setWorkshopId("one")
                .setEmail("darth@deathstar.com")
                .setFullname("Darth Vader")
                .create();
        ParticipantActionResult result = workshopController.reservation(workshopReservation,AuthorEnum.USER);

        assertThat(result.getStatus()).isEqualTo(ParticipantActionResult.Status.ERROR);
        assertThat(result.getErrormessage()).isEqualTo("This is errormessage");
    }

    @Test
    public void shouldHandleCancellation() throws Exception {
        ArgumentCaptor<CancelReservationCommand> cancelReservationCommandArgumentCaptor = ArgumentCaptor.forClass(CancelReservationCommand.class);

        ReservationCancelledByUser reservationCancelledByUser = new ReservationCancelledByUser(System.currentTimeMillis(),5L,"darth@deathstar.com","one",1);
        when(workshopAggregate.createEvent(cancelReservationCommandArgumentCaptor.capture())).thenReturn(reservationCancelledByUser);

        Participant participant = mock(Participant.class);
        when(participant.getWorkshopReservation()).thenReturn(WorkshopReservation.builder().setEmail("darth@deathstar.com").create());
        when(participant.getWorkshopId()).thenReturn("one");

        when(workshopListProjection.findByReservationToken("ReservasjonsToken")).thenReturn(Optional.of(participant));

        ParticipantActionResult result = workshopController.cancellation("ReservasjonsToken", AuthorEnum.USER);

        assertThat(result.getStatus()).isEqualTo(ParticipantActionResult.Status.OK);

        verify(workshopAggregate).createEvent(any(CancelReservationCommand.class));

        CancelReservationCommand cancelReservationCommand = cancelReservationCommandArgumentCaptor.getValue();

        assertThat(cancelReservationCommand.getEmail()).isEqualTo("darth@deathstar.com");
        assertThat(cancelReservationCommand.getWorkshopId()).isEqualTo("one");
    }

    //TODO: Finne ut hvorfor i allverden denne testen feiler.. findAny og findFirst gir null og ikke en Optional??
    @Ignore
    @Test
    public void shouldHandleCorruptCancelId() throws Exception {
        ParticipantActionResult participantActionResult = workshopController.cancellation("dfg", AuthorEnum.USER);

        verify(workshopAggregate, never()).createEvent(any(CancelReservationCommand.class));
        assertThat(participantActionResult.getStatus()).isEqualTo(ParticipantActionResult.Status.ERROR);
        assertThat(participantActionResult.getErrormessage()).isEqualTo("Unknown token, reservation not found");
    }

    @Test
    public void shouldHandleNonExsistingReservationId() throws Exception {
        when(workshopListProjection.findByReservationToken(anyString())).thenReturn(Optional.empty());

        ParticipantActionResult participantActionResult = workshopController.cancellation("ReservasjonsToken", AuthorEnum.USER);

        verify(workshopAggregate, never()).createEvent(any(CancelReservationCommand.class));
        assertThat(participantActionResult.getStatus()).isEqualTo(ParticipantActionResult.Status.ERROR);
        assertThat(participantActionResult.getErrormessage()).isEqualTo("Unknown token, reservation not found");
    }

    @Test
    public void shouldHandleErrorFromAggregate() throws Exception {
        doThrow(new MoosheadException("This is errormessage")).when(workshopAggregate).createEvent(any(CancelReservationCommand.class));

        Participant participant = mock(Participant.class);
        when(participant.getWorkshopReservation()).thenReturn(WorkshopReservation.builder().setEmail("darth@deathstar.com").create());
        when(participant.getWorkshopId()).thenReturn("one");

        when(workshopListProjection.findByReservationToken("ReservasjonsToken")).thenReturn(Optional.of(participant));

        ParticipantActionResult result = workshopController.cancellation("ReservasjonsToken", AuthorEnum.USER);

        assertThat(result.getStatus()).isEqualTo(ParticipantActionResult.Status.ERROR);
        assertThat(result.getErrormessage()).isEqualTo("This is errormessage");
    }

    @Test
    public void shouldConfirmEmail() throws Exception {
        ArgumentCaptor<ConfirmEmailCommand> confirmEmailCommandArgumentCaptor = ArgumentCaptor.forClass(ConfirmEmailCommand.class);
        EmailConfirmedByUser emailConfirmedByUser = new EmailConfirmedByUser("dart@a.com",System.currentTimeMillis(),45L);
        when(workshopAggregate.createEvent(confirmEmailCommandArgumentCaptor.capture())).thenReturn(emailConfirmedByUser);
        Workshop workshop = mock(Workshop.class);
        Participant participant = mock(Participant.class);
        when(participant.getWorkshopReservation()).thenReturn(WorkshopReservation.builder().setReservationToken("Dribbledrobbletoken").create());
        when(workshop.getParticipants()).thenReturn(Collections.singletonList(participant));
        when(workshop.waitingListNumber(participant)).thenReturn(0);
        WorkshopData wsdata = mock(WorkshopData.class);
        when(wsdata.infoText()).thenReturn("xrtyr");
        when(workshop.getWorkshopData()).thenReturn(wsdata);
        when(workshopListProjection.getWorkshops()).thenReturn(Collections.singletonList(workshop));

        ParticipantActionResult result = workshopController.confirmEmail("Dribbledrobbletoken");

        verify(workshop).waitingListNumber(participant);

        assertThat(result.getStatus()).isEqualTo(ParticipantActionResult.Status.OK);
        verify(workshopAggregate).createEvent(any(ConfirmEmailCommand.class));

        ConfirmEmailCommand value = confirmEmailCommandArgumentCaptor.getValue();
        assertThat(value.getReservationToken()).isEqualTo("Dribbledrobbletoken");

        verify(eventstore).addEvent(emailConfirmedByUser);
    }

    @Test
    public void shouldHandleUnknownEmailToken() throws Exception {
        doThrow(new MoosheadException("My error")).when(workshopAggregate).createEvent(any(ConfirmEmailCommand.class));

        ParticipantActionResult result = workshopController.confirmEmail("123");
        assertThat(result.getStatus()).isEqualTo(ParticipantActionResult.Status.ERROR);
        assertThat(result.getErrormessage()).isEqualTo("My error");
    }
}
