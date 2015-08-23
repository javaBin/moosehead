package no.java.moosehead.eventstore;

public class ReservationPartallyCancelled extends AbstractReservationCancelled {
    public ReservationPartallyCancelled(long systemTimeInMillis, long revisionId, String email, String workshopId, int numSpotsCancellled) {
        super(systemTimeInMillis, revisionId,email,workshopId,numSpotsCancellled);
    }

    public ReservationPartallyCancelled() {
    }
}
