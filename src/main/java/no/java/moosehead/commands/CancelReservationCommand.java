package no.java.moosehead.commands;

public class CancelReservationCommand {
    private String email;
    private String workshopId;
    private AuthorEnum authorEnum;

    public CancelReservationCommand(String email,String workshopId, AuthorEnum authorEnum) {
        this.email = email;
        this.workshopId = workshopId;
        this.authorEnum = authorEnum;
    }

    public String getWorkshopId() {
        return workshopId;
    }

    public String getEmail() {
        return email;
    }


    public String toString() {
        return "CancelReservationCommand for workshop:" + workshopId +
                " for user with email:" + email + " by " + authorEnum.name();
    }

    public AuthorEnum getAuthorEnum() {
        return authorEnum;
    }
}
