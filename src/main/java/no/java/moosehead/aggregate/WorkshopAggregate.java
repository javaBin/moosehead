package no.java.moosehead.aggregate;

import no.java.moosehead.commands.AddWorkshopCommand;
import no.java.moosehead.eventstore.AbstractEvent;
import no.java.moosehead.eventstore.EventListener;
import no.java.moosehead.eventstore.WorkshopAddedByAdmin;

import java.util.ArrayList;

public class WorkshopAggregate implements EventListener {

    private long nextRevisionId=0;
    private ArrayList<AbstractEvent> eventArrayList = new ArrayList<>();

    @Override
    public void eventAdded(AbstractEvent event) {
        eventArrayList.add(event);
    }

    public WorkshopAddedByAdmin createEvent(AddWorkshopCommand addWorkshopCommand) throws WorkshopCanNotBeAddedException{
        boolean workshopExists = eventArrayList
                .parallelStream()
                .filter(event -> event instanceof WorkshopAddedByAdmin)
                .anyMatch(event -> ((WorkshopAddedByAdmin) event).getWorkshopId().equals(addWorkshopCommand.getWorkshopId()));
        if (!workshopExists) {
            WorkshopAddedByAdmin event = new WorkshopAddedByAdmin(System.currentTimeMillis(), nextRevisionId, addWorkshopCommand.getWorkshopId());
            nextRevisionId++;
            return event;
        } else {
            throw new WorkshopCanNotBeAddedException(addWorkshopCommand.getWorkshopId() + " already exists");
        }
    }
}
