package no.java.moosehead.api;

public class ParticipantReservation {
    private String email;
    private String workshopid;

    public ParticipantReservation(String email, String workshopid) {
        this.email = email;
        this.workshopid = workshopid;
    }

    public String getEmail() {
        return email;
    }

    public String getWorkshopid() {
        return workshopid;
    }
}
