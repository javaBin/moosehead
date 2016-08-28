package no.java.moosehead.projections;

import no.java.moosehead.domain.WorkshopReservation;
import no.java.moosehead.eventstore.AbstractReservationAdded;
import no.java.moosehead.eventstore.EmailConfirmedByUser;
import no.java.moosehead.eventstore.ReservationAddedByUser;
import org.jsonbuddy.JsonObject;

import java.time.OffsetDateTime;
import java.util.Optional;

public class Participant {
    private boolean emailConfirmed;
    private Workshop workshop;
    private OffsetDateTime confirmedAt;
    private WorkshopReservation workshopReservation;
    private int numberOfSeatsReserved;
    private boolean hasShownUp = false;


    private Participant(WorkshopReservation workshopReservation,Workshop workshop, boolean emailConfirmed) {
        if (workshopReservation == null) {
            throw new NullPointerException("WorkshopReservation can not be null");
        }
        if (workshopReservation.getEmail() == null) {
            throw new NullPointerException("Email can not be null");
        }
        if (workshopReservation.getNumberOfSeatsReserved() <1) {
            throw new IllegalArgumentException("numberOfSeatsReserved can not be less than 1");
        }
        this.workshopReservation = workshopReservation;
        this.workshop = workshop;
        this.emailConfirmed = emailConfirmed;
        if (emailConfirmed) {
            this.confirmedAt = systemMillisToOffsetTime(workshopReservation.getSystemTimeInMillis());
        }
        this.numberOfSeatsReserved = workshopReservation.getNumberOfSeatsReserved();
    }

    public static Participant confirmedParticipant(AbstractReservationAdded reservationAdded,Workshop workshop) {
        return new Participant(reservationAdded.getWorkshopReservation(),
                workshop,
                true);
    }

    public static Participant unconfirmedParticipant(ReservationAddedByUser reservationAddedByUser,Workshop workshop) {
        return new Participant(reservationAddedByUser.getWorkshopReservation(),
                workshop,
                false);
    }

    public static Participant dummyParticipant(String email) {
        WorkshopReservation workshopReservation = WorkshopReservation.builder()
                .setEmail(email)
                .setNumberOfSeatsReserved(1)
                .setReservationToken("token")
                .create();
        return new Participant(workshopReservation,null,false);
    }


    public void confirmEmail(EmailConfirmedByUser emailConfirmedByUser) {
        long confirmedAtMillis = emailConfirmedByUser.getSystemTimeInMillis();
        confirmedAt = systemMillisToOffsetTime(confirmedAtMillis);
        emailConfirmed = true;
        this.workshop.moveToConfirmed(this);
    }

    private OffsetDateTime systemMillisToOffsetTime(long systemMillis) {
        OffsetDateTime reference = OffsetDateTime.now();
        long difference = reference.toEpochSecond() - (systemMillis/1000);
        return reference.minusSeconds(difference);
    }

    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    public int waitingListNumber() {
        return workshop.waitingListNumber(this);
    }

    public boolean isWaiting() {
        return waitingListNumber() > 0;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Participant that = (Participant) o;

        if (!workshopReservation.getEmail().equals(that.workshopReservation.getEmail())) return false;

        return true;
    }

    public String getWorkshopId() {
        return (workshop != null && workshop.getWorkshopData().getId() != null) ? workshop.getWorkshopData().getId() : null;
    }

    @Override
    public int hashCode() {
        return workshopReservation.getEmail().hashCode();
    }

    public Optional<OffsetDateTime> getConfirmedAt() {
        return Optional.ofNullable(confirmedAt);
    }

    public int getNumberOfSeatsReserved() {
        return numberOfSeatsReserved;
    }


    public void reduceReservedSeats(int numSpotsCancelled) {
        numberOfSeatsReserved-=numSpotsCancelled;
    }

    public WorkshopReservation getWorkshopReservation() {
        return workshopReservation;
    }

    public boolean isHasShownUp() {
        return hasShownUp;
    }

    public Participant setHasShownUp(boolean hasShownUp) {
        this.hasShownUp = hasShownUp;
        return this;
    }
}
