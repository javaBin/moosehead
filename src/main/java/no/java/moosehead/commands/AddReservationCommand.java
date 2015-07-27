package no.java.moosehead.commands;

import java.util.Optional;

public class AddReservationCommand {
    private String email;
    private String fullname;
    private String workshopId;
    private AuthorEnum authorEnum;
    private Optional<String> googleEmail;
    private WorkshopTypeEnum workshopType = WorkshopTypeEnum.NORMAL_WORKSHOP;

    public AddReservationCommand(String email, String fullname, String workshopId, AuthorEnum authorEnum, Optional<String> googleEmail) {
        this.email = email;
        this.fullname = fullname;
        this.workshopId = workshopId;
        this.authorEnum = authorEnum;
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
                " by " + authorEnum.name() +
                " googlemail: " + googleEmail;
    }

    public AuthorEnum getAuthorEnum() {
        return authorEnum;
    }

    public Optional<String> getGoogleEmail() {
        return googleEmail;
    }
}
