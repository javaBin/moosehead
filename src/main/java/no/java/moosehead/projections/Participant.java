package no.java.moosehead.projections;

import no.java.moosehead.eventstore.AbstractReservationAdded;
import no.java.moosehead.eventstore.EmailConfirmedByUser;
import no.java.moosehead.eventstore.ReservationAddedByUser;

import java.time.OffsetDateTime;
import java.util.Optional;

public class Participant {
    private final String name;
    private final String email;
    private boolean emailConfirmed;
    private Workshop workshop;
    private String reservationToken;
    private OffsetDateTime confirmedAt;
    private int numberOfSeatsReserved;


    private Participant(String email, String fullname, int numberOfSeatsReserved, Workshop workshop, boolean emailConfirmed, String reservationToken, long eventMillis) {
        if (email == null) {
            throw new NullPointerException("Email can not be null");
        }
        if (numberOfSeatsReserved <1) {
            throw new IllegalArgumentException("numberOfSeatsReserved can not be less than 1");
        }
        this.numberOfSeatsReserved = numberOfSeatsReserved;
        this.workshop = workshop;
        this.reservationToken = reservationToken;
        this.name = fullname;
        this.email = email;
        this.emailConfirmed = emailConfirmed;
        if (emailConfirmed) {
            this.confirmedAt = systemMillisToOffsetTime(eventMillis);
        }
    }

    public static Participant confirmedParticipant(AbstractReservationAdded reservationAdded,Workshop workshop) {
        return new Participant(reservationAdded.getEmail(),
                reservationAdded.getFullname(),
                reservationAdded.getNumberOfSeatsReserved(),
                workshop,
                true,
                reservationAdded.getReservationToken(),
                reservationAdded.getSystemTimeInMillis());
    }

    public static Participant unconfirmedParticipant(ReservationAddedByUser reservationAddedByUser,Workshop workshop) {
        return new Participant(reservationAddedByUser.getEmail(),
                reservationAddedByUser.getFullname(),
                reservationAddedByUser.getNumberOfSeatsReserved(),
                workshop,
                false,
                reservationAddedByUser.getReservationToken(),
                reservationAddedByUser.getSystemTimeInMillis());
    }

    public static Participant dummyParticipant(String email) {
        return new Participant(email, null, 1, null, false, "token", 0);
    }


    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
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

        if (!email.equals(that.email)) return false;

        return true;
    }

    public String getWorkshopId() {
        return (workshop != null && workshop.getWorkshopData().getId() != null) ? workshop.getWorkshopData().getId() : null;
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }

    public Optional<OffsetDateTime> getConfirmedAt() {
        return Optional.ofNullable(confirmedAt);
    }

    public int getNumberOfSeatsReserved() {
        return numberOfSeatsReserved;
    }

    public String getReservationToken() {
        return reservationToken;
    }
}
