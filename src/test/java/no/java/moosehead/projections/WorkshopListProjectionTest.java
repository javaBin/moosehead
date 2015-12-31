package no.java.moosehead.projections;

import no.java.moosehead.commands.WorkshopTypeEnum;
import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.domain.WorkshopReservation;
import no.java.moosehead.eventstore.*;
import no.java.moosehead.repository.WorkshopData;
import no.java.moosehead.repository.WorkshopRepository;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class WorkshopListProjectionTest {
    private WorkshopRepository workshopRepository = mock(WorkshopRepository.class);

    @Test
    public void shouldReturnAddedWorkshop() throws Exception {
        WorkshopListProjection workshopListProjection = setupOneWorkshop();

        List<Workshop> workshops = workshopListProjection.getWorkshops();

        assertThat(workshops).hasSize(1);

        WorkshopData workshopData = workshops.get(0).getWorkshopData();

        assertThat(workshopData.getId()).isEqualTo("one");
        assertThat(workshops.get(0).getNumberOfSeats()).isEqualTo(30);


    }

    private WorkshopListProjection setupOneWorkshop() {
        Optional<WorkshopData> optworkshop = Optional.of(new WorkshopData("one", "title", "description"));

        when(workshopRepository.workshopById("one")).thenReturn(optworkshop);


        WorkshopListProjection workshopListProjection = new WorkshopListProjection();
        workshopListProjection.eventAdded(new WorkshopAddedBySystem(System.currentTimeMillis(),1L,"one",30));
        return workshopListProjection;
    }

    @Before
    public void setup() {
        SystemSetup systemSetup = mock(SystemSetup.class);
        when(systemSetup.workshopRepository()).thenReturn(workshopRepository);
        SystemSetup.setSetup(systemSetup);
    }

    @Test
    public void shouldShowParticipants() throws Exception {
        WorkshopListProjection workshopListProjection = setupOneWorkshop();

        workshopListProjection.eventAdded(new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("a@a.com")
                        .setFullname("Darth Vader")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                ));

        List<Participant> participants = workshopListProjection.getWorkshops().get(0).getParticipants();
        assertThat(participants).hasSize(1);
        Participant participant = participants.get(0);
        assertThat(participant.getWorkshopReservation().getEmail()).isEqualToIgnoringCase("a@a.com");
        assertThat(participant.isEmailConfirmed()).isFalse();

    }

    @Test
    public void shouldHandleCancellations() throws Exception {
        WorkshopListProjection workshopListProjection = setupOneWorkshop();

        workshopListProjection.eventAdded(new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("a@a.com")
                        .setFullname("Darth Vader")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                ));
        workshopListProjection.eventAdded(new ReservationCancelledByUser(System.currentTimeMillis(),3L,"a@a.com","one",1));

        assertThat(workshopListProjection.getWorkshops().get(0).getParticipants()).isEmpty();
    }

    @Test
    public void shoulHandlePartialReservations() throws Exception {
        Optional<WorkshopData> optworkshop = Optional.of(new WorkshopData("one", "title", "description",null,null,Optional.<Instant>empty(), WorkshopTypeEnum.KIDSAKODER_WORKSHOP));

        when(workshopRepository.workshopById("one")).thenReturn(optworkshop);


        WorkshopListProjection workshopListProjection = new WorkshopListProjection();
        workshopListProjection.eventAdded(new WorkshopAddedByAdmin(System.currentTimeMillis(), 1L, "one", 30, null, null, optworkshop.get()));

        workshopListProjection.eventAdded(new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("a@a.com")
                        .setFullname("Darth Vader")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.of("a@a.com"))
                        .setNumberOfSeatsReserved(2)
                        .create()
                ));
        workshopListProjection.eventAdded(new ReservationCancelledByUser(System.currentTimeMillis(), 3L, "a@a.com", "one", 1));

        List<Participant> allReservations = workshopListProjection.findAllReservations("a@a.com");

        assertThat(allReservations).hasSize(1);

        assertThat(allReservations.get(0).getNumberOfSeatsReserved()).isEqualTo(1);


    }

    @Test
    public void shouldGiveConfirmedEmailStatus() throws Exception {
        WorkshopListProjection workshopListProjection = setupOneWorkshop();

        workshopListProjection.eventAdded(new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("a@a.com")
                        .setFullname("Darth Vader")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                ));
        workshopListProjection.eventAdded(new EmailConfirmedByUser("a@a.com",System.currentTimeMillis(),5L));

        Participant participant = workshopListProjection.getWorkshops().get(0).getParticipants().get(0);

        assertThat(participant.isEmailConfirmed()).isTrue();
    }

    @Test
    public void queueShouldBeOrderedAccordingToConfirmTime() throws Exception {
        WorkshopListProjection workshopListProjection = setupOneWorkshop();

        workshopListProjection.eventAdded(new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("a@a.com")
                        .setFullname("Darth Vader")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                ));
        workshopListProjection.eventAdded(new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(3L)
                        .setEmail("b@a.com")
                        .setFullname("Darth Maul")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.empty())
                        .setNumberOfSeatsReserved(1)
                        .create()
                ));
        workshopListProjection.eventAdded(new EmailConfirmedByUser("b@a.com",System.currentTimeMillis(),4L));
        workshopListProjection.eventAdded(new EmailConfirmedByUser("a@a.com",System.currentTimeMillis(),5L));

        List<Participant> participants = workshopListProjection.getWorkshops().get(0).getParticipants();
        assertThat(participants).hasSize(2);
        assertThat(participants.get(0).getWorkshopReservation().getEmail()).isEqualTo("b@a.com");
        assertThat(participants.get(1).getWorkshopReservation().getEmail()).isEqualTo("a@a.com");

    }

    @Test
    public void shouldConfirmEmailWhenLoggedInWithGoogle() throws Exception {
        WorkshopListProjection workshopListProjection = setupOneWorkshop();
        workshopListProjection.eventAdded(new ReservationAddedByUser(WorkshopReservation.builder()
                        .setSystemTimeInMillis(System.currentTimeMillis())
                        .setRevisionId(2L)
                        .setEmail("a@a.com")
                        .setFullname("Darth Vader")
                        .setWorkshopId("one")
                        .setGoogleUserEmail(Optional.of("a@a.com"))
                        .setNumberOfSeatsReserved(1)
                        .create()
                ));
        assertThat(workshopListProjection.isEmailConfirmed("a@a.com")).isTrue();

    }

    @Test
    public void shouldNotUseRepositoryOnEventWithData() throws Exception {
        WorkshopListProjection workshopListProjection = new WorkshopListProjection();
        WorkshopData workshopData = new WorkshopData("one", "title", "description");
        WorkshopAddedByAdmin one = new WorkshopAddedByAdmin(System.currentTimeMillis(), 1L, "one", 30,null,null,workshopData);
        workshopListProjection.eventAdded(one);

        verify(workshopRepository,never()).workshopById(anyString());

        List<Workshop> workshops = workshopListProjection.getWorkshops();

        assertThat(workshops).hasSize(1);

        assertThat(workshops.get(0).getWorkshopData()).isEqualTo(workshopData);


    }
}
