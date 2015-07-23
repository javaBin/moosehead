package no.java.moosehead.commands;

import java.util.Optional;

public class AddReservationCommand {
    private String email;
    private String fullname;
    private String workshopId;
    private Author author;
    private Optional<String> googleEmail;

    public AddReservationCommand(String email, String fullname, String workshopId, Author author, Optional<String> googleEmail) {
        this.email = email;
        this.fullname = fullname;
        this.workshopId = workshopId;
        this.author = author;
        this.googleEmail = googleEmail;
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
                " by " + author.name() +
                " googlemail: " + googleEmail;
    }

    public Author getAuthor() {
        return author;
    }

    public Optional<String> getGoogleEmail() {
        return googleEmail;
    }
}
