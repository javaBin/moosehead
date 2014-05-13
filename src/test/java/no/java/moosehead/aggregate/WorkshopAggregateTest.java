package no.java.moosehead.aggregate;

import no.java.moosehead.commands.AddReservationCommand;
import no.java.moosehead.commands.AddWorkshopCommand;
import no.java.moosehead.eventstore.Eventstore;
import no.java.moosehead.eventstore.ReservationAddedByUser;
import no.java.moosehead.eventstore.WorkshopAddedByAdmin;
import org.junit.*;

import static org.fest.assertions.Assertions.assertThat;

public class WorkshopAggregateTest {

    private final String w2 = "W2";
    private final String w1 = "W1";

    private Eventstore eventstore;
    private WorkshopAggregate workshopAggregate;

    @Before
    public void beforeTest() {
        eventstore = new Eventstore();
        workshopAggregate = new WorkshopAggregate();
        eventstore.addEventListener(workshopAggregate);
    }

    @Test(expected = WorkshopCanNotBeAddedException.class)
    public void workshopShouldNotBeAddedWhenItExistsAlready() {
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, w1, 0));
        AddWorkshopCommand command = new AddWorkshopCommand(w1);
        workshopAggregate.createEvent(command);
    }

    @Test
    public void aUniqueWorkshopShouldBeAdded() {
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, w1, 0));
        AddWorkshopCommand command = new AddWorkshopCommand(w2);
        WorkshopAddedByAdmin event = workshopAggregate.createEvent(command);
        assertThat(event.getWorkshopId()).isEqualTo(w2);
    }

    @Test(expected = ReservationCanNotBeAddedException.class)
    public void ReservationIsNotOkWhenWorkshopDoesNotExists() {
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, w1, 0));
        AddReservationCommand cmd = new AddReservationCommand("bla@email","Donnie Darko",w2);
        workshopAggregate.createEvent(cmd);
    }

    @Test
    public void ReservationIsOkWhenWorkshopExists() {
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, w1, 0));
        AddReservationCommand cmd = new AddReservationCommand("bla@email","Donnie Darko",w1);
        ReservationAddedByUser reservationAddedByUser =  workshopAggregate.createEvent(cmd);
        assertThat(reservationAddedByUser.getWorkshopId()).isEqualTo(w1);
    }
}
