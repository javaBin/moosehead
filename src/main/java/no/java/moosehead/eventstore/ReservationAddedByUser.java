package no.java.moosehead.eventstore;

import java.util.Optional;

public class ReservationAddedByUser extends AbstractReservationAdded {

    private Optional<String> googleUserEmail;

    public ReservationAddedByUser(){}

    public ReservationAddedByUser(long systemTimeInMillis, long revisionId, String email, String fullname, String workshopId,Optional<String> googleUserEmail, int numberOfSeatsReserved) {
        this(AbstractReservationAdded.builder()
                .setSystemTimeInMillis(systemTimeInMillis)
                .setRevisionId(revisionId)
                .setEmail(email)
                .setFullname(fullname)
                .setWorkshopId(workshopId)
                .setGoogleUserEmail(googleUserEmail)
                .setNumberOfSeatsReserved(numberOfSeatsReserved)
        );
    }

    public ReservationAddedByUser(Builder builder) {
        super(builder);
        this.googleUserEmail = builder.googleUserEmail;
    }



    public Optional<String> getGoogleUserEmail() {
        return googleUserEmail;
    }
}
