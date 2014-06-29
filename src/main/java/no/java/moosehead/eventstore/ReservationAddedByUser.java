package no.java.moosehead.eventstore;

public class ReservationAddedByUser extends AbstractEvent implements UserWorkshopEvent {
    private String email;
    private String fullname;
    private String workshopId;

    public ReservationAddedByUser(long systemTimeInMillis, long revisionId, String email, String fullname, String workshopId) {
        super(systemTimeInMillis, revisionId);
        this.email = email;
        this.fullname = fullname;
        this.workshopId = workshopId;
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
}
