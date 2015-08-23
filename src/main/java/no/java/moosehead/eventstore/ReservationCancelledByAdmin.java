package no.java.moosehead.eventstore;


public class ReservationCancelledByAdmin extends AbstractReservationCancelled {

    public ReservationCancelledByAdmin() {}

    public ReservationCancelledByAdmin(long systemTimeInMillis, long revisionId, String email, String workshopId,int numSpotsCancellled) {
        super(systemTimeInMillis, revisionId,email,workshopId,numSpotsCancellled);
    }

}
