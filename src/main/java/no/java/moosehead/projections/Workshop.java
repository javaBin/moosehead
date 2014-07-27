package no.java.moosehead.projections;

import no.java.moosehead.repository.WorkshopData;

import java.util.ArrayList;
import java.util.List;

public class Workshop {
    private WorkshopData workshopData;
    private int numberOfSeats;
    private List<Participant> participants = new ArrayList<>();

    public Workshop(WorkshopData workshopData, int numberOfSeats) {
        this.workshopData = workshopData;
        this.numberOfSeats = numberOfSeats;
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

    public int getNumberOfSeats() {
        return numberOfSeats;
    }
}
