package no.java.moosehead.aggregate;

import no.java.moosehead.commands.AddReservationCommand;
import no.java.moosehead.commands.AddWorkshopCommand;
import no.java.moosehead.commands.CancelReservationCommand;
import no.java.moosehead.eventstore.*;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

public class WorkshopAggregate implements EventSubscription {

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
            throw new WorkshopCanNotBeAddedException("The workshop in [" + addWorkshopCommand + "] already exists");
        }
    }

    public ReservationAddedByUser createEvent(AddReservationCommand addReservationCommand) {
        Optional<WorkshopAddedByAdmin> workshop = getWorkshop(addReservationCommand.getWorkshopId());
        if (workshop.isPresent()) {
            ReservationAddedByUser reservationAddedByUser = new ReservationAddedByUser(System.currentTimeMillis(), nextRevisionId, addReservationCommand.getEmail(),
                    addReservationCommand.getFullname(), addReservationCommand.getWorkshopId());
            if (getReservation(reservationAddedByUser).isPresent()) {
                throw new ReservationCanNotBeAddedException("A reservation already exsists for [" + reservationAddedByUser + "]");
            }
            return reservationAddedByUser;
        } else {
            throw new ReservationCanNotBeAddedException("The workshop in [" + addReservationCommand + "] does not exists");
        }
    }

    public ReservationCancelledByUser createEvent(CancelReservationCommand cancel) {
        if (userWorkshopEvents(cancel.getWorkshopId(),cancel.getEmail()).count() % 2 == 0) {
            throw new NoReservationFoundException(String.format("The reservation for %s in %s not found",cancel.getEmail(),cancel.getWorkshopId()));
        }
        ReservationCancelledByUser reservationCancelledByUser = new ReservationCancelledByUser(System.currentTimeMillis(), nextRevisionId, cancel.getEmail(), cancel.getWorkshopId());
        return reservationCancelledByUser;
    }

    private Stream<? extends UserWorkshopEvent> userWorkshopEvents(String workshopid,String email) {
        return eventArrayList
                .stream()
                .filter(event -> event instanceof UserWorkshopEvent)
                .map(event -> (UserWorkshopEvent) event)
                .filter(uwe -> uwe.getEmail().equals(email) && uwe.getWorkshopId().equals(workshopid));
    }

    private Stream<WorkshopAddedByAdmin> getAllWorkshops() {
       return eventArrayList
               .parallelStream()
               .filter(event -> event instanceof WorkshopAddedByAdmin)
               .map(event ->(WorkshopAddedByAdmin) event);
    }

    private Stream<ReservationAddedByUser> getReservationsForWorkshop(String workshopId) {
        return eventArrayList
                .parallelStream()
                .filter(event -> event instanceof ReservationAddedByUser)
                .map(event -> (ReservationAddedByUser) event)
                .filter(reservation -> reservation.getWorkshopId().equals(workshopId));
    }

    private Optional<WorkshopAddedByAdmin> getWorkshop(String workshopId) {
        return getAllWorkshops()
                .filter(workshop -> workshop.getWorkshopId().equals(workshopId))
                .findFirst();
    }

    private Optional<ReservationAddedByUser> getReservation(ReservationAddedByUser reservationAddedByUser) {
        return getReservationsForWorkshop(reservationAddedByUser.getWorkshopId())
                .filter(reservation -> reservation.getEmail().equals(reservationAddedByUser.getEmail()))
                .findFirst();
    }

}
