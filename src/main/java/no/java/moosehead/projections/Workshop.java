package no.java.moosehead.projections;

import no.java.moosehead.repository.WorkshopData;

import java.util.ArrayList;
import java.util.List;

public class Workshop {
    private WorkshopData workshopData;
    private List<Participant> participants = new ArrayList<>();

    public Workshop(WorkshopData workshopData) {
        this.workshopData = workshopData;
    }


    public WorkshopData getWorkshopData() {
        return workshopData;
    }

    public List<Participant> getParticipants() {
        return new ArrayList<>(participants);
    }

    public void addParticipant(Participant participant) {
        participants.add(participant);
    }
}
