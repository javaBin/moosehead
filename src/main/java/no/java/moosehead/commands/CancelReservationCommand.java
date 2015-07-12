package no.java.moosehead.commands;

public class CancelReservationCommand {
    private String email;
    private String workshopId;
    private Author author;

    public CancelReservationCommand(String email,String workshopId, Author author) {
        this.email = email;
        this.workshopId = workshopId;
        this.author = author;
    }

    public String getWorkshopId() {
        return workshopId;
    }

    public String getEmail() {
        return email;
    }


    public String toString() {
        return "CancelReservationCommand for workshop:" + workshopId +
                " for user with email:" + email + " by " + author.name();
    }

    public Author getAuthor() {
        return author;
    }
}
