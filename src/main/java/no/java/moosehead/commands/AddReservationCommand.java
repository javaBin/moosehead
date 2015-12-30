package no.java.moosehead.commands;

import no.java.moosehead.domain.WorkshopReservation;

import java.util.Optional;

public class AddReservationCommand {
    private final WorkshopReservation workshopReservation;
    private final AuthorEnum authorEnum;

    public AddReservationCommand(WorkshopReservation workshopReservation, AuthorEnum authorEnum) {
        this.workshopReservation = workshopReservation;
        this.authorEnum = authorEnum;
    }

    public String getWorkshopId() {
        return workshopReservation.getWorkshopId();
    }

    public String getEmail() {
        return workshopReservation.getEmail();
    }

    public String getFullname() {
        return workshopReservation.getFullname();
    }


    public AuthorEnum getAuthorEnum() {
        return authorEnum;
    }

    public Optional<String> getGoogleEmail() {
        return workshopReservation.getGoogleUserEmail();
    }


    public int getNumberOfSeatsReserved() {
        return workshopReservation.getNumberOfSeatsReserved();
    }

    public String toString() {
        return "AddReservationCommand for workshop author enumn :" + authorEnum + " info: " + workshopReservation;
    }

}
