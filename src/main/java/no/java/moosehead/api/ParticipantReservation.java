package no.java.moosehead.api;

public class ParticipantReservation {
    private String email;
    private String workshopid;
    private String workshopname;
    private boolean confirmed;

    public ParticipantReservation(String email, String workshopid,String workshopname,boolean confirmed) {
        this.email = email;
        this.workshopid = workshopid;
        this.workshopname = workshopname;
        this.confirmed = confirmed;
    }

    public String getEmail() {
        return email;
    }

    public String getWorkshopid() {
        return workshopid;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getWorkshopname() {
        return workshopname;
    }
}
