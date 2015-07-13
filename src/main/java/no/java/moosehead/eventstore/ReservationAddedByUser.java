package no.java.moosehead.eventstore;

public class ReservationAddedByUser extends AbstractReservationAdded {

    public ReservationAddedByUser(){}

    public ReservationAddedByUser(long systemTimeInMillis, long revisionId, String email, String fullname, String workshopId) {
        super(systemTimeInMillis, revisionId,email,fullname,workshopId);
    }
}
