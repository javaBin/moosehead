package no.java.moosehead.eventstore;

import no.java.moosehead.eventstore.core.AbstractEvent;

public class WorkshopSizeChangedByAdmin extends AbstractEvent {
    private String workshopid;
    private int numspaces;

    public WorkshopSizeChangedByAdmin(long systemTimeInMillis, long revisionId, String workshopid, int numspaces) {
        super(systemTimeInMillis,revisionId);
        this.workshopid = workshopid;
        this.numspaces = numspaces;
    }

    public WorkshopSizeChangedByAdmin() {
    }

    public String getWorkshopid() {
        return workshopid;
    }

    public int getNumspaces() {
        return numspaces;
    }
}
