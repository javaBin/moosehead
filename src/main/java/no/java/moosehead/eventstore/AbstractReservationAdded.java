package no.java.moosehead.eventstore;

import no.java.moosehead.eventstore.core.AbstractEvent;
import no.java.moosehead.eventstore.utils.TokenGenerator;

public abstract class AbstractReservationAdded extends AbstractEvent implements UserWorkshopEvent {
    private String email;
    private String fullname;
    private String workshopId;
    private String reservationToken;
    private int numberOfSeatsReserved;

    public AbstractReservationAdded(long systemTimeInMillis, long revisionId, String email, String fullname, String workshopId, int numberOfSeatsReserved) {
        super(systemTimeInMillis, revisionId);
        this.email = email;
        this.fullname = fullname;
        this.workshopId = workshopId;
        this.reservationToken = TokenGenerator.randomUUIDString();
        this.numberOfSeatsReserved = numberOfSeatsReserved;
    }

    public AbstractReservationAdded() {
    }

    public String getEmail() {
        return email;
    }

    public String getFullname() {
        return fullname;
    }

    public String getWorkshopId() {
        return workshopId;
    }

    public String getReservationToken() {
        return reservationToken;
    }

    public int getNumberOfSeatsReserved() {
        return numberOfSeatsReserved;
    }
}
