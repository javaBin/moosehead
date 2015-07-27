package no.java.moosehead.eventstore;

public class ReservationAddedByAdmin extends AbstractReservationAdded {

    public ReservationAddedByAdmin() {}

    public ReservationAddedByAdmin(long systemTimeInMillis, long revisionId, String email, String fullname, String workshopId, int numberOfSeatsReserved) {
        super(systemTimeInMillis, revisionId, email, fullname, workshopId, numberOfSeatsReserved);
    }
}
