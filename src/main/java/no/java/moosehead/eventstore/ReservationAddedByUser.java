package no.java.moosehead.eventstore;

import java.util.Optional;

public class ReservationAddedByUser extends AbstractReservationAdded {

    private Optional<String> googleUserEmail;

    public ReservationAddedByUser(){}

    public ReservationAddedByUser(long systemTimeInMillis, long revisionId, String email, String fullname, String workshopId,Optional<String> googleUserEmail) {
        super(systemTimeInMillis, revisionId,email,fullname,workshopId);
        this.googleUserEmail = googleUserEmail;
    }

    public Optional<String> getGoogleUserEmail() {
        return googleUserEmail;
    }
}
