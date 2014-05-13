package no.java.moosehead.aggregate;


public class ReservationCanNotBeAddedException extends RuntimeException {
    public ReservationCanNotBeAddedException(String message) {
        super(message);
    }
}
