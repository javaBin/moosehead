package no.java.moosehead.eventstore;

import no.java.moosehead.eventstore.core.AbstractEvent;

public class ShowUpRegisteredByAdmin extends AbstractEvent {
    private boolean shownUp;
    private String reservationToken;

    public ShowUpRegisteredByAdmin(long systemTimeInMillis, long revisionId, boolean shownUp, String reservationToken) {
        super(systemTimeInMillis, revisionId);
        this.shownUp = shownUp;
        this.reservationToken = reservationToken;
    }

    public boolean isShownUp() {
        return shownUp;
    }

    public String getReservationToken() {
        return reservationToken;
    }
}
