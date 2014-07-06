package no.java.moosehead.aggregate;

public class NoReservationFoundException extends RuntimeException {
    public NoReservationFoundException(String message) {
        super(message);
    }
}
