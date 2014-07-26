package no.java.moosehead.projections;

import no.java.moosehead.repository.WorkshopData;

import java.util.List;

public class Workshop {
    private WorkshopData workshopData;
    private List<Participant> participants;
    public Workshop(WorkshopData workshopData) {
        this.workshopData = workshopData;
    }

    public WorkshopData getWorkshopData() {
        return workshopData;
    }
}
