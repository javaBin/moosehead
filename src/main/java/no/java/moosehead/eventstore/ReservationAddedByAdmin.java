package no.java.moosehead.eventstore;

public class ReservationAddedByAdmin extends AbstractReservationAdded {

    public ReservationAddedByAdmin(long systemTimeInMillis, long revisionId, String email, String fullname, String workshopId) {
        super(systemTimeInMillis, revisionId,email,fullname,workshopId);
    }
}
