package no.java.moosehead.commands;

public class CancelReservationCommand {
    private String email;
    private String workshopId;

    public CancelReservationCommand(String email,String workshopId) {
        this.email = email;
        this.workshopId = workshopId;
    }

    public String getWorkshopId() {
        return workshopId;
    }

    public String getEmail() {
        return email;
    }


    public String toString() {
        return "CancelReservationCommand for workshop:" + workshopId +
                " for user with email:" + email;
    }
}
