package no.java.moosehead.aggregate;


import no.java.moosehead.MoosheadException;

public class ReservationCanNotBeAddedException extends MoosheadException {
    public ReservationCanNotBeAddedException(String message) {
        super(message);
    }
}
