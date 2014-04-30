package no.java.moosehead.eventstore;

public class WorkshopAddedByAdmin extends AbstractEvent {
    private String workshopId;

    public WorkshopAddedByAdmin(long systemTimeInMillis, long revisionId, String workshopId) {
        super(systemTimeInMillis, revisionId);
        this.workshopId = workshopId;
    }

    public String getWorkshopId() {
        return workshopId;
    }
}
