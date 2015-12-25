package no.java.moosehead.eventstore;

import java.util.Optional;

public class ReservationAddedByUser extends AbstractReservationAdded {

    private Optional<String> googleUserEmail;

    public ReservationAddedByUser(){}

    public ReservationAddedByUser(Builder builder) {
        super(builder);
        this.googleUserEmail = builder.googleUserEmail;
    }



    public Optional<String> getGoogleUserEmail() {
        return googleUserEmail;
    }
}
