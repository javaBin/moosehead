package no.java.moosehead.aggregate;


import no.java.moosehead.MoosheadException;

public class ReservationCanNotBeCanceledException extends MoosheadException {
    public ReservationCanNotBeCanceledException(String message, Exception e) {
        super(message,e);
    }

    public ReservationCanNotBeCanceledException(String message) {
        super(message);
    }
}
