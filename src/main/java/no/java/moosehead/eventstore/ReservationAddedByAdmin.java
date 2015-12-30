package no.java.moosehead.eventstore;

import no.java.moosehead.domain.WorkshopReservation;

public class ReservationAddedByAdmin extends AbstractReservationAdded {

    public ReservationAddedByAdmin() {}

    public ReservationAddedByAdmin(WorkshopReservation workshopReservation) {
        super(workshopReservation);
    }


}
