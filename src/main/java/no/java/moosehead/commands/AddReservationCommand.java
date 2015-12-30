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

    public WorkshopReservation getWorkshopReservation() {
        return workshopReservation;
    }

    public AuthorEnum getAuthorEnum() {
        return authorEnum;
    }

    public String toString() {
        return "AddReservationCommand for workshop author enumn :" + authorEnum + " info: " + workshopReservation;
    }

}
