package no.java.moosehead.eventstore;

public class WorkshopAddedByAdmin extends AbstractEvent {
    private String workshopId;
    private int numberOfSeats;


    public WorkshopAddedByAdmin() {
        super();
    }

    public WorkshopAddedByAdmin(long systemTimeInMillis, long revisionId, String workshopId, int numberOfSeats) {
        super(systemTimeInMillis, revisionId);
        this.workshopId = workshopId;
        this.numberOfSeats = numberOfSeats;
    }

    public String getWorkshopId() {
        return workshopId;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }
}
