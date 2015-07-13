package no.java.moosehead.controller;

import jdk.nashorn.internal.ir.annotations.Ignore;
import no.java.moosehead.MoosheadException;
import no.java.moosehead.aggregate.WorkshopAggregate;
import no.java.moosehead.api.ParticipantActionResult;
import no.java.moosehead.api.WorkshopInfo;
import no.java.moosehead.api.WorkshopStatus;
import no.java.moosehead.commands.AddReservationCommand;
import no.java.moosehead.commands.Author;
import no.java.moosehead.commands.CancelReservationCommand;
import no.java.moosehead.commands.ConfirmEmailCommand;
import no.java.moosehead.eventstore.EmailConfirmedByUser;
import no.java.moosehead.eventstore.ReservationAddedByUser;
import no.java.moosehead.eventstore.ReservationCancelledByUser;
import no.java.moosehead.eventstore.core.Eventstore;
import no.java.moosehead.eventstore.utils.TokenGenerator;
import no.java.moosehead.projections.Participant;
import no.java.moosehead.projections.Workshop;
import no.java.moosehead.projections.WorkshopListProjection;
import no.java.moosehead.repository.WorkshopData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class WorkshopControllerTest {

    private SystemSetup systemSetup;
    private WorkshopController workshopController;
    private WorkshopListProjection workshopListProjection;
    private WorkshopAggregate workshopAggregate;
    private Eventstore eventstore;

    @Before
    public void setUp() throws Exception {
        systemSetup = mock(SystemSetup.class);
        SystemSetup.setSetup(systemSetup);
        workshopController = new WorkshopController();
        workshopListProjection = mock(WorkshopListProjection.class);
        when(systemSetup.workshopListProjection()).thenReturn(workshopListProjection);
        workshopAggregate = mock(WorkshopAggregate.class);
        when(systemSetup.workshopAggregate()).thenReturn(workshopAggregate);
        eventstore = mock(Eventstore.class);
        when(systemSetup.eventstore()).thenReturn(eventstore);
        TokenGenerator tokenGenerator = new TokenGenerator();
        when(systemSetup.revisionGenerator()).thenReturn(tokenGenerator);
    }

    @After
    public void tearDown() throws Exception {
        SystemSetup.setSetup(null);
    }

    @Ignore
    @Test
    public void workshopShouldBeOpenBeforeStartTime() {
        //TODO Write this test, but figure out timestuff first..
//        WorkshopStatus workshopStatus = workshopController.computeWorkshopStatus(new Workshop(new WorkshopData("one", "title", "description"), 30, 3L));
  //      System.out.println(workshopStatus.toString());
    }

    @Test
    public void shouldReturnWorkshopList() throws Exception {
        when(workshopListProjection.getWorkshops()).thenReturn(Arrays.asList(new Workshop(new WorkshopData("one", "title", "description"), 30, 3L)));

        List<WorkshopInfo> workshops = workshopController.workshops();

        assertThat(workshops).hasSize(1);

        WorkshopInfo workshopInfo = workshops.get(0);

        assertThat(workshopInfo.getId()).isEqualTo("one");
        assertThat(workshopInfo.getTitle()).isEqualTo("title");
        assertThat(workshopInfo.getDescription()).isEqualTo("description");
    }

    @Test
    public void shouldHandleRegistration() throws Exception {

        ReservationAddedByUser rad = new ReservationAddedByUser(System.currentTimeMillis(),5L,"darth@deathstar.com","Darth Vader","one");
        ArgumentCaptor<AddReservationCommand> resCmndCapture = ArgumentCaptor.forClass(AddReservationCommand.class);
        when(workshopAggregate.createEvent(resCmndCapture.capture())).thenReturn(rad);

        when(workshopListProjection.isEmailConfirmed("darth@deathstar.com")).thenReturn(false);

        ParticipantActionResult result = workshopController.reservation("one", "darth@deathstar.com", "Darth Vader", Author.USER);

        assertThat(result.getStatus()).isEqualTo(ParticipantActionResult.Status.CONFIRM_EMAIL);

        verify(eventstore).addEvent(rad);
        verify(workshopAggregate).createEvent(any(AddReservationCommand.class));
        AddReservationCommand value = resCmndCapture.getValue();

        assertThat(value.getEmail()).isEqualTo("darth@deathstar.com");
        assertThat(value.getFullname()).isEqualTo("Darth Vader");
        assertThat(value.getWorkshopId()).isEqualTo("one");
    }

    @Test
    public void shouldReturnErrorIfAggregateThrowsError() throws Exception {
        doThrow(new MoosheadException("This is errormessage")).when(workshopAggregate).createEvent(any(AddReservationCommand.class));

        ParticipantActionResult result = workshopController.reservation("one", "darth@deathstar.com", "Darth Vader", Author.USER);

        assertThat(result.getStatus()).isEqualTo(ParticipantActionResult.Status.ERROR);
        assertThat(result.getErrormessage()).isEqualTo("This is errormessage");
    }

    @Test
    public void shouldHandleCancellation() throws Exception {
        ArgumentCaptor<CancelReservationCommand> cancelReservationCommandArgumentCaptor = ArgumentCaptor.forClass(CancelReservationCommand.class);

        ReservationCancelledByUser reservationCancelledByUser = new ReservationCancelledByUser(System.currentTimeMillis(),5L,"darth@deathstar.com","one");
        when(workshopAggregate.createEvent(cancelReservationCommandArgumentCaptor.capture())).thenReturn(reservationCancelledByUser);

        Participant participant = mock(Participant.class);
        when(participant.getEmail()).thenReturn("darth@deathstar.com");
        when(participant.getWorkshopId()).thenReturn("one");

        when(workshopListProjection.findByReservationId(2456L)).thenReturn(Optional.of(participant));

        ParticipantActionResult result = workshopController.cancellation("2456", Author.USER);

        assertThat(result.getStatus()).isEqualTo(ParticipantActionResult.Status.OK);

        verify(workshopAggregate).createEvent(any(CancelReservationCommand.class));

        CancelReservationCommand cancelReservationCommand = cancelReservationCommandArgumentCaptor.getValue();

        assertThat(cancelReservationCommand.getEmail()).isEqualTo("darth@deathstar.com");
        assertThat(cancelReservationCommand.getWorkshopId()).isEqualTo("one");
    }

    @Test
    public void shouldHandleCorruptCancelId() throws Exception {
        ParticipantActionResult participantActionResult = workshopController.cancellation("dfg", Author.USER);

        verify(workshopAggregate, never()).createEvent(any(CancelReservationCommand.class));
        assertThat(participantActionResult.getStatus()).isEqualTo(ParticipantActionResult.Status.ERROR);
        assertThat(participantActionResult.getErrormessage()).isEqualTo("Unknown token, reservation not found");
    }

    @Test
    public void shouldHandleNonExsistingReservationId() throws Exception {
        when(workshopListProjection.findByReservationId(anyLong())).thenReturn(Optional.empty());

        ParticipantActionResult participantActionResult = workshopController.cancellation("123", Author.USER);

        verify(workshopAggregate, never()).createEvent(any(CancelReservationCommand.class));
        assertThat(participantActionResult.getStatus()).isEqualTo(ParticipantActionResult.Status.ERROR);
        assertThat(participantActionResult.getErrormessage()).isEqualTo("Unknown token, reservation not found");
    }

    @Test
    public void shouldHandleErrorFromAggregate() throws Exception {
        doThrow(new MoosheadException("This is errormessage")).when(workshopAggregate).createEvent(any(CancelReservationCommand.class));

        Participant participant = mock(Participant.class);
        when(participant.getEmail()).thenReturn("darth@deathstar.com");
        when(participant.getWorkshopId()).thenReturn("one");

        when(workshopListProjection.findByReservationId(2456L)).thenReturn(Optional.of(participant));

        ParticipantActionResult result = workshopController.cancellation("2456", Author.USER);

        assertThat(result.getStatus()).isEqualTo(ParticipantActionResult.Status.ERROR);
        assertThat(result.getErrormessage()).isEqualTo("This is errormessage");
    }

    @Test
    public void shouldConfirmEmail() throws Exception {
        ArgumentCaptor<ConfirmEmailCommand> confirmEmailCommandArgumentCaptor = ArgumentCaptor.forClass(ConfirmEmailCommand.class);
        EmailConfirmedByUser emailConfirmedByUser = new EmailConfirmedByUser("dart@a.com",System.currentTimeMillis(),45L);
        when(workshopAggregate.createEvent(confirmEmailCommandArgumentCaptor.capture())).thenReturn(emailConfirmedByUser);

        ParticipantActionResult result = workshopController.confirmEmail("Dribbledrobbletoken");

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
