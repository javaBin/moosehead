package no.java.moosehead.aggregate;

import no.java.moosehead.commands.AddWorkshopCommand;
import no.java.moosehead.eventstore.Eventstore;
import no.java.moosehead.eventstore.WorkshopAddedByAdmin;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class WorkshopAggregateTest {

    private final String w2 = "W2";
    private final String w1 = "W1";

    @Test(expected = WorkshopCanNotBeAddedException.class)
    public void WorkshopShouldNotBeAddedWhenItExistsAlready() throws Exception {
        Eventstore eventstore = new Eventstore();
        WorkshopAggregate workshopAggregate = new WorkshopAggregate();
        eventstore.addEventListener(workshopAggregate);
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, w1));
        AddWorkshopCommand command = new AddWorkshopCommand(w1);
        //Should throw WorkshopCanNotBeAddedException
        WorkshopAddedByAdmin event = workshopAggregate.createEvent(command);
    }

    @Test()
    public void AUniqueWorkshopShouldBeAdded() throws Exception {
        Eventstore eventstore = new Eventstore();
        WorkshopAggregate workshopAggregate = new WorkshopAggregate();
        eventstore.addEventListener(workshopAggregate);
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis(),1L, w1));
        AddWorkshopCommand command = new AddWorkshopCommand(w2);
        WorkshopAddedByAdmin event = workshopAggregate.createEvent(command);
        assertThat(event.getWorkshopId()).isEqualTo(w2);
    }
}
