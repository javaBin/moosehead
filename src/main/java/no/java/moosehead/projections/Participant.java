package no.java.moosehead.projections;

import no.java.moosehead.eventstore.ReservationAddedByUser;

public class Participant {
    private final String name;
    private final String email;
    private boolean emailConfirmed;
    private Workshop workshop;
    private long reservationEventRevisionId;


    private Participant(String email, String fullname, Workshop workshop, boolean emailConfirmed, long reservationEventRevisionId) {
        if (email == null) {
            throw new NullPointerException("Email can not be null");
        }
        this.workshop = workshop;
        this.reservationEventRevisionId = reservationEventRevisionId;
        this.name = fullname;
        this.email = email;
        this.emailConfirmed = emailConfirmed;
    }

    public static Participant confirmedParticipant(ReservationAddedByUser reservationAddedByUser,Workshop workshop) {
        return new Participant(reservationAddedByUser.getEmail(),reservationAddedByUser.getFullname(),workshop,true, reservationAddedByUser.getRevisionId());
    }

    public static Participant unconfirmedParticipant(ReservationAddedByUser reservationAddedByUser,Workshop workshop) {
        return new Participant(reservationAddedByUser.getEmail(),reservationAddedByUser.getFullname(),workshop,false, reservationAddedByUser.getRevisionId());
    }

    public static Participant dummyParticipant(String email) {
        return new Participant(email,null,null,false, 0);
    }


    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void confirmEmail() {
        emailConfirmed = true;
        this.workshop.moveToConfirmed(this);
    }

    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    public int waitingListNumber() {
        return workshop.waitingListNumber(this);
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

    public long getReservationEventRevisionId() {
        return reservationEventRevisionId;
    }
}
