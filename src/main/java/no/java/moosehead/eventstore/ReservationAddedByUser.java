package no.java.moosehead.eventstore;

import no.java.moosehead.domain.WorkshopReservation;

import java.util.Optional;

public class ReservationAddedByUser extends AbstractReservationAdded {

    public ReservationAddedByUser(){}

    public ReservationAddedByUser(WorkshopReservation workshopReservation) {
        super(workshopReservation);
    }



    public Optional<String> getGoogleUserEmail() {
        return getWorkshopReservation().getGoogleUserEmail();
    }
}
