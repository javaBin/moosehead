package no.java.moosehead.eventstore;

import no.java.moosehead.domain.WorkshopReservation;
import no.java.moosehead.eventstore.core.AbstractEvent;
import no.java.moosehead.eventstore.utils.TokenGenerator;
import org.jsonbuddy.JsonObject;

import java.util.Optional;

public abstract class AbstractReservationAdded extends AbstractEvent implements UserWorkshopEvent {
    private final WorkshopReservation workshopReservation;

    public AbstractReservationAdded() {
        workshopReservation = null;
    }

    public AbstractReservationAdded(WorkshopReservation workshopReservation) {
        super(workshopReservation.getSystemTimeInMillis(),workshopReservation.getRevisionId());
        this.workshopReservation = workshopReservation;
    }


    public String getEmail() {
        return workshopReservation.getEmail();
    }

    public String getFullname() {
        return workshopReservation.getFullname();
    }

    public String getWorkshopId() {
        return workshopReservation.getWorkshopId();
    }

    public String getReservationToken() {
        return workshopReservation.getReservationToken();
    }

    public int getNumberOfSeatsReserved() {
        return workshopReservation.getNumberOfSeatsReserved();
    }

    public JsonObject getAdditionalInfo() {
        return workshopReservation.getAdditionalInfo();
    }

    public WorkshopReservation getWorkshopReservation() {
        return workshopReservation;
    }
}
