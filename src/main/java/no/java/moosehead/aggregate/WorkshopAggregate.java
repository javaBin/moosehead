package no.java.moosehead.aggregate;

import no.java.moosehead.commands.*;
import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.*;
import no.java.moosehead.eventstore.core.AbstractEvent;
import no.java.moosehead.eventstore.core.EventSubscription;
import no.java.moosehead.web.Configuration;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

public class WorkshopAggregate implements EventSubscription {

    private ArrayList<AbstractEvent> eventArrayList = new ArrayList<>();

    @Override
    public void eventAdded(AbstractEvent event) {
        eventArrayList.add(event);
    }

    public WorkshopAddedEvent createEvent(AddWorkshopCommand addWorkshopCommand){
        Optional<WorkshopAddedEvent> workshop = getWorkshop(addWorkshopCommand.getWorkshopId());
        if (!workshop.isPresent()) {
            switch (addWorkshopCommand.getAuthorEnum()) {
                case SYSTEM:
                    if (addWorkshopCommand.hasStartAndEndTime()) {
                        return new WorkshopAddedBySystem(System.currentTimeMillis(), nextRevision(), addWorkshopCommand.getWorkshopId(), addWorkshopCommand.getNumberOfSeats(), addWorkshopCommand.getStartTime(), addWorkshopCommand.getEndTime());
                    }   else {
                        return new WorkshopAddedBySystem(System.currentTimeMillis(), nextRevision(), addWorkshopCommand.getWorkshopId(), addWorkshopCommand.getNumberOfSeats());
                    }
                case ADMIN:
                    if (!addWorkshopCommand.getWorkshopData().isPresent()) {
                        throw new WorkshopCanNotBeAddedException("Need WorkshopData for workshop added by admin");
                    }
                    switch (addWorkshopCommand.getWorkshopTypeEnum()) {
                        case NORMAL_WORKSHOP:
                            return new WorkshopAddedByAdmin(System.currentTimeMillis(),
                                    nextRevision(),
                                    addWorkshopCommand.getWorkshopId(),
                                    addWorkshopCommand.getNumberOfSeats(),
                                    addWorkshopCommand.getStartTime(),
                                    addWorkshopCommand.getEndTime(),
                                    addWorkshopCommand.getWorkshopData().get());
                        case KIDSAKODER_WORKSHOP:
                            return new KidsaKoderWorkshopAddedByAdmin(System.currentTimeMillis(),
                                    nextRevision(),
                                    addWorkshopCommand.getWorkshopId(),
                                    addWorkshopCommand.getNumberOfSeats(),
                                    addWorkshopCommand.getStartTime(),
                                    addWorkshopCommand.getEndTime(),
                                    addWorkshopCommand.getWorkshopData().get());
                    }
                    default:
                        throw new WorkshopCanNotBeAddedException("Workshop cannot be added", new IllegalArgumentException("AuthorEnum + " + AuthorEnum.USER+ " is not supported"));
            }
        } else {
            throw new WorkshopCanNotBeAddedException("The workshop in [" + addWorkshopCommand + "] already exists");
        }
    }

    private long nextRevision() {
        return SystemSetup.instance().revisionGenerator().nextRevisionId();
    }

    public AbstractReservationAdded createEvent(AddReservationCommand addReservationCommand) {
        Optional<WorkshopAddedEvent> workshop = getWorkshop(addReservationCommand.getWorkshopId());
        if (workshop.isPresent()) {
            if (OffsetDateTime.now().isBefore(Configuration.openTime())) {
                throw new ReservationCanNotBeAddedException("Reservations has not opened yet for this workshop");
            }
            if (getReservation(addReservationCommand).isPresent()) {
                throw new ReservationCanNotBeAddedException("A reservation already exsists for [" + addReservationCommand.getEmail() + "]");
            }
            switch (addReservationCommand.getAuthorEnum()) {
                case ADMIN:
                    return new ReservationAddedByAdmin(System.currentTimeMillis(), nextRevision(), addReservationCommand.getEmail(),
                            addReservationCommand.getFullname(), addReservationCommand.getWorkshopId(), addReservationCommand.getNumberOfSeatsReserved());
                case USER:
                    return new ReservationAddedByUser(System.currentTimeMillis(), nextRevision(), addReservationCommand.getEmail(),
                            addReservationCommand.getFullname(), addReservationCommand.getWorkshopId(),addReservationCommand.getGoogleEmail(), addReservationCommand.getNumberOfSeatsReserved());
                default:
                    throw new ReservationCanNotBeAddedException("Reservation cannot be added", new IllegalArgumentException("AuthorEnum + " + AuthorEnum.SYSTEM + " is not supported"));
            }
        } else {
            throw new ReservationCanNotBeAddedException("The workshop in [" + addReservationCommand + "] does not exists");
        }
    }

    public AbstractReservationCancelled createEvent(CancelReservationCommand cancelReservationCommand) {
        long count = userWorkshopEvents(cancelReservationCommand.getWorkshopId(), cancelReservationCommand.getEmail()).count();
        if (count % 2 == 0) {
            throw new NoReservationFoundException(String.format("The reservation for %s in %s not found",cancelReservationCommand.getEmail(),cancelReservationCommand.getWorkshopId()));
        }
        switch (cancelReservationCommand.getAuthorEnum()) {
            case USER:
                return new ReservationCancelledByUser(System.currentTimeMillis(), nextRevision(), cancelReservationCommand.getEmail(), cancelReservationCommand.getWorkshopId());
            case ADMIN:
                return new ReservationCancelledByAdmin(System.currentTimeMillis(), nextRevision(), cancelReservationCommand.getEmail(), cancelReservationCommand.getWorkshopId());
            default:
                throw new ReservationCanNotBeCanceledException("Reservation cannot be canceled", new IllegalArgumentException("AuthorEnum + " + AuthorEnum.SYSTEM + " is not supported"));
        }
    }

    private Stream<? extends UserWorkshopEvent> userWorkshopEvents(String workshopid,String email) {
        return eventArrayList
                .stream()
                .filter(event -> event instanceof UserWorkshopEvent)
                .map(event -> (UserWorkshopEvent) event)
                .filter(uwe -> uwe.getEmail().equals(email) && uwe.getWorkshopId().equals(workshopid));
    }

    private Stream<WorkshopAddedEvent> getAllWorkshops() {
       return eventArrayList
               .parallelStream()
               .filter(event -> event instanceof WorkshopAddedEvent)
               .map(event ->(WorkshopAddedEvent) event);
    }

    private Stream<ReservationAddedByUser> getReservationsForWorkshop(String workshopId) {
        return eventArrayList
                .parallelStream()
                .filter(event -> event instanceof ReservationAddedByUser)
                .map(event -> (ReservationAddedByUser) event)
                .filter(reservation -> reservation.getWorkshopId().equals(workshopId));
    }

    private Optional<WorkshopAddedEvent> getWorkshop(String workshopId) {
        return getAllWorkshops()
                .filter(workshop -> workshop.getWorkshopId().equals(workshopId))
                .findFirst();
    }

    private Optional<ReservationAddedByUser> getReservation(AddReservationCommand reservationAdded) {
        return getReservationsForWorkshop(reservationAdded.getWorkshopId())
                .filter(reservation -> reservation.getEmail().equals(reservationAdded.getEmail()))
                .findFirst();
    }

    public EmailConfirmedByUser createEvent(ConfirmEmailCommand confirmEmailCommand) {
        Optional<ReservationAddedByUser> event = eventArrayList
                .stream()
                .filter(ae -> ae instanceof ReservationAddedByUser)
                .map(ae -> (ReservationAddedByUser) ae)
                .filter(ae -> ae.getReservationToken().equals(confirmEmailCommand.getReservationToken()))
                .findFirst();
        if (!(event.isPresent())) {
            throw new NoReservationFoundException("Could not find reservation with token [" + confirmEmailCommand.getReservationToken()+ "]");
        }
        ReservationAddedByUser reservation = event.get();
        Optional<AbstractEvent> any = eventArrayList.stream()
                .filter(ae -> {
                    if (!(ae instanceof EmailConfirmedByUser)) {
                        return false;
                    }
                    EmailConfirmedByUser confirmedByUser = (EmailConfirmedByUser) ae;
                    return confirmedByUser.getEmail().equals(reservation.getEmail());
                })
                .findAny();
        if (any.isPresent()) {
            throw new NoReservationFoundException("This email is already confirmed");
        }
        return new EmailConfirmedByUser(reservation.getEmail(),System.currentTimeMillis(),nextRevision());
    }
}
