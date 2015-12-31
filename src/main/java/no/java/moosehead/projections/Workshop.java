package no.java.moosehead.projections;

import no.java.moosehead.repository.WorkshopData;

import java.util.ArrayList;
import java.util.List;

public class Workshop {
    private WorkshopData workshopData;
    private final int numberOfSeats;
    private List<Participant> confirmed = new ArrayList<>();
    private List<Participant> notConfirmed = new ArrayList<>();

    public Workshop(WorkshopData workshopData, int numberOfSeats) {
        this.workshopData = workshopData;
        this.numberOfSeats = numberOfSeats;
    }


    public WorkshopData getWorkshopData() {
        return workshopData;
    }

    public List<Participant> getParticipants() {
        List<Participant> all = new ArrayList<>(confirmed);
        all.addAll(notConfirmed);
        return all;
    }

    public void addParticipant(Participant participant) {
        if (participant.isEmailConfirmed()) {
            confirmed.add(participant);
        } else {
            notConfirmed.add(participant);
        }
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void removeParticipant(String email) {
        Participant dummy = Participant.dummyParticipant(email);
        int pos = confirmed.indexOf(dummy);
        if (pos == -1) {
            pos = notConfirmed.indexOf(dummy);
            if (pos == -1) {
                throw new IllegalArgumentException("Participant not found " + email);
            }
            notConfirmed.remove(pos);
        } else {
            confirmed.remove(pos);
        }
    }

    public void moveToConfirmed(Participant participant) {
        if (confirmed.indexOf(participant) != -1) {
            return;
        }
        int pos = notConfirmed.indexOf(participant);
        if (pos == -1) {
            throw new IllegalArgumentException("Participant not found when confirming " + participant.getWorkshopReservation().getEmail());
        }
        notConfirmed.remove(pos);
        confirmed.add(participant);
    }

    public int waitingListNumber(Participant participant) {
        int pos = confirmed.indexOf(participant);
        if (pos < 0) {
            return -1;
        }

        // calculate the index where the workshop is full
        int seatCounter=0;
        int fullIndex=0;
        for (;fullIndex<confirmed.size() && seatCounter <= numberOfSeats; fullIndex++) {
            seatCounter+=confirmed.get(fullIndex).getNumberOfSeatsReserved();
        }

        if (seatCounter > numberOfSeats) {
            fullIndex--;
        }

        if (pos < fullIndex) {
            return 0;
        } else {
            return  pos + 1 - fullIndex;
        }
    }
}
