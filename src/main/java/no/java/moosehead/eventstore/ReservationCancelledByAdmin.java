package no.java.moosehead.eventstore;


public class ReservationCancelledByAdmin extends AbstractReservationCancelled {

    public ReservationCancelledByAdmin(long systemTimeInMillis, long revisionId, String email, String workshopId) {
        super(systemTimeInMillis, revisionId,email,workshopId);
    }

}
