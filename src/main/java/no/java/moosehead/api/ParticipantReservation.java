package no.java.moosehead.api;

public class ParticipantReservation {
    private String email;
    private String workshopid;
    private String workshopname;
    private ParticipantReservationStatus status;

    public ParticipantReservation(String email, String workshopid,String workshopname,ParticipantReservationStatus status) {
        this.email = email;
        this.workshopid = workshopid;
        this.workshopname = workshopname;
        this.status = status;
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
}
