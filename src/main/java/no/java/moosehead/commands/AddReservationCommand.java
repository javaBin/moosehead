package no.java.moosehead.commands;

public class AddReservationCommand {
    private String email;
    private String fullname;
    private String workshopId;
    private Author author;

    public AddReservationCommand(String email, String fullname, String workshopId, Author author) {
        this.email = email;
        this.fullname = fullname;
        this.workshopId = workshopId;
        this.author = author;
    }

    public String getWorkshopId() {
        return workshopId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullname() {
        return fullname;
    }

    public String toString() {
        return "AddReservationCommand for workshop:" + workshopId +
                " for user:" + fullname +
                " with email:" + email +
                " by " + author.name();
    }

    public Author getAuthor() {
        return author;
    }
}
