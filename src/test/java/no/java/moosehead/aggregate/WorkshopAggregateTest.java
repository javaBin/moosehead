package no.java.moosehead.aggregate;

import no.java.moosehead.eventstore.Eventstore;
import no.java.moosehead.eventstore.WorkshopAddedByAdmin;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class WorkshopAggregateTest {
    @Test
    public void testIfWorkshopCantBeAddedWhenItExistsAlready() throws Exception {
        Eventstore eventstore = new Eventstore();
        WorkshopAggregate workshopAggregate = new WorkshopAggregate();
        eventstore.addEventListener(workshopAggregate);
        eventstore.addEvent(new WorkshopAddedByAdmin(System.currentTimeMillis()));
        AddWorkshopCommand command = new AddWorkshopCommand();
        assertThat(workshopAggregate.canWorkshopBeAdded(command)).isFalse();
    }
}
