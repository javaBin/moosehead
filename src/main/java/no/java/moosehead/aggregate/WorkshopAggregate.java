package no.java.moosehead.aggregate;

import no.java.moosehead.commands.*;
import no.java.moosehead.controller.SystemSetup;
import no.java.moosehead.eventstore.*;
import no.java.moosehead.eventstore.core.AbstractEvent;
import no.java.moosehead.eventstore.core.EventSubscription;
import no.java.moosehead.repository.WorkshopData;
import no.java.moosehead.saga.EmailSender;
import no.java.moosehead.web.Configuration;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

public class WorkshopAggregate implements EventSubscription {

    private ArrayList<AbstractEvent> eventArrayList = new ArrayList<>();
    private EmailSender emailSender;

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
                        case BEER_WORKSHOP:
                            return new BeerWorkshopAddedByAdmin(System.currentTimeMillis(),
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
        Optional<WorkshopAddedEvent> workshop = getWorkshop(addReservationCommand.getWorkshopReservation().getWorkshopId());
        if (workshop.isPresent()) {
            if (OffsetDateTime.now().isBefore(computeOpenTime(workshop.get()))) {
                throw new ReservationCanNotBeAddedException("Reservations has not opened yet for this workshop");
            }
            Optional<ReservationAddedByUser> reservation = getActiveReservationIfPresent(addReservationCommand);
            if (reservation.isPresent()) {
                emailSender.sendEmailConfirmation(reservation.get().getEmail(),reservation.get().getReservationToken(),workshop.get().getWorkshopId());
                throw new ReservationCanNotBeAddedException(String.format(
                        "You have already tried to register with email [%s]. You need to click the link in the email to reserve your spot. We have now sent you the email again in case the first one did not reach you.",addReservationCommand.getWorkshopReservation().getEmail())
                );
            }

            switch (addReservationCommand.getAuthorEnum()) {
                case ADMIN:
                    return new ReservationAddedByAdmin(addReservationCommand.getWorkshopReservation().copy()
                                    .setSystemTimeInMillis(System.currentTimeMillis())
                                    .setRevisionId(nextRevision())
                                    .create()
                            );
                case USER:
                    WorkshopTypeEnum workshopTypeEnum = workshop.get().getWorkshopData().map(WorkshopData::getWorkshopTypeEnum).orElse(WorkshopTypeEnum.NORMAL_WORKSHOP);
                    int maxNumberOfSeatsToReserve = workshopTypeEnum.equals(WorkshopTypeEnum.KIDSAKODER_WORKSHOP)?Configuration.maxNumberOfSeatsToReserve():1;
                    if (addReservationCommand.getWorkshopReservation().getNumberOfSeatsReserved() > maxNumberOfSeatsToReserve || addReservationCommand.getWorkshopReservation().getNumberOfSeatsReserved() < 1 ) {
                        throw new ReservationCanNotBeAddedException("Too many/few seats reserved [" + addReservationCommand.getWorkshopReservation().getNumberOfSeatsReserved() + "]");
                    }
                    return new ReservationAddedByUser(addReservationCommand.getWorkshopReservation().copy()
                                    .setSystemTimeInMillis(System.currentTimeMillis())
                                    .setRevisionId(nextRevision())
                                    .create()
                            );
                default:
                    throw new ReservationCanNotBeAddedException("Reservation cannot be added", new IllegalArgumentException("AuthorEnum + " + AuthorEnum.SYSTEM + " is not supported"));
            }
        } else {
            throw new ReservationCanNotBeAddedException("The workshop in [" + addReservationCommand + "] does not exists");
        }
    }



    private OffsetDateTime computeOpenTime(WorkshopAddedEvent workshop) {
        Optional<WorkshopData> dataOptional = workshop.getWorkshopData();
        if (!dataOptional.isPresent()) {
            return Configuration.openTime();
        }
        Optional<Instant> registrationOpens = dataOptional.get().getRegistrationOpens();
        if (!registrationOpens.isPresent()) {
            return Configuration.openTime();
        }

        OffsetDateTime opening = registrationOpens.get().atOffset(ZoneOffset.ofHours(2));
        return opening;
    }

    public AbstractReservationCancelled createEvent(CancelReservationCommand cancelReservationCommand) {
        int count = spotsReserved(userWorkshopEvents(cancelReservationCommand.getWorkshopId(), cancelReservationCommand.getEmail()));
        if (count == 0) {
            throw new NoReservationFoundException(String.format("The reservation for %s in %s not found",cancelReservationCommand.getEmail(),cancelReservationCommand.getWorkshopId()));
        }
        switch (cancelReservationCommand.getAuthorEnum()) {
            case USER:
                return new ReservationCancelledByUser(System.currentTimeMillis(), nextRevision(), cancelReservationCommand.getEmail(), cancelReservationCommand.getWorkshopId(),count);
            case ADMIN:
                return new ReservationCancelledByAdmin(System.currentTimeMillis(), nextRevision(), cancelReservationCommand.getEmail(), cancelReservationCommand.getWorkshopId(),count);
            default:
                throw new ReservationCanNotBeCanceledException("Reservation cannot be canceled", new IllegalArgumentException("AuthorEnum + " + AuthorEnum.SYSTEM + " is not supported"));
        }
    }

    public AbstractReservationCancelled createEvent(ParitalCancellationCommand paritalCancellationCommand) {
        int count = spotsReserved(userWorkshopEvents(paritalCancellationCommand.getWorkshopId(), paritalCancellationCommand.getEmail()));
        if (count == 0) {
            throw new NoReservationFoundException(String.format("The reservation for %s in %s not found",paritalCancellationCommand.getEmail(),paritalCancellationCommand.getWorkshopId()));
        }
        return new ReservationPartallyCancelled(System.currentTimeMillis(), nextRevision(), paritalCancellationCommand.getEmail(), paritalCancellationCommand.getWorkshopId(),paritalCancellationCommand.getNumberOfSpotsCancelled());
    }

    private int spotsReserved(Stream<? extends UserWorkshopEvent> userWorkshops) {
        return userWorkshops
                .map(uw -> {
                    if (uw instanceof AbstractReservationAdded) {
                        return ((AbstractReservationAdded) uw).getNumberOfSeatsReserved();
                    }
                    if (uw instanceof AbstractReservationCancelled) {
                        return -((AbstractReservationCancelled) uw).getNumSpotsCancelled();
                    }
                    return 0;
                })
                .reduce(Integer::sum)
                .orElse(0);
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

    private Optional<WorkshopAddedEvent> getWorkshop(String workshopId) {
        return getAllWorkshops()
                .filter(workshop -> workshop.getWorkshopId().equals(workshopId))
                .findFirst();
    }

    private Optional<ReservationAddedByUser> getActiveReservationIfPresent(AddReservationCommand reservationAdded) {

        Optional<UserWorkshopEvent> lastEvent =
                userWorkshopEvents(reservationAdded.getWorkshopReservation().getWorkshopId(),
                reservationAdded.getWorkshopReservation().getEmail())
                        .map(uw -> (UserWorkshopEvent)uw)
                        .reduce((a, b) -> b);

        if (lastEvent.isPresent() && lastEvent.get() instanceof ReservationAddedByUser){
            return Optional.of((ReservationAddedByUser)lastEvent.get());
        }else{
            return Optional.empty();
        }

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

    public void setEmailSender(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public ShowUpRegisteredByAdmin createEvent(ShownUpRegisteredCommand shownUpRegisteredCommand) {
        return new ShowUpRegisteredByAdmin(System.currentTimeMillis(),nextRevision(),shownUpRegisteredCommand.isShownUp(),shownUpRegisteredCommand.getReservationToken());
    }
}
