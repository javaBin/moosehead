package no.java.moosehead.commands;

import java.util.Optional;

public class AddReservationCommand {
    private final String email;
    private final String fullname;
    private final String workshopId;
    private final AuthorEnum authorEnum;
    private final Optional<String> googleEmail;
    private final WorkshopTypeEnum workshopType;
    private final int numberOfSeatsReserved;

    public AddReservationCommand(String email, String fullname, String workshopId, AuthorEnum authorEnum, Optional<String> googleEmail, WorkshopTypeEnum workshopType,int numberOfSeatsReserved) {
        this.email = email;
        this.fullname = fullname;
        this.workshopId = workshopId;
        this.authorEnum = authorEnum;
        this.googleEmail = googleEmail;
        this.workshopType = workshopType;
        this.numberOfSeatsReserved = numberOfSeatsReserved;
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

    public WorkshopTypeEnum getWorkshopType() {
        return workshopType;
    }

    public int getNumberOfSeatsReserved() {
        return numberOfSeatsReserved;
    }
}
