package no.java.moosehead.commands;

public class ConfirmEmailCommand {
    String reservationToken;

    public ConfirmEmailCommand(String reservationToken) {
        this.reservationToken = reservationToken;
    }

    public String getReservationToken() {
        return reservationToken;
    }

    @Override
    public String toString() {
        return "Confirm email command for [" + reservationToken + "]";
    }
}
