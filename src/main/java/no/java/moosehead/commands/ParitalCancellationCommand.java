package no.java.moosehead.commands;

import no.java.moosehead.eventstore.AbstractReservationCancelled;

public class ParitalCancellationCommand {
    private final String email;
    private final String workshopId;
    private final int numberOfSpotsCancelled;

    public ParitalCancellationCommand(String email, String workshopId, int numberOfSpotsCancelled) {
        this.email = email;
        this.workshopId = workshopId;
        this.numberOfSpotsCancelled = numberOfSpotsCancelled;
    }

    public String getEmail() {
        return email;
    }

    public String getWorkshopId() {
        return workshopId;
    }

    public int getNumberOfSpotsCancelled() {
        return numberOfSpotsCancelled;
    }
}
