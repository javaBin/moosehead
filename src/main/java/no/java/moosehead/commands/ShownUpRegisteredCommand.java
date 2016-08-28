package no.java.moosehead.commands;

public class ShownUpRegisteredCommand {
    private final String reservationToken;
    private final boolean shownUp;

    public ShownUpRegisteredCommand(String reservationToken, boolean shownUp) {
        this.reservationToken = reservationToken;
        this.shownUp = shownUp;
    }

    public String getReservationToken() {
        return reservationToken;
    }

    public boolean isShownUp() {
        return shownUp;
    }
}
