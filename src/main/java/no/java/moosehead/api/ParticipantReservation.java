package no.java.moosehead.api;

import java.util.Optional;

public class ParticipantReservation {
    private String email;
    private String workshopid;
    private String workshopname;
    private int numberOfSeatsReserved;
    private Optional<Integer> waitingListNumber;
    private ParticipantReservationStatus status;

    public ParticipantReservation(String email, String workshopid, String workshopname, ParticipantReservationStatus status, int numberOfSeatsReserved, Optional<Integer> waitingListNumber) {
        this.email = email;
        this.workshopid = workshopid;
        this.workshopname = workshopname;
        this.status = status;
        this.numberOfSeatsReserved = numberOfSeatsReserved;
        this.waitingListNumber = waitingListNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getWorkshopid() {
        return workshopid;
    }


    public String getWorkshopname() {
        return workshopname;
    }

    public ParticipantReservationStatus getStatus() {
        return status;
    }

    public int getNumberOfSeatsReserved() {
        return numberOfSeatsReserved;
    }

    public Optional<Integer> getWaitingListNumber() {
        return waitingListNumber;
    }
}
