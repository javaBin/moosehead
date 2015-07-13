package no.java.moosehead.eventstore;


public class ReservationCancelledByUser extends AbstractReservationCancelled {

    public ReservationCancelledByUser(){}

    public ReservationCancelledByUser(long systemTimeInMillis, long revisionId, String email, String workshopId) {
        super(systemTimeInMillis, revisionId,email,workshopId);
    }

}
