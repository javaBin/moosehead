package no.java.moosehead.aggregate;

import no.java.moosehead.eventstore.AbstractEvent;
import no.java.moosehead.eventstore.EventListener;

public class WorkshopAggregate implements EventListener {

    @Override
    public void eventAdded(AbstractEvent event) {

    }

    public boolean canWorkshopBeAdded(AddWorkshopCommand addWorkshopCommand) {
        return true;
    }
}
