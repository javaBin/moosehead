package no.java.moosehead.controller;

import jdk.nashorn.internal.ir.annotations.Ignore;
import no.java.moosehead.MoosheadException;
import no.java.moosehead.aggregate.WorkshopAggregate;
import no.java.moosehead.api.ParticipantActionResult;
import no.java.moosehead.api.WorkshopInfo;
import no.java.moosehead.commands.AddReservationCommand;
import no.java.moosehead.eventstore.ReservationAddedByUser;
import no.java.moosehead.eventstore.core.Eventstore;
import no.java.moosehead.projections.Workshop;
import no.java.moosehead.projections.WorkshopListProjection;
import no.java.moosehead.repository.WorkshopData;
import org.fest.assertions.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

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

    }

    @After
    public void tearDown() throws Exception {
        SystemSetup.setSetup(null);
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

        ReservationAddedByUser rad = new ReservationAddedByUser(System.currentTimeMillis(),5L,"darth@deathstar.com","Darth Vader","one");
        when(workshopAggregate.createEvent(any(AddReservationCommand.class))).thenReturn(rad);

        when(workshopListProjection.isEmailConfirmed("darth@deathstar.com")).thenReturn(false);

        ParticipantActionResult result = workshopController.reservation("one", "darth@deathstar.com", "Darth Vader");

        assertThat(result.getStatus()).isEqualTo(ParticipantActionResult.Status.CONFIRM_EMAIL);

        verify(eventstore).addEvent(rad);

    }

    @Test
    public void shouldReturnErrorIfAggregateThrowsError() throws Exception {
        doThrow(new MoosheadException("This is errormessage")).when(workshopAggregate).createEvent(any(AddReservationCommand.class));

        ParticipantActionResult result = workshopController.reservation("one", "darth@deathstar.com", "Darth Vader");

        assertThat(result.getStatus()).isEqualTo(ParticipantActionResult.Status.ERROR);
        assertThat(result.getErrormessage()).isEqualTo("This is errormessage");

    }
}
