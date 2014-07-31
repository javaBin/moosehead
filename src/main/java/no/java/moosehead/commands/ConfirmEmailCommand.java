package no.java.moosehead.commands;

public class ConfirmEmailCommand {
    long reservationRevisionId;

    public ConfirmEmailCommand(long reservationRevisionId) {
        this.reservationRevisionId = reservationRevisionId;
    }

    public long getReservationRevisionId() {
        return reservationRevisionId;
    }

    @Override
    public String toString() {
        return "Confirm email command for " + reservationRevisionId;
    }
}
