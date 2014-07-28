package no.java.moosehead.aggregate;

import no.java.moosehead.MoosheadException;

public class WorkshopCanNotBeAddedException extends MoosheadException{
    WorkshopCanNotBeAddedException(String message){
        super(message);
    }
}
