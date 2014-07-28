package no.java.moosehead.aggregate;

import no.java.moosehead.MoosheadException;

public class NoReservationFoundException extends MoosheadException {
    public NoReservationFoundException(String message) {
        super(message);
    }
}
