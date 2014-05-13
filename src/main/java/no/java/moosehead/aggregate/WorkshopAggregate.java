package no.java.moosehead.aggregate;

import no.java.moosehead.commands.AddReservationCommand;
import no.java.moosehead.commands.AddWorkshopCommand;
import no.java.moosehead.eventstore.AbstractEvent;
import no.java.moosehead.eventstore.EventListener;
import no.java.moosehead.eventstore.ReservationAddedByUser;
import no.java.moosehead.eventstore.WorkshopAddedByAdmin;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

public class WorkshopAggregate implements EventListener {

    private long nextRevisionId=0;
    private ArrayList<AbstractEvent> eventArrayList = new ArrayList<>();

    @Override
    public void eventAdded(AbstractEvent event) {
        eventArrayList.add(event);
    }

    public WorkshopAddedByAdmin createEvent(AddWorkshopCommand addWorkshopCommand){
        Optional<WorkshopAddedByAdmin> workshop = getWorkshop(addWorkshopCommand.getWorkshopId());
        if (!workshop.isPresent()) {
            WorkshopAddedByAdmin event = new WorkshopAddedByAdmin(System.currentTimeMillis(), nextRevisionId, addWorkshopCommand.getWorkshopId(), 0);
            nextRevisionId++;
            return event;
        } else {
            throw new WorkshopCanNotBeAddedException(addWorkshopCommand.getWorkshopId() + " already exists");
        }
    }

    public ReservationAddedByUser createEvent(AddReservationCommand addReservationCommand) {
        Optional<WorkshopAddedByAdmin> workshop = getWorkshop(addReservationCommand.getWorkshopId());
        if (workshop.isPresent()) {
            return new ReservationAddedByUser(System.currentTimeMillis(), nextRevisionId, "TEST@SOMEWHERE", "JONAS TESTESEN", addReservationCommand.getWorkshopId());
        } else {
            throw new ReservationCanNotBeAddedException(addReservationCommand.getWorkshopId() + " does not exists");
        }
    }

    private Stream<WorkshopAddedByAdmin> getAllWorkshops() {
       return eventArrayList
               .parallelStream()
               .filter(event -> event instanceof WorkshopAddedByAdmin)
               .map(event ->(WorkshopAddedByAdmin) event);
    }

    private Optional<WorkshopAddedByAdmin> getWorkshop(String workshopId) {
        return getAllWorkshops()
                .filter(workshop -> workshop.getWorkshopId().equals(workshopId))
                .findFirst();
    }
}
